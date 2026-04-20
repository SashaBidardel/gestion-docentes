package com.example.sashabf;

import com.example.sashabf.entity.Docente;
import com.example.sashabf.entity.Guardia;
import com.example.sashabf.repository.DocenteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Limpia la base de datos después de cada test
public class GuardiaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocenteRepository docenteRepository;

    private Docente docenteGuardado;

    @BeforeEach
    void setUp() {
        // Creamos un docente real para que la Guardia tenga a quién referenciar
        Docente docente = new Docente();
        docente.setNombre("Sasha");
        docente.setApellidos("Pruebas");
        docente.setEmail("sasha.test@centro.com");
        docenteGuardado = docenteRepository.save(docente);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void debeCrearUnaGuardiaExitosamente() throws Exception {
        // 1. Preparamos el objeto Guardia 
        Guardia nuevaGuardia = new Guardia();
        nuevaGuardia.setFecha(LocalDate.now());
        nuevaGuardia.setAnotacion("Guardia de patio sector norte");
        nuevaGuardia.setMaterial("Silbato y chaleco");
        nuevaGuardia.setDocente(docenteGuardado); // FK correcta

        // 2. Ejecutamos la petición POST a /api/guardias
        mockMvc.perform(post("/api/guardias")
                .with(csrf()) // Necesario por Spring Security
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaGuardia)))
                
                // 3. Verificaciones (Asserts)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.anotacion").value("Guardia de patio sector norte"))
                .andExpect(jsonPath("$.material").value("Silbato y chaleco"))
                .andExpect(jsonPath("$.docente.nombre").value("Sasha"));
    }
}
