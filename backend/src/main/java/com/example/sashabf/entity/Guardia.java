package com.example.sashabf.entity;

import java.time.LocalDate;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
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
@Table(name = "guardia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Guardia {
     
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private LocalDate fecha;
	private String anotacion;
	private String material;
	
	@ManyToOne
	//@JsonBackReference
	@JsonIgnoreProperties("guardias")
	@JoinColumn(name="docente_id")
	private Docente docente;
	
	@ManyToOne
	@JoinColumn(name="horario_id")
	@JsonIgnoreProperties("guardias") // Esto evita que el horario vuelva a pintar las guardias
	private Horario horario;
	
	@OneToOne
	@JoinColumn(name = "asunto_propio_id", nullable = true, unique = true)
	private AsuntoPropio asuntoPropio; // Puede ser null si es una guardia por "falta genérica"
	
}
