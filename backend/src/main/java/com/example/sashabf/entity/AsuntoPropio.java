package com.example.sashabf.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "asuntos_propios")
public class AsuntoPropio {
  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @JsonFormat(pattern = "yyyy-MM-dd") // Asegura que el JSON sea "2026-10-09"
    private LocalDate diaSolicitado;
    
    private String descripcion;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaTramitacion;
    
    private boolean aprobado;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"asuntosPropios", "password", "horarios", "rol", "departamento", "guardias"})
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @ManyToOne
    @JoinColumn(name = "horario_id")
    @JsonIgnoreProperties({"docente", "asignatura", "asuntosPropios"})
    private Horario horario;
     
    @OneToOne(mappedBy = "asuntoPropio")
    @JsonIgnore // para evitar el bucle con Guardia
    private Guardia guardia;
}
