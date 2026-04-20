package com.example.sashabf.controller;

import com.example.sashabf.service.ImportadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/importar")
@CrossOrigin(origins = "*")
public class ImportadorController {

    @Autowired
    private ImportadorService importadorService;

    // Solo usuarios con rol ADMIN pueden entrar aquí
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/excel")
    public ResponseEntity<?> subirExcel(@RequestParam("file") MultipartFile file) {
        try {
            importadorService.importarDesdeExcel(file);
            return ResponseEntity.ok(Map.of("message", "Importación exitosa"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}