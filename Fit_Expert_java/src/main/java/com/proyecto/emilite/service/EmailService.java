package com.proyecto.emilite.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@SuppressWarnings("null")
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 🔥 Inyectamos la URL base. Si no existe en la nube, usa localhost por defecto.
    @Value("${app.base-url:http://localhost:8082}")
    private String baseUrl;

    // Envio de correo
    public void enviarNotificacionRegistro(String destinatario, String nombre, Double score){
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("FitExpert - Recibimos tu postulación 🏋️");

            String contenido = 
                "<div style='font-family: Arial, sans-serif; color: #1e293b; padding: 20px; border: 1px solid #e2e8f0; border-radius: 10px;'>" +
                "<h2 style='color: #4f46e5;'>¡Hola, " + nombre + "!</h2>" +
                "<p>Gracias por registrarte como entrenador. Nuestro sistema de IA ha analizado tu CV.</p>" +
                "<div style='background: #f1f5f9; padding: 10px; border-radius: 5px; font-weight: bold;'>" +
                "Score de compatibilidad: " + score + "%" +
                "</div>" +
                "<p>Un administrador revisará tu perfil pronto para darte acceso total.</p>" +
                "<p style='font-size: 0.8rem; color: #64748b;'>No respondas a este correo automático.</p>" +
                "</div>";

            helper.setText(contenido, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Error al enviar correo de registro: " + e.getMessage());
        }
    }
    
    public void enviarCorreoAprobacion(String destinatario, String nombre) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("¡Bienvenido al Equipo Oficial de FitExpert! 🚀");

            // 🔥 Usamos la variable baseUrl para que el botón apunte al lugar correcto siempre
            String contenido = 
                "<div style='font-family: Arial, sans-serif; color: #1e293b; padding: 20px; background: #fafafa; border-radius: 10px;'>" +
                "<h1 style='color: #10b981;'>¡Felicidades, " + nombre + "!</h1>" +
                "<p>Tu perfil ha sido <strong>aprobado oficialmente</strong>.</p>" +
                "<p>Ya puedes ingresar a la plataforma con tus credenciales y comenzar a gestionar tus rutinas.</p>" +
                "<a href='" + baseUrl + "/login' style='background: #4f46e5; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; margin-top: 10px;'>Ir al Login</a>" +
                "</div>";

            helper.setText(contenido, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Error al enviar correo de aprobación: " + e.getMessage());
        }
    }
}