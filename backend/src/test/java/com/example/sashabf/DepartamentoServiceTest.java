package com.example.sashabf;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sashabf.entity.Departamento;
import com.example.sashabf.repository.DepartamentoRepository;
import com.example.sashabf.service.DepartamentoService;

@ExtendWith(MockitoExtension.class) // <-- Esto le dice a JUnit que use Mockito
public class DepartamentoServiceTest {

    @Mock
    private DepartamentoRepository departamentoRepository; // El falso

    @InjectMocks
    private DepartamentoService departamentoService; // El real que usa al falso

    @Test
    void testGuardarDepartamento() {
        // GIVEN: Preparamos un objeto para la prueba
        Departamento depto = new Departamento("Informática", "INF", "985");
        
        // Configuramos el Mock: Cuando el repositorio guarde algo, que devuelva ese depto
        when(departamentoRepository.save(any(Departamento.class))).thenReturn(depto);

        // WHEN: Ejecutamos la acción del servicio
        Departamento guardado = departamentoService.guardar(depto);

        // THEN: Verificamos que no sea nulo y que el nombre coincida
        assertNotNull(guardado);
        assertEquals("Informática", guardado.getNombre());
        
        // Verificamos que se llamó al método save del repositorio 1 vez
        verify(departamentoRepository, times(1)).save(depto);
    }
}
