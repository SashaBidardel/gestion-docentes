package com.example.sashabf.repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.sashabf.entity.AsuntoPropio;
import com.example.sashabf.entity.Docente;

@Repository
public interface AsuntoPropioRepository extends JpaRepository<AsuntoPropio, Long> {

    // 1. Listados para el historial del docente
    List<AsuntoPropio> findByDocenteIdAndAprobadoTrue(Long docenteId);
    List<AsuntoPropio> findByDocenteIdAndAprobadoFalse(Long docenteId);
    List<AsuntoPropio> findByDocenteId(Long docenteId);

    // 2. REGLA TRIMESTRAL: Cuenta días de UN docente en un rango
    long countByDocenteAndDiaSolicitadoBetween(Docente docente, Date inicio, Date fin);

    // 3. REGLA DIARIA: Cuenta cuántas personas han pedido EL MISMO DÍA
    // Corregido: 'diaSolicitado' en lugar de 'fecha'
    long countByDiaSolicitado(Date diaSolicitado);

    // 4. Utilidades extras 
    // para que el Service pueda preguntar si ya existe la solicitud
    boolean existsByDocenteIdAndDiaSolicitado(Long docenteId, LocalDate diaSolicitado);
    List<AsuntoPropio> findByDocenteIdAndDiaSolicitado(Long docenteId, Date fecha);
	List<AsuntoPropio> findByAprobadoFalse();
	List<AsuntoPropio> findByFechaTramitacionIsNull();
	@Query("SELECT a FROM AsuntoPropio a JOIN FETCH a.docente WHERE a.fechaTramitacion IS NULL")
	List<AsuntoPropio> findPendientesConDocente();
	

    //Query explícita para evitar errores de interpretación de Spring Data
    @Query("SELECT COUNT(a) FROM AsuntoPropio a WHERE a.docente.id = :docenteId AND a.aprobado = true")
    long countAprobadasByDocenteId(@Param("docenteId") Long docenteId);
    @Query("SELECT a FROM AsuntoPropio a JOIN FETCH a.docente LEFT JOIN FETCH a.horario")
    List<AsuntoPropio> findAllWithDocente();

    @Query("SELECT a FROM AsuntoPropio a WHERE a.docente.id = :docenteId AND a.aprobado = true")
    List<AsuntoPropio> findAprobadasByDocenteId(@Param("docenteId") Long docenteId);
    @Query("SELECT a FROM AsuntoPropio a JOIN FETCH a.docente WHERE a.aprobado = false")
    List<AsuntoPropio> findAllPendientesConDocente();
    @Query("SELECT a FROM AsuntoPropio a " +
            "LEFT JOIN FETCH a.docente " +
            "LEFT JOIN FETCH a.horario " +
            "WHERE a.aprobado = false")
     List<AsuntoPropio> findAllPendientesConRelaciones();
    @Query("SELECT DISTINCT a FROM AsuntoPropio a " +
    	       "LEFT JOIN FETCH a.docente d " +
    	       "LEFT JOIN FETCH a.horario h " +
    	       "WHERE d.id = :docenteId")
    	List<AsuntoPropio> findByDocenteIdConRelaciones(@Param("docenteId") Long docenteId);
}

