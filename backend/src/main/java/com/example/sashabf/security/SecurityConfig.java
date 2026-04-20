package com.example.sashabf.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .cors(Customizer.withDefaults())
	        .csrf(csrf -> csrf.disable()) 
	        // 1. IMPORTANTE: Evita conflictos de sesión entre Admin y Docente
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        
	        .authorizeHttpRequests(auth -> auth
	            
	            // --- REGLAS DE ACCESO COMÚN (ADMIN Y DOCENTE) ---
	        	// PERMITIR RUTAS DE SWAGGER Y OPENAPI(por si se usasen para documentación)
	            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
	            // Permitir que ambos vean su perfil al loguearse
	            .requestMatchers("/api/admin/docentes/usuario-actual").authenticated()
	            
	            // Permitir que el docente cambie su propia contraseña (aunque la ruta cuelgue de /admin)
	            .requestMatchers(HttpMethod.POST, "/api/admin/docentes/cambiar-password").authenticated()


	            // --- REGLAS ESPECÍFICAS DE DOCENTE ---
	            
	            .requestMatchers("/api/asuntos-propios/mis-solicitudes").hasRole("DOCENTE")
	            .requestMatchers("/api/asuntos-propios/pendientes").hasAnyRole("DOCENTE", "ADMIN") // Ajusta según quién deba ver esto
	            .requestMatchers("/api/horarios/docente/**").hasRole("DOCENTE")


	            // --- REGLAS EXCLUSIVAS DE ADMINISTRADOR ---
	            
	            // Registro de nuevos docentes
	            .requestMatchers(HttpMethod.POST, "/api/admin/docentes").hasRole("ADMIN")
	            
	            // Gestión de asuntos propios para admin
	            .requestMatchers("/api/asuntos-propios/admin/**").hasRole("ADMIN")
	            
	            // Importación y Guardias
	            .requestMatchers("/api/importar/excel").hasRole("ADMIN")
	            .requestMatchers("/api/admin/guardias/**").hasRole("ADMIN")
	            
	            // Bloqueo general de cualquier otra ruta que empiece por /api/admin/
	            .requestMatchers("/api/admin/**").hasRole("ADMIN")
	            //Reiniciar el sistema
	            .requestMatchers("/api/admin/mantenimiento/**").hasRole("ADMIN")

	            // --- REGLA FINAL ---
	            .anyRequest().authenticated()
	        )
	        .httpBasic(Customizer.withDefaults()); 

	    return http.build();
	}

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        
        // El origen del Frontend (Vite/Vue)
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        
        // 1. AÑADIMOS "Cache-Control" y "X-Requested-With" para mayor compatibilidad
        config.setAllowedHeaders(List.of(
            "Origin", 
            "Content-Type", 
            "Accept", 
            "Authorization", 
            "Cache-Control", 
            "X-Requested-With"
        ));
        
        // 2. MÉTODOS PERMITIDOS
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 3. EXPOSICIÓN DE CABECERAS (Opcional, pero ayuda en algunos navegadores)
        config.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}