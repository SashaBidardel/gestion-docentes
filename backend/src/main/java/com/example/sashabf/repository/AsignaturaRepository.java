package com.example.sashabf.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sashabf.entity.Asignatura;
import com.example.sashabf.entity.Docente;

@Repository
public interface AsignaturaRepository extends JpaRepository <Asignatura,Long> {

	Optional<Asignatura> findBySiglas(String siglasExcel);

	

}
