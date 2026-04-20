package com.example.sashabf.service;

import com.example.sashabf.dto.DocenteDTO;
import com.example.sashabf.dto.DocenteDepartamentoDto;
import com.example.sashabf.entity.Docente;
import com.example.sashabf.exception.BadRequestException;
import com.example.sashabf.exception.PasswordInvalidException;
import com.example.sashabf.exception.ResourceAlreadyExistsException;
import com.example.sashabf.exception.ResourceNotFoundException;
import com.example.sashabf.repository.DocenteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocenteService {

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Busca un docente por email (Útil para el Login/UserDetails).
     */
    public Docente buscarPorEmail(String email) {
        return docenteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No se encontró el docente con email: " + email));
    }

    /**
     * CREAR DOCENTE (Desde el Panel de Admin)
     * Lógica: Si no se define contraseña, se usan las siglas cifradas por defecto.
     */
    @Transactional
    public Docente guardarDocente(Docente docente) {
        // 1. LIMPIEZA PRIMERO (para que la búsqueda funcione)
        if (docente.getEmail() != null) {
            docente.setEmail(docente.getEmail().toLowerCase().trim());
        }

        // 2. VALIDAR DUPLICADOS (Ahora sí, comparando minúsculas con minúsculas)
        if (docenteRepository.findByEmail(docente.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("El email " + docente.getEmail() + " ya está registrado.");
        }

        // 3. LÓGICA DE PASSWORD
        String passwordParaCifrar;
        if (docente.getPassword() != null && !docente.getPassword().trim().isEmpty()) {
            passwordParaCifrar = docente.getPassword();
        } 
        else if (docente.getSiglas() != null && !docente.getSiglas().trim().isEmpty()) {
            passwordParaCifrar = docente.getSiglas();
        } 
        else {
            throw new BadRequestException("Faltan datos para la contraseña.");
        }

        docente.setPassword(passwordEncoder.encode(passwordParaCifrar));
        
        // 4. GUARDAR
        return docenteRepository.save(docente);
    }

    /**
     * ACTUALIZAR DOCENTE (Desde el Panel de Admin)
     */
    @Transactional
    public DocenteDTO actualizar(Long id, DocenteDTO dto) {
        // 1. Verificar existencia
        Docente docenteExistente = docenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el docente con ID: " + id));

        // 2. Backup del password actual
        String passwordEnBD = docenteExistente.getPassword();

        // 3. Mapear cambios EXCLUYENDO el password para que ModelMapper no lo toque
        // Creamos una configuración temporal para esta operación
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        modelMapper.map(dto, docenteExistente);

        // 4. Gestión manual de la contraseña
        String passwordRecibida = dto.getPassword();

        if (passwordRecibida != null && !passwordRecibida.trim().isEmpty()) {
            // Solo encriptamos si NO es ya un hash (las contraseñas planas no empiezan por $2a$)
            if (!passwordRecibida.startsWith("$2a$")) {
                docenteExistente.setPassword(passwordEncoder.encode(passwordRecibida));
            } else {
                // Si lo que recibimos empieza por $2a$, es el hash antiguo.
                // Lo mantenemos tal cual (o usamos el backup).
                docenteExistente.setPassword(passwordEnBD);
            }
        } else {
            // Si viene nulo o vacío, restauramos el backup
            docenteExistente.setPassword(passwordEnBD);
        }

        Docente guardado = docenteRepository.save(docenteExistente);
        return modelMapper.map(guardado, DocenteDTO.class);
    }


    /**
     * ELIMINAR DOCENTE
     */
    public void eliminar(Long id) {
        if (!docenteRepository.existsById(id)) {
            throw new RuntimeException("El docente con ID " + id + " no existe.");
        }
        docenteRepository.deleteById(id);
    }

    // --- MÉTODOS DE LISTADO ---

    public List<Docente> listarTodos() {
        return docenteRepository.findAll();
    }

    public List<DocenteDTO> listarDocentesDTO() {
        return docenteRepository.findAll().stream()
                .map(docente -> modelMapper.map(docente, DocenteDTO.class))
                .collect(Collectors.toList());
    }

    public List<DocenteDepartamentoDto> listarDocentesPorDepartamento(String nombreDep) {
        return docenteRepository.findByDepartamentoNombre(nombreDep).stream()
                .map(docente -> modelMapper.map(docente, DocenteDepartamentoDto.class))
                .collect(Collectors.toList());
    }

    public Docente findById(Long id) {
        return docenteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el docente con ID: " + id));
    }
    @Transactional
    public void cambiarPassword(String email, String oldPassword, String newPassword) {
        // 1. Buscamos al docente
        Docente docente = docenteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con el email: " + email));

        // 2. Verificamos la contraseña actual
        if (!passwordEncoder.matches(oldPassword, docente.getPassword())) {
            throw new PasswordInvalidException("La contraseña actual no es correcta. Inténtalo de nuevo.");
        }

        // 3. NUEVO: Verificar que la nueva no sea igual a la antigua
        if (passwordEncoder.matches(newPassword, docente.getPassword())) {
            throw new PasswordInvalidException("La nueva contraseña no puede ser igual a la anterior. Elige una distinta.");
        }

        // 4. Ciframos y guardamos
        docente.setPassword(passwordEncoder.encode(newPassword));
        docenteRepository.save(docente);
    }
}