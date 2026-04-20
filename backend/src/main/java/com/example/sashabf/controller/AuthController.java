package com.example.sashabf.controller;

import com.example.sashabf.entity.Docente;
import com.example.sashabf.repository.DocenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private DocenteRepository docenteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

   
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        // 1. Buscar al docente
        Docente docente = docenteRepository.findByEmail(email)
                .orElse(null);

        // 2. Validar si existe y si la contraseña coincide
        if (docente != null && passwordEncoder.matches(password, docente.getPassword())) {
            
            return ResponseEntity.ok(Map.of(
                "mensaje", "Login correcto",
                "email", docente.getEmail(),
                "rol", docente.getRol() 
            ));
        }

        // 3. Si falla devolvemos 401 
        return ResponseEntity.status(401).body(Map.of("error", "Email o contraseña incorrectos"));
    }
}