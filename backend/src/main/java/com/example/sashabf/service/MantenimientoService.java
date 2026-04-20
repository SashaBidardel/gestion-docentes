package com.example.sashabf.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.sashabf.repository.AsignaturaRepository;
import com.example.sashabf.repository.AsuntoPropioRepository;
import com.example.sashabf.repository.DocenteRepository;
import com.example.sashabf.repository.GuardiaRepository;
import com.example.sashabf.repository.HorarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class MantenimientoService {

    @Autowired private GuardiaRepository guardiaRepository;
    @Autowired private AsuntoPropioRepository asuntoRepo;
    @Autowired private HorarioRepository horarioRepository;
    @Autowired private AsignaturaRepository asignaturaRepository;
    @Autowired private DocenteRepository docenteRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void reiniciarSistema() {
        System.out.println("--- Iniciando Purga de Datos (Nombres Corregidos) ---");
        
        // Desactivar FK para evitar el error 1451
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        try {
            // 1. Limpiamos las tablas de movimientos y detalles
            // Usamos los nombres que hemos confirmado
            entityManager.createNativeQuery("TRUNCATE TABLE guardia").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE horario").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE asuntos_propios").executeUpdate();
            
            // Intentamos asignatura por si existe
            try {
                entityManager.createNativeQuery("TRUNCATE TABLE asignatura").executeUpdate();
            } catch (Exception e) {
                System.out.println("Info: Tabla 'asignatura' no encontrada, ignorando...");
            }

            // 2. Limpiar docentes (excepto administración)
            int borrados = entityManager.createNativeQuery(
                "DELETE FROM docente WHERE email NOT LIKE '%@sashabf.com'"
            ).executeUpdate();

            System.out.println("✅ Purga completada. Docentes eliminados: " + borrados);

        } catch (Exception e) {
            System.err.println("❌ Error durante la ejecución: " + e.getMessage());
        } finally {
            // IMPORTANTÍSIMO: Reactivar siempre
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            System.out.println("--- Restricciones reactivadas ---");
        }
    }
}
