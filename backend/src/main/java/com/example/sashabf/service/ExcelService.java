package com.example.sashabf.service;

import com.example.sashabf.entity.AsuntoPropio;
import com.example.sashabf.entity.Docente;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter; // IMPORTANTE: Usamos java.time

@Service
public class ExcelService {

    public byte[] generarExcelAsuntoPropio(Docente docente, AsuntoPropio solicitud,String material, String anotaciones) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Solicitud_Guardia");

            // 1. Cabeceras
            Row header = sheet.createRow(0);
            String[] cols = { "ID_HORARIO", "DOCENTE", "MOTIVO_SOLICITUD", "MATERIAL", "ANOTACIONES", "FECHA_DIA" };

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            // 2. Inserción de Datos
            Row row = sheet.createRow(1);

            // ID_HORARIO
            if (solicitud.getHorario() != null) {
                row.createCell(0).setCellValue(solicitud.getHorario().getId());
            } else {
                row.createCell(0).setCellValue("ERROR: SIN ID"); 
            }

            // DOCENTE
            String nombreCompleto = (docente != null) ? 
                docente.getNombre() + " " + docente.getApellidos() : "Docente no identificado";
            row.createCell(1).setCellValue(nombreCompleto);

            // MOTIVO
            String descripcion = (solicitud.getDescripcion() != null) ? 
                solicitud.getDescripcion() : "Asuntos Propios";
            row.createCell(2).setCellValue(descripcion);
         // MATERIAL y ANOTACIONES
            // Ahora usamos directamente los Strings que recibimos por parámetro
            row.createCell(3).setCellValue(material != null ? material : "Sin material");
            row.createCell(4).setCellValue(anotaciones != null ? anotaciones : "Sin anotaciones");
            

            // FECHA_DIA 
            if (solicitud.getDiaSolicitado() != null) {
                // ✅ USAMOS DateTimeFormatter para LocalDate
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                row.createCell(5).setCellValue(solicitud.getDiaSolicitado().format(dtf));
            } else {
                row.createCell(5).setCellValue("Fecha no definida");
            }

            // Formato
            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(bos);
            return bos.toByteArray();
        }
    }
}