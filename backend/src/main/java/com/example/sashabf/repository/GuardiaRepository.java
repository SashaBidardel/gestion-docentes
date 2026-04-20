package com.example.sashabf.repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.sashabf.entity.Guardia;
import com.example.sashabf.entity.Horario;

@Repository
public interface GuardiaRepository extends JpaRepository <Guardia,Long> {

	
	List<Guardia> findByDocente_Id(Long docenteId);

	boolean existsByAsuntoPropioId(Long asuntoPropioId);
	Optional<Guardia> findByHorarioIdAndFecha(Long horarioId, java.sql.Date fecha);
	List<Guardia> findAllByOrderByIdDesc();
	Optional<Guardia> findByHorarioIdAndFecha(Long horarioId, LocalDate fecha);
	List<Guardia> findByDocenteEmail(String email);
	long countByDocenteId(Long docenteId);
	void deleteByHorarioIdAndFecha(Long horarioId, LocalDate fecha);
	@Modifying
	@Query("DELETE FROM Guardia g WHERE g.horario.id = :idHorario AND g.fecha = :fecha")
	void borrarPorHorarioYFecha(@Param("idHorario") Long idHorario, @Param("fecha") LocalDate fecha);
	@Modifying
    @Query("DELETE FROM Guardia g WHERE g.horario.id = :horarioId AND g.fecha = :fecha")
    void borrarGuardiaExistente(@Param("horarioId") Long horarioId, @Param("fecha") LocalDate fecha);
	boolean existsByHorarioIdAndFecha(Long horarioId, LocalDate fecha);
}
