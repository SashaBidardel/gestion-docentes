package com.example.sashabf.service;

import com.example.sashabf.dto.AsuntoPropioDTO;
import com.example.sashabf.entity.AsuntoPropio;
import com.example.sashabf.entity.Docente;
import com.example.sashabf.entity.Guardia;
import com.example.sashabf.exception.BadRequestException;
import com.example.sashabf.exception.ResourceAlreadyExistsException;
import com.example.sashabf.exception.ResourceNotFoundException;
import com.example.sashabf.repository.AsuntoPropioRepository;
import com.example.sashabf.repository.GuardiaRepository;
import com.example.sashabf.repository.HorarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AsuntoPropioService {

    @Autowired
    private AsuntoPropioRepository repo;
    
    @Autowired
    private GuardiaRepository guardiaRepository;
    
    @Autowired
    private ExcelService excelService;
    
    @Autowired
    private HorarioRepository horarioRepository;
    
    @Autowired
    private MailService mailService;
    @Autowired
    private GuardiaService guardiaService;
    /**
     * Solicitar un asunto propio validando límites.
     */
    @Transactional
    public void solicitarAsuntoPropio(AsuntoPropio solicitud, Docente docente, String mat, String anot) {
        // 0. VALIDACIÓN DE DATOS BÁSICOS
        if (docente == null || solicitud.getDiaSolicitado() == null || solicitud.getHorario() == null) {
            throw new BadRequestException("Datos incompletos.");
        }

        List<AsuntoPropio> historial = repo.findByDocenteId(docente.getId());

        // 1. VALIDAR LÍMITE ANUAL (Máximo 4 días)
        long totalActivas = historial.stream()
                .filter(a -> a.getFechaTramitacion() == null || a.isAprobado())
                .count();

        if (totalActivas >= 4) {
            throw new BadRequestException("Has alcanzado el límite anual de 4 días.");
        }

        // 2. VALIDAR LÍMITE TRIMESTRAL (Máximo 3 días)
        // Solo validamos el trimestre si NO es la cuarta solicitud del año
        if (totalActivas < 3) {
            int trimestreDeLaNueva = getTrimestre(solicitud.getDiaSolicitado());
            long yaEnEsteTrimestre = historial.stream()
                    .filter(a -> (a.getFechaTramitacion() == null || a.isAprobado()) 
                                 && getTrimestre(a.getDiaSolicitado()) == trimestreDeLaNueva)
                    .count();

            if (yaEnEsteTrimestre >= 3) {
                throw new BadRequestException("Límite trimestral alcanzado (máx. 3 días).");
            }
        }

     // 3. VALIDAR DUPLICADOS
        boolean diaRepetido = historial.stream()
                .anyMatch(a -> a.getDiaSolicitado().equals(solicitud.getDiaSolicitado()) 
                               && a.getHorario().getId() == solicitud.getHorario().getId());
        
        if (diaRepetido) {
            throw new ResourceAlreadyExistsException("Ya has solicitado esta hora para el día " + solicitud.getDiaSolicitado());
        }

        // 4. GUARDAR
        solicitud.setDocente(docente);
        solicitud.setAprobado(false);
        solicitud.setFechaTramitacion(null);
        
        AsuntoPropio guardada = repo.save(solicitud);

        // 5. ENVÍO DE EMAIL
        try {
            byte[] excel = excelService.generarExcelAsuntoPropio(docente, guardada,  mat, anot);
            mailService.enviarSolicitudAdmin(
                docente.getNombre() + " " + docente.getApellidos(),
                guardada.getDiaSolicitado().toString(),
                guardada.getDescripcion(),
                excel
            );
        } catch (Exception e) {
            System.err.println("⚠️ Error al enviar email: " + e.getMessage());
        }
    }
    @Transactional // Para que todo ocurra en una sola operación
    public AsuntoPropio aprobarSolicitud(Long id, String material, String anotaciones) {
        AsuntoPropio asunto = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        
        // Marcamos como aprobado
        asunto.setAprobado(true);
        asunto.setFechaTramitacion(LocalDateTime.now()); 
        
        // Pasamos los datos del asunto propio al motor de guardias
        guardiaService.procesarSustitucion(
            asunto.getHorario().getId(), 
            material, 
            anotaciones, 
            asunto.getDiaSolicitado() // La fecha que pidió el docente
        );
        
        return repo.save(asunto);
    }

    @Transactional(readOnly = true)
    public List<AsuntoPropioDTO> listarPorDocente(Long docenteId) {
        return repo.findByDocenteId(docenteId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<AsuntoPropioDTO> listarTodosDTO() {
        return repo.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AsuntoPropioDTO> obtenerSolicitudesPendientesDTO() {
        return repo.findAll().stream()
                .filter(a -> a.getFechaTramitacion() == null)
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * MÉTODO CLAVE: Convierte la entidad al DTO plano que espera el Dashboard.
     */
    private AsuntoPropioDTO convertirADTO(AsuntoPropio ap) {
        // Lógica de Estado
        String estadoDesc = (ap.getFechaTramitacion() == null) ? "PENDIENTE" : (ap.isAprobado() ? "APROBADO" : "RECHAZADO");
        
        String nombreDocente = (ap.getDocente() != null) 
            ? ap.getDocente().getNombre() + " " + ap.getDocente().getApellidos() 
            : "No asignado";

        // Mapeo seguro de campos de Horario y Asignatura
        String aula = "---";
        String hora = "---";
        String asignatura = "Sin asignar";
        String curso = "---";

        if (ap.getHorario() != null) {
            aula = ap.getHorario().getAula();
            hora = String.valueOf(ap.getHorario().getHora());
        if (ap.getHorario().getAsignatura() != null) {
                asignatura = ap.getHorario().getAsignatura().getNombre();
                // Convertimos el int/Integer a String para que el DTO lo acepte
                curso = String.valueOf(ap.getHorario().getAsignatura().getCurso());
            }
        }

        return AsuntoPropioDTO.builder()
                .id(ap.getId())
                .nombreDocente(nombreDocente)
                .diaSolicitado(ap.getDiaSolicitado())
                .descripcion(ap.getDescripcion())
                .estado(estadoDesc)
                .fechaTramitacion(ap.getFechaTramitacion())
                .aula(aula)
                .hora(hora)
                .asignatura(asignatura)
                .curso(curso)           
                .build();
    }

    private int getTrimestre(LocalDate fecha) {
        if (fecha == null) return 0;
        int mes = fecha.getMonthValue();
        if (mes >= 9 && mes <= 12) return 1;
        if (mes >= 1 && mes <= 3)  return 2;
        return 3;
    }
    public AsuntoPropio findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el asunto con ID: " + id));
    }
}