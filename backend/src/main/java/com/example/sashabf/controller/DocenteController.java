package com.example.sashabf.controller;

import com.example.sashabf.dto.DocenteDTO;
import com.example.sashabf.dto.DocenteDepartamentoDto;
import com.example.sashabf.dto.PasswordChangeRequest;
import com.example.sashabf.entity.Docente;
import com.example.sashabf.service.DocenteService;

import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/admin/docentes")
public class DocenteController {

    private final DocenteService docenteService;
    private final ModelMapper modelMapper;

    public DocenteController(DocenteService docenteService, ModelMapper modelMapper) {
        this.docenteService = docenteService;
        this.modelMapper = modelMapper;
    }

    // 1. LOGIN: Usuario actual
    
        @GetMapping("/usuario-actual")
        public ResponseEntity<?> obtenerUsuarioActual(Authentication authentication) {
            log.info("Entrando al endpoint /usuario-actual");

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Authentication es nula o no está autenticada");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesión no válida");
            }
            
            try {
                String email = authentication.getName();
                log.info("Buscando datos para el email: {}", email);
                
                Docente docente = docenteService.buscarPorEmail(email);
                
                log.info("Usuario recuperado: {} con Rol: {}", 
                         docente.getNombre(), 
                         docente.getRol() != null ? docente.getRol().getNombre() : "SIN ROL");

                return ResponseEntity.ok(docente);
            } catch (Exception e) {
                log.error("Error al buscar docente: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Docente no encontrado");
            }
        }
    

    // 2. Registro
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Solo el admin puede usar este botón
    public ResponseEntity<Docente> registrarDocente(@RequestBody Docente docente) {
        Docente nuevo = docenteService.guardarDocente(docente);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    // 3. LISTADOS DTO
    @GetMapping("/dto/lista")
    public List<DocenteDTO> listarDocentesDTO() {
        return docenteService.listarDocentesDTO();
    }

    // 4. ELIMINAR
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarDocente(@PathVariable Long id) {
        try {
            docenteService.eliminar(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body("No se puede eliminar: el docente tiene datos asociados.");
        }
    }

    // 5. OBTENER UNO (Para edición)
    @GetMapping("/{id}")
    public ResponseEntity<DocenteDTO> obtenerUno(@PathVariable("id") Long id) {
        Docente docente = docenteService.findById(id);
        DocenteDTO dto = modelMapper.map(docente, DocenteDTO.class);
        return ResponseEntity.ok(dto);
    }

    // 6. ACTUALIZAR (PUT)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DocenteDTO> actualizarDocente(@PathVariable Long id, @RequestBody DocenteDTO docenteDTO) {
        DocenteDTO actualizado = docenteService.actualizar(id, docenteDTO);
        return ResponseEntity.ok(actualizado);
    }
    @PostMapping("/cambiar-password") 
    public ResponseEntity<?> cambiarPassword(@RequestBody PasswordChangeRequest request, Authentication authentication) {
        // 1. Obtenemos el email del contexto de seguridad
        String emailLogueado = authentication.getName(); 
        
        // 2. Llamamos al servicio (si falla, saltará la excepción al GlobalExceptionHandler)
        docenteService.cambiarPassword(
            emailLogueado, 
            request.getOldPassword(), 
            request.getNewPassword()
        );
        
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
}