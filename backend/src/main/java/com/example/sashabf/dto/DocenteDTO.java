package com.example.sashabf.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class DocenteDTO {
    private String nombre;
    private String apellidos;
    private String email;
    private String siglas; // Esta será su contraseña inicial
    private String Password;
    private Long idDepartamento;
    private String tipo;
    private LocalDate antiguedad;
    private int posicion;
    private Long idRol;
}