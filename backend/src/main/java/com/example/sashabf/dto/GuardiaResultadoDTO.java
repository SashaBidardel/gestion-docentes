package com.example.sashabf.dto;

import java.time.LocalDate;
import java.util.List;

import com.example.sashabf.entity.Asignatura;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public class GuardiaResultadoDTO {
	    private Long id;
	    private Long idHorario;
	    private String profesorAusente; // Nombre + Apellidos
	    private int hora;               // El entero (1, 2, 3...)
	    private String asignatura;
	    private String aula;
	    private String sustitutoAsignado; // Nombre + Apellidos
	    private String material;
	    private String anotacion;
	    private Integer conteoGuardias; // Aquí guardaremos la longitud de la lista
	    private List<String> candidatosIgnorados;
	    private int dia;
	    private LocalDate fecha;
	}

