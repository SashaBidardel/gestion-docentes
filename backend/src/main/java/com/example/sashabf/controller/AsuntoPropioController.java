package com.example.sashabf.controller;

import com.example.sashabf.dto.AsuntoPropioDTO;
import com.example.sashabf.entity.AsuntoPropio;
import com.example.sashabf.entity.Docente;
import com.example.sashabf.entity.Horario;
import com.example.sashabf.service.AsuntoPropioService;
import com.example.sashabf.service.DocenteService;
import com.example.sashabf.service.GuardiaService;
import com.example.sashabf.service.HorarioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/asuntos-propios")
@CrossOrigin(origins = "http://localhost:5173")
public class AsuntoPropioController {

    @Autowired
    private AsuntoPropioService asuntoService;

    @Autowired
    private DocenteService docenteService;
    
    @Autowired
    private GuardiaService guardiaService;
    @Autowired
    private HorarioService horarioService;
    @PostMapping("/solicitar")
    public ResponseEntity<?> solicitarDia(@RequestBody Map<String, Object> payload, Authentication authentication) {
        Map<String, Object> respuesta = new HashMap<>();
        String materialDocente = (String) payload.get("material"); // Extraemos del JSON de Vue
        String anotacionesDocente = (String) payload.get("anotaciones"); // Extraemos del JSON de Vue
        try {
            // 1. Obtener el docente autenticado
            Docente docente = docenteService.buscarPorEmail(authentication.getName());
            
            // 2. Extraer datos del JSON enviado por Vue
            String fechaRaw = (String) payload.get("diaSolicitado");
            String motivo = (String) payload.get("descripcion"); // Vue envía 'descripcion'
            
            // --- LA CLAVE: Recuperar el idHorario ---
            Object idHorarioObj = payload.get("idHorario");
            if (idHorarioObj == null) {
                throw new RuntimeException("Debe seleccionar una clase (idHorario)");
            }
            Long idHorario = Long.valueOf(idHorarioObj.toString());

            // 3. Buscar el objeto Horario real en la base de datos
            // Asegúrate de tener este método en tu HorarioService
            Horario horario = horarioService.buscarPorId(idHorario); 

            // 4. Configurar la nueva solicitud
            AsuntoPropio nuevaSolicitud = new AsuntoPropio();
            nuevaSolicitud.setDiaSolicitado(LocalDate.parse(fechaRaw));
            nuevaSolicitud.setDescripcion(motivo);
            nuevaSolicitud.setAprobado(false);
            
            // VINCULAMOS LOS OBJETOS (Esto quita los NULL de la DB)
            nuevaSolicitud.setHorario(horario); 
            nuevaSolicitud.setDocente(docente);

            // 5. Guardar
            asuntoService.solicitarAsuntoPropio(nuevaSolicitud, docente, materialDocente, anotacionesDocente);

            respuesta.put("status", "success");
            respuesta.put("message", "Solicitud registrada correctamente");
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            respuesta.put("status", "error");
            respuesta.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }
    @GetMapping("/mis-solicitudes")
    public ResponseEntity<?> listarMisSolicitudes(Authentication authentication) {
        try {
            Docente docente = docenteService.buscarPorEmail(authentication.getName());
            return ResponseEntity.ok(asuntoService.listarPorDocente(docente.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(value = "/admin/aprobar/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> aprobarAsunto(
        @PathVariable Long id,
        @RequestParam("archivo") MultipartFile archivo, 
        @RequestParam(value = "material", required = false) String material,
        @RequestParam(value = "anotaciones", required = false) String anotaciones
    ) {
        try {
        	// 1. Buscamos el asunto ANTES de aprobarlo para tener los datos
            AsuntoPropio asunto = asuntoService.findById(id); 
            // 2. Ejecutamos  lógica de aprobación
            asuntoService.aprobarSolicitud(id, material, anotaciones);
            // 2.Pasamos el ID del horario, el material/anotaciones del admin y la fecha del asunto
            guardiaService.procesarSustitucion(
                asunto.getHorario().getId(), 
                material, 
                anotaciones, 
                asunto.getDiaSolicitado()
            );

            return ResponseEntity.ok().body("Solicitud aprobada y sustituto asignado por carga de trabajo");
        } catch (Exception e) {
            e.printStackTrace(); // Para que ver el error real en la consola
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<AsuntoPropioDTO>> listarPendientes() {
        return ResponseEntity.ok(asuntoService.obtenerSolicitudesPendientesDTO());
    }

    @GetMapping("/admin/listar") 
    public ResponseEntity<List<AsuntoPropioDTO>> listarParaAdmin() {
        return ResponseEntity.ok(asuntoService.listarTodosDTO());
    }
    
}