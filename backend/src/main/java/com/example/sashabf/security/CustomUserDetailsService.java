package com.example.sashabf.security;

import com.example.sashabf.entity.Docente;
import com.example.sashabf.repository.DocenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private DocenteRepository docenteRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Intentando autenticar al usuario con email: {}", email);

        Docente docente = docenteRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado en la base de datos: {}", email);
                    return new UsernameNotFoundException("Usuario no encontrado: " + email);
                });

        // 1. Normalizar el Rol
        String nombreRolOriginal = docente.getRol().getNombre(); 
        String nombreRolSpring = nombreRolOriginal.toUpperCase();
        
        if (!nombreRolSpring.startsWith("ROLE_")) {
            nombreRolSpring = "ROLE_" + nombreRolSpring;
        }

        log.debug("🔑 Usuario localizado. Nombre: {} {}, Rol: {}", 
                  docente.getNombre(), docente.getApellidos(), nombreRolSpring);

        // 2. Retornar UserDetails
        // El password que pasamos (docente.getSiglas()) DEBE estar cifrado con BCrypt
        log.info("Usuario encontrado: {}", docente.getEmail());
        log.info("Password almacenado (Hash): {}", docente.getPassword());
     // LOGS DE DIAGNÓSTICO
        log.info("✅ Usuario encontrado: {}", docente.getNombre());
        log.info("🔑 Hash en BD: {}", docente.getPassword());
        log.info("🛡️ Rol procesado: ROLE_{}", docente.getRol().getNombre().toUpperCase());
        return new User(
                docente.getEmail(),
                docente.getPassword(), 
                List.of(new SimpleGrantedAuthority(nombreRolSpring))
        );
    }
}