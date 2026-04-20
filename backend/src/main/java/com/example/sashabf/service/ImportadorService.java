package com.example.sashabf.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.text.Normalizer;
import java.util.stream.Collectors;

import com.example.sashabf.entity.*;
import com.example.sashabf.repository.*;

@Service
public class ImportadorService {

    private static final Logger log = LoggerFactory.getLogger(ImportadorService.class);

    @Autowired private DocenteRepository docenteRepo;
    @Autowired private DepartamentoRepository deptoRepo;
    @Autowired private AsignaturaRepository asignaturaRepo;
    @Autowired private HorarioRepository horarioRepo;
    @Autowired private RolRepository rolRepo;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CicloRepository cicloRepo;
    
    // Inyectamos el servicio de guardias para procesar las sustituciones al vuelo
    @Autowired private GuardiaService guardiaService; 

    @Transactional
    public void importarDesdeExcel(MultipartFile file) throws Exception {
    	
    	DataFormatter formatter = new DataFormatter();
        List<Departamento> todosLosDeptos = deptoRepo.findAll();
        Random random = new Random();

        log.info("🚀 Iniciando importación masiva y generación de guardias...");

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Saltar cabecera

                // 1. FILTRO DE TIPO (Columna J)
                String tipoExcel = obtenerValor(row.getCell(9), evaluator, formatter).toUpperCase().trim();
                if (!tipoExcel.equals("G") && !tipoExcel.equals("LEC")) continue;

                // 2. EXTRAER VALORES
                String siglasDocente = obtenerValor(row.getCell(0), evaluator, formatter).trim(); 
                String nombre = obtenerValor(row.getCell(1), evaluator, formatter).trim();        
                String apellidos = obtenerValor(row.getCell(2), evaluator, formatter).trim();     
                String tipoDocente = obtenerValor(row.getCell(3), evaluator, formatter);    
                String posicionStr = obtenerValor(row.getCell(4), evaluator, formatter);  
                String antiguedadStr = obtenerValor(row.getCell(5), evaluator, formatter); 
                String aulaExcel = obtenerValor(row.getCell(8), evaluator, formatter);     
                String siglasModulo = obtenerValor(row.getCell(10), evaluator, formatter); 
                String cursoStr = obtenerValor(row.getCell(11), evaluator, formatter);
                
                String cicloRaw = obtenerValor(row.getCell(13), evaluator, formatter).trim();
                String codigoCicloLimpio = cicloRaw.replaceAll("[a-zA-Z]", "").split("\\.")[0].trim();
                
                if (siglasDocente.isEmpty()) continue;

                // 3. PROCESAR DOCENTE
                String email = normalizarTexto(nombre) + normalizarTexto(apellidos) + "@centro.es";
                Optional<Docente> docenteOpt = docenteRepo.findByEmail(email);
                Docente docente;
                
                if (docenteOpt.isEmpty()) {
                    docente = new Docente();
                    docente.setEmail(email);
                    rolRepo.findById(2L).ifPresent(docente::setRol);
                    docente.setSiglas(siglasDocente); 
                    docente.setPassword(passwordEncoder.encode(siglasDocente));
                } else {
                    docente = docenteOpt.get();
                }
                
                docente.setNombre(nombre);
                docente.setApellidos(apellidos);
                docente.setTipo(tipoDocente);

                try {
                    docente.setPosicion(Integer.parseInt(posicionStr.replaceAll("[^0-9]", "")));
                } catch (Exception e) { docente.setPosicion(0); }

                if (!antiguedadStr.isEmpty()) {
                    try {
                        if (row.getCell(5).getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(row.getCell(5))) {
                            docente.setAntiguedad(row.getCell(5).getLocalDateTimeCellValue().toLocalDate());
                        } else {
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("[yyyy-MM-dd][dd/MM/yyyy]");
                            docente.setAntiguedad(LocalDate.parse(antiguedadStr, dtf));
                        }
                    } catch (Exception e) { docente.setAntiguedad(LocalDate.now()); }
                }
                
                if (docente.getDepartamento() == null && !todosLosDeptos.isEmpty()) {
                    docente.setDepartamento(todosLosDeptos.get(random.nextInt(todosLosDeptos.size())));
                }
                
                docente = docenteRepo.save(docente);

                // 4. PROCESAR ASIGNATURA
                final String idAsig = tipoExcel.equals("G") ? "GUAR" : siglasModulo + "-" + cursoStr + "-" + codigoCicloLimpio;
                final String nomAsig = tipoExcel.equals("G") ? "Guardia" : siglasModulo + " (" + cursoStr + "º " + codigoCicloLimpio + ")";

                Asignatura asignatura = asignaturaRepo.findBySiglas(idAsig).orElseGet(() -> {
                    Asignatura nueva = new Asignatura();
                    nueva.setSiglas(idAsig);
                    nueva.setNombre(nomAsig);
                    return asignaturaRepo.save(nueva); 
                });

                if (!tipoExcel.equals("G")) {
                    try {
                        asignatura.setCurso(Integer.parseInt(cursoStr.replaceAll("[^0-9]", "")));
                    } catch (Exception e) { asignatura.setCurso(1); }
                    List<Ciclo> ciclosCandidatos = cicloRepo.findAllByCodigo(codigoCicloLimpio);
                    if (!ciclosCandidatos.isEmpty()) {
                        asignatura.setCiclo(ciclosCandidatos.get(random.nextInt(ciclosCandidatos.size())));
                    }
                    asignatura = asignaturaRepo.save(asignatura);
                }

                // 5. GUARDAR HORARIO Y PROCESAR GUARDIA
                Horario h = new Horario();
                h.setDocente(docente);
                h.setAsignatura(asignatura);
                h.setTipo(tipoExcel); 
                h.setAula(aulaExcel);
                h.setDia(mapearDia(obtenerValor(row.getCell(6), evaluator, formatter))); 
                h.setHora(obtenerHoraInt(row.getCell(7), evaluator, formatter)); 
                
                // IMPORTANTE: saveAndFlush para que esté disponible para la búsqueda de sustitutos
                Horario horarioGuardado = horarioRepo.saveAndFlush(h);

               /* if ("G".equalsIgnoreCase(tipoExcel)) {
                    // 1. Obtenemos el día del horario (1=Lunes, 2=Martes, etc.)
                    int diaSemanaHorario = horarioGuardado.getDia(); 
                    LocalDate hoy = LocalDate.now();
                    
                    // 2. Calculamos la fecha: Buscamos el PRÓXIMO día que coincida con el horario
                    // Si hoy es Domingo (12) e importa un horario de Lunes (1), devolverá el 13 de abril.
                    LocalDate fechaGuardia = hoy.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.of(diaSemanaHorario)));

                    // Opcional: Si el cálculo da HOY (domingo) pero quieres que sea para la semana que viene
                    if (fechaGuardia.equals(hoy)) {
                        fechaGuardia = fechaGuardia.plusWeeks(1);
                    }

                    System.out.println("📅 Generando guardia para el día: " + fechaGuardia + " (Horario ID: " + horarioGuardado.getId() + ")");

                    // 3. Ejecutamos la lógica de búsqueda de sustituto y guardado
                    // Pasamos la 'fechaGuardia' calculada, NO LocalDate.now()
                    guardiaService.procesarSustitucion(
                        horarioGuardado.getId(), 
                        "Material de guardia automático", 
                        "Sustitución generada por importación Excel", 
                        fechaGuardia 
                    );
                }*/
            }
            log.info("✅ Importación y procesamiento de guardias finalizado.");
        }
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replace(" ", "")
                .toLowerCase()
                .trim();
    }

    private String obtenerValor(Cell cell, FormulaEvaluator evaluator, DataFormatter formatter) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.FORMULA) {
            try {
                CellValue cv = evaluator.evaluate(cell);
                return switch (cv.getCellType()) {
                    case STRING -> cv.getStringValue().trim();
                    case NUMERIC -> formatter.formatCellValue(cell, evaluator).trim();
                    default -> formatter.formatCellValue(cell).trim();
                };
            } catch (Exception e) { return formatter.formatCellValue(cell).trim(); }
        }
        return formatter.formatCellValue(cell).trim();
    }

 
    private int mapearDia(String diaStr) {
        if (diaStr == null || diaStr.isEmpty()) return 1;
        
        String dia = diaStr.toLowerCase().trim();
        
        // Si el Excel trae nombres en lugar de números
        if (dia.contains("lun")) return 1;
        if (dia.contains("mar")) return 2;
        if (dia.contains("mie") || dia.contains("mié")) return 3;
        if (dia.contains("jue")) return 4;
        if (dia.contains("vie")) return 5;
        if (dia.contains("sab") || dia.contains("sáb")) return 6;
        if (dia.contains("dom")) return 7;

        // Si trae números ("1", "2", etc.)
        String num = dia.replaceAll("[^1-7]", "");
        return num.isEmpty() ? 1 : Integer.parseInt(num);
    }

    private int obtenerHoraInt(Cell cell, FormulaEvaluator evaluator, DataFormatter formatter) {
        String valor = obtenerValor(cell, evaluator, formatter).replaceAll("[^0-9]", "");
        try { return valor.isEmpty() ? 0 : Integer.parseInt(valor); } catch (Exception e) { return 0; }
    }
}