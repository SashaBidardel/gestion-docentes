package com.example.sashabf.security;



import com.example.sashabf.entity.Ciclo;
import com.example.sashabf.entity.Departamento;
import com.example.sashabf.entity.Docente;
import com.example.sashabf.entity.Rol;
import com.example.sashabf.repository.CicloRepository;
import com.example.sashabf.repository.DepartamentoRepository;
import com.example.sashabf.repository.DocenteRepository;
import com.example.sashabf.repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final DepartamentoRepository depRepo;
    private final DocenteRepository docenteRepo;
    private final RolRepository rolRepo;
    private final PasswordEncoder passwordEncoder;
    private final CicloRepository cicloRepo;

    public DataInitializer(DepartamentoRepository depRepo, DocenteRepository docenteRepo, 
                           RolRepository rolRepo, PasswordEncoder passwordEncoder,CicloRepository cicloRepo) {
        this.depRepo = depRepo;
        this.docenteRepo = docenteRepo;
        this.rolRepo = rolRepo;
        this.passwordEncoder = passwordEncoder;
        this.cicloRepo= cicloRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        
        // 1. Cargar Departamentos si la tabla está vacía
        if (depRepo.count() == 0) {
            depRepo.saveAll(List.of(
                new Departamento( "Informática y Comunicaciones", "IFC", "984100101"),
                new Departamento( "Electricidad y Electrónica", "ELE", "984100102"),
                new Departamento( "Fabricación Mecánica", "FME", "984100103"),
                new Departamento( "Instalación y Mantenimiento", "IMA", "984100104"),
                new Departamento( "Química", "QUI", "984100105"),
                new Departamento( "Administración y Gestión", "ADG", "984100109"),
                new Departamento( "Comercio y Marketing", "COM", "984100110"),
                new Departamento( "Edificación y Obra Civil", "EOC", "984100113")
            ));
            System.out.println(">> Departamentos inicializados.");
        }

        // 2. Crear Roles básicos 
        if (rolRepo.count() == 0) {
            rolRepo.save(new Rol("ADMIN"));
            rolRepo.save(new Rol("DOCENTE"));
        }

        // 3. Crear Usuarios de prueba (Admin y Docente)
        if (docenteRepo.count() == 0) {
            Departamento ifc = depRepo.findAll().get(0); // Asignamos el primero por defecto
            Rol adminRol = rolRepo.findAll().stream().filter(r -> r.getNombre().equals("ADMIN")).findFirst().get();
            Rol userRol = rolRepo.findAll().stream().filter(r -> r.getNombre().equals("DOCENTE")).findFirst().get();

            // Usuario ADMIN
            Docente admin = new Docente();
            admin.setNombre("Admin");
            admin.setApellidos("Sistema");
            admin.setEmail("admin@sashabf.com");
            admin.setPassword(passwordEncoder.encode("1234")); // Siglas actúan como password
            admin.setRol(adminRol);
            admin.setDepartamento(ifc);
            admin.setSiglas("ADM");
            admin.setPosicion(1);
            admin.setTipo("CARRERA"); 
            admin.setAntiguedad(LocalDate.of(2010, 9, 1)); // Fecha fija del pasado
            docenteRepo.save(admin);
            
            // Usuario DOCENTE
            Docente profe = new Docente();
            profe.setNombre("Juan");
            profe.setApellidos("Docente");
            profe.setEmail("juan@sashabf.com");
            profe.setPassword(passwordEncoder.encode("1234"));
            profe.setRol(userRol);
            profe.setDepartamento(ifc);
            profe.setSiglas("USR");
            profe.setPosicion(2);
            profe.setTipo("INTERINO");
            profe.setAntiguedad(LocalDate.now()); // Empezó hoy mismo
            docenteRepo.save(profe);

            System.out.println(">> Usuarios iniciales creados (pass: 1234).");
        }
        if (cicloRepo.count() == 0) {
            // Orden: Nombre, Familia (Siglas), Código, Turno
            
            // ADMINISTRACIÓN Y GESTIÓN (ADG)
            crearCiclo("Administración y Finanzas", "ADG", "201", "Mañana");
            crearCiclo("Asistencia a la Dirección", "ADG", "301", "Mañana");

            // INFORMÁTICA Y COMUNICACIONES (IFC)
            crearCiclo("Desarrollo de Aplicaciones Web", "IFC", "201", "Mañana");
            crearCiclo("Administración de Sistemas Informáticos en Red", "IFC", "301", "Tarde");
            crearCiclo("Desarrollo de Aplicaciones Multiplataforma", "IFC", "302", "Mañana");
            crearCiclo("Sistemas informaticos", "IFC", "303", "Mañana");
            crearCiclo("Ciberseguridad", "IFC", "401", "Tarde");
            // ELECTRICIDAD Y ELECTRÓNICA (ELE)
            crearCiclo("Sistemas Electrotécnicos y Automatizados", "ELE", "301", "Mañana");
            crearCiclo("Mantenimiento Electrónico", "ELE", "202", "Mañana");
            crearCiclo("Instalaciones electricas", "ELE", "203", "Mañana");
            crearCiclo("Instalaciones electricas de alta tension", "ELE", "302", "Mañana");
            crearCiclo("Electronica de computadores", "ELE", "304", "tarde");
            crearCiclo("Instalaciones frigorificas", "ELE", "402", "Mañana");
            // FABRICACIÓN MECÁNICA (FME)
            crearCiclo("Programación de la Producción", "FME", "202", "Mañana");
            crearCiclo("Mantenimiento de gruas", "FME", "203", "Mañana");
            crearCiclo("Automatizacion de procesos", "FME", "301", "Mañana");

           // FAMILIA: IMAGEN PERSONAL (IMP) 
            crearCiclo("Estética y Belleza", "IMP", "101", "Mañana");
            
            // FAMILIA: TRANSPORTE Y MANTENIMIENTO DE VEHÍCULOS (TMV) 
            crearCiclo("Guía en el Medio Natural y de Tiempo Libre", "TMV", "101", "Tarde");
        }
    }

    private void crearCiclo(String nombre, String familia, String codigo, String turno) {
        Ciclo c = new Ciclo();
        c.setNombre(nombre);    
        c.setFamilia(familia);  
        c.setCodigo(codigo);    
        c.setTurno(turno);      
        cicloRepo.save(c);
    }
        
    }
    

