package com.example.sashabf.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ciclo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ciclo {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private String nombre;
	private String familia;
	private String codigo;
	private String turno;
	
	@OneToMany(mappedBy="ciclo",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
	@JsonIgnore
	private List<Asignatura> asignatura;
	

}
