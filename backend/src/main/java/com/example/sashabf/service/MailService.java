package com.example.sashabf.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ByteArrayResource;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.admin.emails}")
    private String[] adminEmails; // Lee la lista del properties automáticamente

    public void enviarSolicitudAdmin(String nombreDocente, String fecha, String motivo, byte[] excelContenido) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // El 'true' indica que es un mensaje multipart (con adjuntos)
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(adminEmails);
            helper.setSubject("NUEVA SOLICITUD: Asunto Propio - " + nombreDocente);
            
            String cuerpo = String.format(
                "Hola,\n\nEl docente %s ha solicitado un día de asuntos propios.\n" +
                "Fecha solicitada: %s\nMotivo: %s\n\nSe adjunta el modelo oficial en Excel.",
                nombreDocente, fecha, motivo
            );
            
            helper.setText(cuerpo);

            // Adjuntamos el Excel
            helper.addAttachment("Solicitud_" + nombreDocente + ".xlsx", 
                               new ByteArrayResource(excelContenido));

            mailSender.send(message);
            System.out.println("Email enviado correctamente a los admins.");
            
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar el email: " + e.getMessage());
        }
    }
}
