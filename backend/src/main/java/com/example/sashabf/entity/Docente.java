package com.example.sashabf.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "docente")
public class Docente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    private String nombre;
    private String apellidos;
    private String email;
    private String siglas;
    private String tipo;
    private LocalDate antiguedad;
    private int posicion;
    
    @JsonIgnore // El password NUNCA debe viajar al frontend
    private String password;

    @ManyToOne
    @JoinColumn(name="departamento_id")
    private Departamento departamento;
    
    @ManyToOne
    @JoinColumn(name="rol_id")
    private Rol rol;
    
    // Usamos JsonIgnore en las relaciones bidireccionales pesadas 
    // para evitar que el JSON de Docentes entre en bucle
    
    @OneToMany(mappedBy="docente")
    @JsonIgnore 
    private List<Guardia> guardias;
    
    @OneToMany(mappedBy="docente")
    @JsonIgnore
    private List<AsuntoPropio> asuntosPropios;
    
    @OneToMany(mappedBy="docente")
    @JsonIgnore
    private List<Horario> horarios;
}