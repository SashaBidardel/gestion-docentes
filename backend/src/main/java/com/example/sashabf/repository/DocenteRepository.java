package com.example.sashabf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.sashabf.entity.Docente;

import jakarta.transaction.Transactional;

@Repository
public interface DocenteRepository extends JpaRepository <Docente,Long> {

	List<Docente> findAllByOrderByApellidosAsc();
	// Devuelve un Optional para manejar de forma segura si no se encuentra
    Optional<Docente> findByApellidos(String apellidos);
    Optional<Docente> findByEmail(String email);
    Optional<Docente> findBySiglas(String siglas);
 // Para filtrar por departamento (Spring busca en la entidad Departamento el campo 'nombre')
    List<Docente> findByDepartamentoNombre(String nombreDep);
 // En DocenteRepository.java

        // Usamos una consulta JPQL manual para evitar errores de interpretación de nombre
    @Transactional
    @Modifying
    @Query("DELETE FROM Docente d WHERE d.email NOT LIKE %:dominio")
        void borrarDocentesQueNoSeanDelDominio(@Param("dominio") String dominio);
    
}

