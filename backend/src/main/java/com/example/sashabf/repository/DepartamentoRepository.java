package com.example.sashabf.repository;



import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sashabf.entity.Departamento;
import com.example.sashabf.entity.Docente;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Long> {

   
    Optional<Departamento> findByNombre(String nombreDepartamento);

   
    Optional<Departamento> findByCodigo(String codigo);
}
