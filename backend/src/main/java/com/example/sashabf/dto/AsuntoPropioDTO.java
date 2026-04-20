package com.example.sashabf.dto;

import lombok.*;
import java.time.LocalDate; // <-- CAMBIO CLAVE
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsuntoPropioDTO {
    private Long id;
    private String nombreDocente;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate diaSolicitado; 
    
    private String descripcion;
    private String estado;
    private LocalDateTime fechaTramitacion;
    private String aula;
    private String hora;
    private String asignatura;
    private String curso;
}
