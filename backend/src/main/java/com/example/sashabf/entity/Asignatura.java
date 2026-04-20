package com.example.sashabf.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@Table(name = "asignatura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Asignatura {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private String nombre;
	private String siglas;
	private int curso;
	
	@ManyToOne
	@JoinColumn(name="ciclo_id")
	private Ciclo ciclo;
	
	@OneToMany(mappedBy="asignatura")
	@JsonIgnore
	private List<Horario> horarios;
	
}
