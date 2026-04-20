package com.example.sashabf.service;

import com.example.sashabf.entity.Horario;
import com.example.sashabf.repository.HorarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class HorarioService {

    @Autowired
    private HorarioRepository horarioRepository;

    /**
     * Busca todos los horarios asociados a un docente por su ID
     */
    public List<Horario> buscarPorDocente(Long docenteId) {
        return horarioRepository.findByDocenteId(docenteId);
    }
    public Horario buscarPorId(Long id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el horario con ID: " + id));
    }
}