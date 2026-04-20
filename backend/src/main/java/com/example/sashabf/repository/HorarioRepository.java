package com.example.sashabf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.sashabf.entity.Docente;
import com.example.sashabf.entity.Horario;

@Repository
public interface HorarioRepository extends JpaRepository <Horario,Long> {
	@Query("SELECT h.docente FROM Horario h " +
		       "LEFT JOIN Guardia g ON g.docente.id = h.docente.id " + 
		       "WHERE h.dia = :dia " +
		       "AND h.hora = :hora " +
		       "AND h.tipo = 'GUARDIA' " +
		       "AND h.docente.departamento.id = :deptoId " +
		       "GROUP BY h.docente.id " +
		       "ORDER BY COUNT(g.id) ASC") // El que menos registros tenga en la tabla Guardia va primero
		List<Docente> findSustitutoMasJusto(
		    @Param("dia") int dia, 
		    @Param("hora") int hora, 
		    @Param("deptoId") Long deptoId
		);

	List<Horario> findByDocenteId(Long docenteId);
	

}
