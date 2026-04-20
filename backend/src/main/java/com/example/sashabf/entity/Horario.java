package com.example.sashabf.entity;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "horario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Horario {
    
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private int dia;
	private int hora;
	private String aula;
	private String tipo;
	
	@OneToMany(mappedBy = "horario", fetch = FetchType.LAZY)
	@JsonIgnore
	private List<Guardia> guardias;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JsonIgnoreProperties({"horarios", "password", "rol", "departamento"})
	@JoinColumn(name="docente_id")
	private Docente docente;
    
	@ManyToOne(fetch = FetchType.LAZY) // <--- Añade LAZY aquí también
	@JsonIgnoreProperties({"horarios", "docente"}) // Evita que la asignatura intente cargar otros horarios
	@JoinColumn(name="asignatura_id")
	private Asignatura asignatura;
}

