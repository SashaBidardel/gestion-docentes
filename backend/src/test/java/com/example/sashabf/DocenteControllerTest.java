package com.example.sashabf;

import com.example.sashabf.entity.Docente;
import com.example.sashabf.entity.Departamento;
import com.example.sashabf.entity.Rol;
import com.example.sashabf.repository.DepartamentoRepository;
import com.example.sashabf.repository.RolRepository;
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
public class DocenteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private RolRepository rolRepository;

    private Departamento departamentoGuardado;
    private Rol rolGuardado;

    @BeforeEach
    void setUp() {
        // 1. Preparamos el terreno: Creamos un departamento real en la DB de pruebas
        Departamento dept = new Departamento();
        dept.setNombre("Informática");
        departamentoGuardado = departamentoRepository.save(dept);

        // 2. Preparamos un rol real
        Rol rol = new Rol();
        rol.setNombre("ROLE_DOCENTE");
        rolGuardado = rolRepository.save(rol);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void debeCrearUnDocenteConRelacionesExitosamente() throws Exception {
        // 3. Creamos el docente asociando los objetos que acabamos de guardar
        Docente nuevoDocente = new Docente();
        nuevoDocente.setNombre("Lidia");
        nuevoDocente.setApellidos("Test");
        nuevoDocente.setEmail("lidia.integracion@centro.com");
        nuevoDocente.setSiglas("LID");
        nuevoDocente.setPassword("secret123");
        nuevoDocente.setAntiguedad(LocalDate.now());
        nuevoDocente.setPosicion(1);
        
        // Asignamos las entidades persistidas
        nuevoDocente.setDepartamento(departamentoGuardado);
        nuevoDocente.setRol(rolGuardado);

        // 4. Ejecutamos la petición POST a la ruta de ADMIN
        mockMvc.perform(post("/api/admin/docentes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoDocente)))
                
                // 5. Verificaciones
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Lidia"))
                .andExpect(jsonPath("$.email").value("lidia.integracion@centro.com"))
                // Verificamos que el departamento se ha mapeado correctamente en la respuesta
                .andExpect(jsonPath("$.departamento.nombre").value("Informática"))
                .andExpect(jsonPath("$.rol.nombre").value("ROLE_DOCENTE"));
    }
}