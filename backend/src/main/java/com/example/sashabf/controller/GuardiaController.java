package com.example.sashabf.controller;

import com.example.sashabf.dto.GuardiaResultadoDTO;
import com.example.sashabf.entity.Guardia;
import com.example.sashabf.service.GuardiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.sashabf.service.ImportacionGuardiasService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/guardias")
@CrossOrigin(origins = "*")
public class GuardiaController {

    @Autowired private ImportacionGuardiasService importacionService;
    @Autowired private GuardiaService guardiaService;

    @PostMapping("/admin/procesar-lote")
    public ResponseEntity<?> procesarLote(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(importacionService.importarAusenciasDesdeExcel(file));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/admin/paginado")
    public ResponseEntity<Page<GuardiaResultadoDTO>> obtenerGuardiasPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Ordenamos por ID de forma descendente para ver las últimas primero
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(guardiaService.listarGuardiasPaginadas(pageable));
    }

    @GetMapping("/docente/{id}")
    public ResponseEntity<List<GuardiaResultadoDTO>> obtenerHistorialDocente(@PathVariable Long id) {
        return ResponseEntity.ok(guardiaService.obtenerHistorialPorDocente(id));
    }

    @PostMapping("/procesar-individual")
    public ResponseEntity<?> procesarSustitucionIndividual(
            @RequestParam Long idHorario,
            @RequestParam(required = false) String material,
            @RequestParam(required = false) String anotacion) {
        
        // Llamamos al servicio (que ya tiene los logs y la lógica de departamentos)
        guardiaService.procesarSustitucion(idHorario, material, anotacion, LocalDate.now());
        
        return ResponseEntity.ok("Guardia generada correctamente");
    }
    @GetMapping("/todas") // <-- ¡REVISA ESTO!
    public ResponseEntity<List<Guardia>> listarTodas() {
        return ResponseEntity.ok(guardiaService.obtenerTodas());
    }

    @GetMapping("/mis-guardias")
    public ResponseEntity<List<GuardiaResultadoDTO>> obtenerMisGuardias(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        
        // 1. Spring sabe quién está logueado gracias al Token
        String email = userDetails.getUsername(); 
        
        // 2. Llamamos al servicio para que busque solo las del docente actual
        // Usamos el DTO para evitar errores de recursión infinita
        return ResponseEntity.ok(guardiaService.obtenerGuardiasPorEmailDocente(email));
    }
 
    @PostMapping
    public ResponseEntity<Guardia> crearGuardia(@RequestBody Guardia guardia) {
        // Llamamos al nuevo método que pusimos en el service
        Guardia guardada = guardiaService.guardar(guardia);
        return new ResponseEntity<>(guardada, org.springframework.http.HttpStatus.CREATED);
    }
}