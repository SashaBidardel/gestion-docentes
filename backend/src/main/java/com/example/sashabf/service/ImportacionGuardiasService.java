package com.example.sashabf.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Iterator;

@Service
public class ImportacionGuardiasService {

    @Autowired
    private GuardiaService guardiaService;

    /**
     * Procesa un Excel simplificado de 3 columnas:
     * Columna 0: ID_HORARIO (Numérico o Texto)
     * Columna 1: MATERIAL (Texto)
     * Columna 2: ANOTACIONES (Texto)
     */
    @Transactional
    public String importarAusenciasDesdeExcel(MultipartFile file) {
        int filasProcesadas = 0;
        int filaActual = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 1. Saltar cabecera
            if (rowIterator.hasNext()) {
                rowIterator.next();
                filaActual++;
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                filaActual++;
                
                if (row == null) continue;
                
                // 2. Leer ID_HORARIO (Columna 0)
                Cell cellId = row.getCell(0);
                if (cellId == null || cellId.getCellType() == CellType.BLANK) continue;

                try {
                    Long idHorario;
                    if (cellId.getCellType() == CellType.NUMERIC) {
                        idHorario = (long) cellId.getNumericCellValue();
                    } else {
                        idHorario = Long.parseLong(cellId.getStringCellValue().trim());
                    }

                    // --- EXCEL SIMPLIFICADO: ÍNDICES CORREGIDOS ---
                    
                    // 3. Extraer MATERIAL (Ahora es la Columna 1)
                    String material = obtenerTextoSeguro(row.getCell(1), "Sin material");
                    
                    // 4. Extraer ANOTACIONES (Ahora es la Columna 2)
                    String anotacion = obtenerTextoSeguro(row.getCell(2), "Sin anotaciones");

                    // 5. Procesar la guardia con la fecha de hoy
                    // Esto dispara la lógica de buscar sustituto y candidatos en GuardiaService
                    guardiaService.procesarSustitucion(idHorario, material, anotacion, LocalDate.now());
                    
                    filasProcesadas++;

                } catch (Exception e) {
                    // Si una fila falla, lanzamos error indicando qué fila exacta es
                    throw new RuntimeException("Error en fila " + filaActual + ": " + e.getMessage());
                }
            }
            return "Éxito: Se han generado/actualizado " + filasProcesadas + " guardias correctamente.";

        } catch (Exception e) {
            throw new RuntimeException("Error crítico al procesar el archivo Excel: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar para evitar NullPointerExceptions y limpiar textos de ayuda del Excel
     */
    private String obtenerTextoSeguro(Cell cell, String valorDefecto) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return valorDefecto;
        }
        
        String valor;
        if (cell.getCellType() == CellType.NUMERIC) {
            valor = String.valueOf((long) cell.getNumericCellValue());
        } else {
            valor = cell.getStringCellValue().trim();
        }
        
        // Filtro para limpiar instrucciones que el Admin haya podido dejar en la plantilla
        if (valor.toLowerCase().contains("escriba aquí") || valor.toLowerCase().contains("instrucciones")) {
            return valorDefecto;
        }
        
        return valor.isEmpty() ? valorDefecto : valor;
    }
}