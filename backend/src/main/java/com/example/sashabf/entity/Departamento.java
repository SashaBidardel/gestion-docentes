package com.example.sashabf.entity;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "departamento")
public class Departamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nombre;
    private String codigo;
    private String telefono;

    @OneToMany(mappedBy = "departamento")
    @JsonIgnore //  para que el JSON no entre en bucle infinito
    private List<Docente> docentes;

    public Departamento(String nombre, String codigo, String telefono) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.telefono = telefono;
    }
}