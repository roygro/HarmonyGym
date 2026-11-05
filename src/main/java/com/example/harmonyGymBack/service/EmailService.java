package com.example.harmonyGymBack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);


    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.subject.credenciales}")
    private String subjectCredenciales;

    // ==================== MÉTODO PARA CLIENTES ====================

    public void enviarCredencialesCliente(String emailCliente, String nombreCliente,
                                          String username, String passwordTemporal) {
        try {
            // Validar que el email del cliente no sea nulo o vacío
            if (emailCliente == null || emailCliente.trim().isEmpty()) {
                throw new RuntimeException("El email del cliente es requerido");
            }

            log.info("📧 Preparando envío de email a: " + emailCliente);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailCliente);
            helper.setSubject(subjectCredenciales);

            String contenido = construirContenidoEmailCliente(nombreCliente, username, passwordTemporal);
            helper.setText(contenido, true);

            mailSender.send(message);
            log.info("✅ Email enviado exitosamente a: " + emailCliente);

        } catch (MessagingException e) {
            log.error("❌ Error de mensajería al enviar email: " + e.getMessage());
            throw new RuntimeException("Error al enviar las credenciales por email: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error inesperado al enviar email: " + e.getMessage());
            throw new RuntimeException("Error inesperado al enviar email: " + e.getMessage());
        }
    }

    private String construirContenidoEmailCliente(String nombreCliente, String username, String passwordTemporal) {
        StringBuilder contenido = new StringBuilder();

        contenido.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<meta charset=\"UTF-8\">")
                .append("<style>")
                .append("body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }")
                .append(".container { max-width: 600px; margin: 20px auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 0 20px rgba(0,0,0,0.1); }")
                .append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px 20px; text-align: center; }")
                .append(".header h1 { margin: 0; font-size: 28px; }")
                .append(".content { padding: 30px; }")
                .append(".credentials { background: #f8f9fa; padding: 20px; margin: 20px 0; border-radius: 8px; border-left: 4px solid #667eea; }")
                .append(".credential-item { margin: 10px 0; padding: 10px; background: white; border-radius: 5px; border: 1px solid #e9ecef; }")
                .append(".warning { background: #fff3cd; color: #856404; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #ffc107; }")
                .append(".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e9ecef; font-size: 12px; color: #666; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class=\"container\">")
                .append("<div class=\"header\">")
                .append("<h1>🎉 ¡Bienvenido a Harmony Gym!</h1>")
                .append("</div>")
                .append("<div class=\"content\">")
                .append("<h2>Hola ").append(nombreCliente != null ? nombreCliente : "Cliente").append(",</h2>")
                .append("<p>Tu cuenta ha sido creada exitosamente. Aquí tienes tus credenciales de acceso:</p>")
                .append("<div class=\"credentials\">")
                .append("<h3>🔐 Tus Credenciales de Acceso:</h3>")
                .append("<div class=\"credential-item\">")
                .append("<strong>👤 Usuario:</strong>")
                .append("<div style=\"background: #e9ecef; padding: 8px; border-radius: 4px; margin-top: 5px; font-family: monospace;\">")
                .append(username)
                .append("</div>")
                .append("</div>")
                .append("<div class=\"credential-item\">")
                .append("<strong>🔑 Contraseña Temporal:</strong>")
                .append("<div style=\"background: #e9ecef; padding: 8px; border-radius: 4px; margin-top: 5px; font-family: monospace; font-weight: bold; color: #d63384;\">")
                .append(passwordTemporal)
                .append("</div>")
                .append("</div>")
                .append("</div>")
                .append("<div class=\"warning\">")
                .append("<strong>⚠️ Información Importante:</strong>")
                .append("<ul>")
                .append("<li>Cambia tu contraseña después del primer inicio de sesión</li>")
                .append("<li>No compartas tus credenciales con nadie</li>")
                .append("<li>Guarda esta información en un lugar seguro</li>")
                .append("<li>Esta contraseña es temporal - cámbiala pronto</li>")
                .append("</ul>")
                .append("</div>")
                .append("<p>Puedes acceder a tu cuenta en nuestra plataforma usando estas credenciales.</p>")
                .append("<p>Si tienes alguna pregunta o necesitas ayuda, no dudes en contactarnos.</p>")
                .append("<p>¡Esperamos verte pronto en el gimnasio! 💪</p>")
                .append("<p><strong>El equipo de Harmony Gym</strong></p>")
                .append("</div>")
                .append("<div class=\"footer\">")
                .append("<p>Harmony Gym &copy; 2024 - Transformando vidas a través del fitness</p>")
                .append("<p>Este es un email automático, por favor no respondas a este mensaje.</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return contenido.toString();
    }

    // ==================== MÉTODO PARA INSTRUCTORES ====================

    public void enviarCredencialesInstructor(String emailInstructor, String nombreInstructor,
                                             String username, String passwordTemporal) {
        try {
            // Validar que el email del instructor no sea nulo o vacío
            if (emailInstructor == null || emailInstructor.trim().isEmpty()) {
                throw new RuntimeException("El email del instructor es requerido");
            }

            log.info("📧 Preparando envío de email a instructor: " + emailInstructor);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailInstructor);
            helper.setSubject("🎯 Credenciales de Acceso - Harmony Gym Instructor");

            String contenido = construirContenidoEmailInstructor(nombreInstructor, username, passwordTemporal);
            helper.setText(contenido, true);

            mailSender.send(message);
            log.info("✅ Email enviado exitosamente a instructor: " + emailInstructor);

        } catch (MessagingException e) {
            log.error("❌ Error de mensajería al enviar email a instructor: " + e.getMessage());
            throw new RuntimeException("Error al enviar las credenciales por email: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error inesperado al enviar email a instructor: " + e.getMessage());
            throw new RuntimeException("Error inesperado al enviar email: " + e.getMessage());
        }
    }

    private String construirContenidoEmailInstructor(String nombreInstructor, String username, String passwordTemporal) {
        StringBuilder contenido = new StringBuilder();

        contenido.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<meta charset=\"UTF-8\">")
                .append("<style>")
                .append("body { font-family: 'Arial', sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }")
                .append(".container { max-width: 600px; margin: 20px auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 0 20px rgba(0,0,0,0.1); }")
                .append(".header { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%); color: white; padding: 30px 20px; text-align: center; }")
                .append(".header h1 { margin: 0; font-size: 28px; }")
                .append(".content { padding: 30px; }")
                .append(".credentials { background: #f8f9fa; padding: 20px; margin: 20px 0; border-radius: 8px; border-left: 4px solid #ff6b6b; }")
                .append(".credential-item { margin: 10px 0; padding: 10px; background: white; border-radius: 5px; border: 1px solid #e9ecef; }")
                .append(".warning { background: #fff3cd; color: #856404; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #ffc107; }")
                .append(".features { background: #d4edda; color: #155724; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #28a745; }")
                .append(".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e9ecef; font-size: 12px; color: #666; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<div class=\"container\">")
                .append("<div class=\"header\">")
                .append("<h1>🎯 ¡Bienvenido al Equipo Harmony Gym!</h1>")
                .append("</div>")
                .append("<div class=\"content\">")
                .append("<h2>Hola ").append(nombreInstructor != null ? nombreInstructor : "Instructor").append(",</h2>")
                .append("<p>Tu cuenta de instructor ha sido creada exitosamente. Aquí tienes tus credenciales de acceso:</p>")
                .append("<div class=\"credentials\">")
                .append("<h3>🔐 Tus Credenciales de Acceso:</h3>")
                .append("<div class=\"credential-item\">")
                .append("<strong>👤 Usuario:</strong>")
                .append("<div style=\"background: #e9ecef; padding: 8px; border-radius: 4px; margin-top: 5px; font-family: monospace;\">")
                .append(username)
                .append("</div>")
                .append("</div>")
                .append("<div class=\"credential-item\">")
                .append("<strong>🔑 Contraseña Temporal:</strong>")
                .append("<div style=\"background: #e9ecef; padding: 8px; border-radius: 4px; margin-top: 5px; font-family: monospace; font-weight: bold; color: #d63384;\">")
                .append(passwordTemporal)
                .append("</div>")
                .append("</div>")
                .append("</div>")
                .append("<div class=\"features\">")
                .append("<h3>🚀 Funcionalidades Disponibles:</h3>")
                .append("<ul>")
                .append("<li>Gestión de actividades y clases</li>")
                .append("<li>Asignación de rutinas a clientes</li>")
                .append("<li>Seguimiento del progreso de clientes</li>")
                .append("<li>Calendario de horarios</li>")
                .append("<li>Reportes y estadísticas</li>")
                .append("</ul>")
                .append("</div>")
                .append("<div class=\"warning\">")
                .append("<strong>⚠️ Información Importante:</strong>")
                .append("<ul>")
                .append("<li>Cambia tu contraseña después del primer inicio de sesión</li>")
                .append("<li>No compartas tus credenciales con nadie</li>")
                .append("<li>Guarda esta información en un lugar seguro</li>")
                .append("<li>Esta contraseña es temporal - cámbiala pronto</li>")
                .append("</ul>")
                .append("</div>")
                .append("<p>Puedes acceder al sistema de instructores usando estas credenciales.</p>")
                .append("<p>Si tienes alguna pregunta o necesitas ayuda, contacta al administrador del sistema.</p>")
                .append("<p>¡Bienvenido al equipo! 💪</p>")
                .append("<p><strong>El equipo de Harmony Gym</strong></p>")
                .append("</div>")
                .append("<div class=\"footer\">")
                .append("<p>Harmony Gym &copy; 2024 - Sistema de Gestión de Instructores</p>")
                .append("<p>Este es un email automático, por favor no respondas a este mensaje.</p>")
                .append("</div>")
                .append("</div>")
                .append("</body>")
                .append("</html>");

        return contenido.toString();
    }
}