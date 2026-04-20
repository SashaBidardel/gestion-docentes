package com.example.sashabf.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.sashabf.entity.Docente;
import com.example.sashabf.entity.Departamento;
import com.example.sashabf.repository.DepartamentoRepository; // Asegúrate de importar tu Repo
import com.example.sashabf.service.DepartamentoService;
import com.example.sashabf.service.DocenteService;

@RestController
@RequestMapping("/api/departamentos")

public class DepartamentoController {

    private final DepartamentoService departamentoService;
    
    @Autowired
    private DepartamentoRepository departamentoRepository; // Añadimos el repo para el listado general
    @Autowired
    private DocenteService docenteService;
    public DepartamentoController(DepartamentoService departamentoService) {
        this.departamentoService = departamentoService;
    }

   
    @GetMapping
    public List<Departamento> listarTodos() {
        return departamentoRepository.findAll();
    }

    @GetMapping("/docentes/{nombre}")
    public List<Docente> docentesNombreDepartamento(@PathVariable String nombre) {
        return departamentoService.obtenerDocentesPorNombre(nombre);
    }

   
    @GetMapping("/api/usuario-actual")
    public ResponseEntity<?> obtenerUsuarioActual(Authentication authentication) {
        // 1. Verificamos si hay una sesión activa
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No estás logueado");
        }

        try {
            // 2. Extraemos el email
            String email = authentication.getName();

            // 3. Llamamos al servicio (que ya devuelve Docente directamente)
            Docente docente = docenteService.buscarPorEmail(email);

            // 4. Devolvemos el objeto docente (JSON)
            return ResponseEntity.ok(docente);

        } catch (RuntimeException e) {
            // Si el servicio no lo encuentra y lanza la excepción, respondemos 404
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
