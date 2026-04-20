package com.example.sashabf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sashabf.entity.Ciclo;

@Repository
public interface CicloRepository extends JpaRepository <Ciclo,Long> {

	
	Optional<Ciclo> findById(long id);
	//Ahora devuelve una lista en lugar de un Optional
    List<Ciclo> findAllByCodigo(String codigo);

}
