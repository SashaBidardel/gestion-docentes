package com.example.sashabf.controller;

import com.example.sashabf.entity.Docente;
import com.example.sashabf.entity.Horario;
import com.example.sashabf.service.DocenteService; // Importación necesaria
import com.example.sashabf.service.HorarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // Importación necesaria para Authentication
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
@CrossOrigin(origins = "http://localhost:5173")
public class HorarioController {

    private final HorarioService horarioService;
    private final DocenteService docenteService; // Añadimos el servicio de docentes

    // Inyectamos ambos servicios en el constructor
    public HorarioController(HorarioService horarioService, DocenteService docenteService) {
        this.horarioService = horarioService;
        this.docenteService = docenteService;
    }

    /**
     * Endpoint para que el Frontend (Vue) obtenga SOLO los horarios 
     * del docente que ha iniciado sesión.
     * URL: GET http://localhost:8080/api/horarios/mis-horarios
     */
    @GetMapping("/mis-horarios")
    public ResponseEntity<List<Horario>> listarMisHorarios(Authentication authentication) {
        try {
            // 1. Obtenemos el email directamente del token de seguridad
            String email = authentication.getName(); 
            
            // 2. Buscamos el objeto Docente completo
            Docente docente = docenteService.buscarPorEmail(email); 
            
            if (docente == null) {
                return ResponseEntity.status(404).build();
            }

            // 3. Devolvemos sus horarios específicos (los 20 que tiene Fernando)
            List<Horario> horarios = horarioService.buscarPorDocente(docente.getId());
            
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Mantenemos este por si el Admin necesita consultar horarios de otros,
     * pero para el formulario de solicitud, usaremos el de arriba.
     */
    @GetMapping("/docente/{id}")
    public ResponseEntity<List<Horario>> listarPorDocente(@PathVariable Long id) {
        List<Horario> horarios = horarioService.buscarPorDocente(id);
        
        if (horarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(horarios);
    }
}
