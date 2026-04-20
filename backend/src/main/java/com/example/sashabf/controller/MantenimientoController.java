package com.example.sashabf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sashabf.service.MantenimientoService;

@RestController
@RequestMapping("/api/admin/mantenimiento")
public class MantenimientoController {

    @Autowired private MantenimientoService mantenimientoService;

    @DeleteMapping("/reiniciar-datos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> reiniciarDatos() {
        try {
            mantenimientoService.reiniciarSistema();
            return ResponseEntity.ok("Sistema limpiado con éxito. Solo quedan los usuarios de gestión.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al limpiar: " + e.getMessage());
        }
    }
}
