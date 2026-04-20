package com.example.sashabf.controller;

import com.example.sashabf.entity.Docente;
import com.example.sashabf.service.DocenteService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/docentes")
public class AdminController {

    @Autowired
    private DocenteService docenteService;
 // Listar todos los docentes para la tabla de gestión
    @GetMapping
    public List<Docente> listarTodos() {
        return docenteService.listarTodos();
    }

   

   
}