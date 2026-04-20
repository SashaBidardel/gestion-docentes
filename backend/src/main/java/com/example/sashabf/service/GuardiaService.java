package com.example.sashabf.service;

import com.example.sashabf.dto.GuardiaResultadoDTO;
import com.example.sashabf.entity.*;
import com.example.sashabf.exception.GuardiaYaAsignadaException;
import com.example.sashabf.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GuardiaService {

    @Autowired private DocenteRepository docenteRepository;
    @Autowired private HorarioRepository horarioRepository;
    @Autowired private GuardiaRepository guardiaRepository;
    @Autowired private AsuntoPropioRepository asuntoRepo;
    
    @PersistenceContext
    private EntityManager entityManager;

    // --- MÉTODOS DE CONSULTA ---
    public Page<GuardiaResultadoDTO> listarGuardiasPaginadas(Pageable pageable) {
        return guardiaRepository.findAll(pageable).map(this::convertirADTO);
    }

    public List<Guardia> obtenerTodas() {
        return guardiaRepository.findAllByOrderByIdDesc();
    }

    public List<GuardiaResultadoDTO> obtenerHistorialPorDocente(Long docenteId) {
        return guardiaRepository.findByDocente_Id(docenteId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<GuardiaResultadoDTO> obtenerGuardiasPorEmailDocente(String email) {
        return guardiaRepository.findByDocenteEmail(email).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // --- MÉTODOS DE ESCRITURA ---

    // Este es el que necesita el Test
    public Guardia guardar(Guardia guardia) {
        return guardiaRepository.save(guardia);
    }

    @Transactional
    public void generarGuardiaDesdeAsunto(Long asuntoId, String material, String anotacion) {
        AsuntoPropio ap = asuntoRepo.findById(asuntoId)
                .orElseThrow(() -> new RuntimeException("Asunto no encontrado"));

        Docente docenteAusente = ap.getDocente();
        if (docenteAusente == null) return;

        long idAusente = docenteAusente.getId(); 
        int diaSemana = ap.getDiaSolicitado().getDayOfWeek().getValue();
        LocalDate fechaAusencia = ap.getDiaSolicitado();

        List<Horario> horariosParaSustituir = horarioRepository.findAll().stream()
            .filter(h -> h.getDocente() != null && h.getDocente().getId() == idAusente && h.getDia() == diaSemana && !"G".equalsIgnoreCase(h.getTipo()))
            .collect(Collectors.toList());

        for (Horario h : horariosParaSustituir) {
            procesarSustitucion(h.getId(), material, anotacion, fechaAusencia);
        }
    }

    @Transactional
    public void procesarSustitucion(Long idHorario, String material, String anotacion, LocalDate fechaFalta) {
        try {
            Horario h = horarioRepository.findById(idHorario)
                    .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

            int diaSemanaHorario = h.getDia(); 
            fechaFalta = LocalDate.now().with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.of(diaSemanaHorario)));
            
            if (fechaFalta.equals(LocalDate.now()) && h.getDia() > 5) return;

            Optional<Guardia> guardiaExistente = guardiaRepository.findByHorarioIdAndFecha(idHorario, fechaFalta);

            if (guardiaExistente.isPresent()) {
                Guardia gPrevia = guardiaExistente.get();
                if (gPrevia.getDocente() != null) {
                    throw new GuardiaYaAsignadaException("Ya está cubierto por " + gPrevia.getDocente().getNombre());
                } 
                guardiaRepository.delete(gPrevia);
                guardiaRepository.flush();
            }

            Docente ausente = h.getDocente();
            if (ausente == null || ausente.getDepartamento() == null) return;

            long deptoId = ausente.getDepartamento().getId();

            // 4. FILTRAR CANDIDATOS
            List<Docente> candidatos = docenteRepository.findAll().stream()
                .filter(d -> d.getId() != ausente.getId())
                .filter(d -> d.getHorarios().stream().anyMatch(slot -> 
                    slot.getDia() == h.getDia() && slot.getHora() == h.getHora() && "G".equalsIgnoreCase(slot.getTipo())))
                .collect(Collectors.toList());

            // 5. FILTRAR POR DEPARTAMENTO
            List<Docente> mismoDepto = candidatos.stream()
                .filter(d -> d.getDepartamento() != null && d.getDepartamento().getId() == deptoId)
                .collect(Collectors.toList());

            Docente elegido = null;
            if (!mismoDepto.isEmpty()) {
                elegido = mismoDepto.stream()
                    .min(Comparator.comparingLong((Docente d) -> guardiaRepository.countByDocenteId(d.getId()))
                        .thenComparing(Comparator.comparingInt(Docente::getPosicion).reversed()))
                    .orElse(null);
            }

            // 6. CREAR Y GUARDAR
            Guardia g = new Guardia();
            g.setHorario(h);
            g.setFecha(fechaFalta);
            g.setMaterial(material);
            g.setAnotacion(anotacion);
            g.setDocente(elegido);

            guardiaRepository.save(g);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private GuardiaResultadoDTO convertirADTO(Guardia g) {
        GuardiaResultadoDTO dto = new GuardiaResultadoDTO();
        dto.setId(g.getId());
        dto.setFecha(g.getFecha());
        dto.setMaterial(g.getMaterial() != null ? g.getMaterial() : "Sin material");
        dto.setAnotacion(g.getAnotacion() != null ? g.getAnotacion() : "Sin anotaciones");

        if (g.getHorario() != null) {
            Horario h = g.getHorario();
            dto.setIdHorario(h.getId());
            dto.setDia(h.getDia());
            dto.setHora(h.getHora());
            dto.setAula(h.getAula() != null ? h.getAula() : "N/A");
            dto.setAsignatura(h.getAsignatura() != null ? h.getAsignatura().getNombre() : "Guardia de Centro");
            if (h.getDocente() != null) {
                dto.setProfesorAusente(h.getDocente().getNombre() + " " + h.getDocente().getApellidos());
            }
        }

        if (g.getDocente() != null) {
            dto.setSustitutoAsignado(g.getDocente().getNombre() + " " + g.getDocente().getApellidos());
            dto.setConteoGuardias((int) guardiaRepository.countByDocenteId(g.getDocente().getId()));
        } else {
            dto.setSustitutoAsignado(null); 
            dto.setConteoGuardias(0);
        }
        return dto;
    }
}