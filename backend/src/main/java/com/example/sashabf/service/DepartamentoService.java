package com.example.sashabf.service;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.sashabf.entity.Departamento;
import com.example.sashabf.entity.Docente;
import com.example.sashabf.repository.DepartamentoRepository;

import jakarta.transaction.Transactional;

@Service
public class DepartamentoService {

    @Autowired
    private DepartamentoRepository deptoRepo;

    /**
     * Obtiene la lista de docentes de un departamento específico usando su código (ej: "IFC").
     */
    public List<Docente> obtenerDocentesPorNombre(String nombre) {
        return deptoRepo.findByNombre(nombre)
                .map(Departamento::getDocentes) // Accede a la lista de la entidad
                .orElse(Collections.emptyList());
    }

    /**
     * Lista todos los departamentos disponibles.
     * Útil para rellenar desplegables (Select) en los formularios de Vue.
     */
    public List<Departamento> listarTodos() {
        return deptoRepo.findAll();
    }

    /**
     * Busca un departamento por su ID.
     */
    public Departamento buscarPorId(Long id) {
        return deptoRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado con ID: " + id));
    }

    /**
     * Guarda o crea un nuevo departamento.
     */
    @Transactional
    public Departamento guardar(Departamento depto) {
        return deptoRepo.save(depto);
    }

    /**
     * Elimina un departamento por ID.
     */
    @Transactional
    public void eliminar(Long id) {
        if (!deptoRepo.existsById(id)) {
            throw new RuntimeException("No se puede eliminar: El departamento no existe.");
        }
        deptoRepo.deleteById(id);
    }

    /**
     * Busca un departamento por su nombre exacto.
     */
    public Departamento buscarPorNombre(String nombre) {
        return deptoRepo.findByNombre(nombre)
                .orElseThrow(() -> new RuntimeException("No se encontró el departamento: " + nombre));
    }
}