package com.app.fairfree.service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {


    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendNotification(String to, String subject, String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(buildHtmlTemplate(subject, message), true); // true = HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildHtmlTemplate(String subject, String message) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='margin:0; padding:0; font-family:Arial,sans-serif; background-color:#f4f4f4;'>" +
                "<table width='100%' style='max-width:600px; margin:0 auto; background-color:#ffffff;'>" +
                "<tr><td style='background-color:#4A90A4; padding:30px; text-align:center;'>" +
                "<h2 style='color:#ffffff; margin:0;'>" + subject + "</h2>" +
                "</td></tr>" +
                "<tr><td style='padding:40px 30px;'>" +
                "<p style='color:#333; font-size:16px; line-height:1.6;'>" + message + "</p>" +
                "</td></tr>" +
                "<tr><td style='background-color:#f8f8f8; padding:20px; text-align:center;'>" +
                "<p style='color:#888; font-size:12px;'>Don't replay to this mail, it is an automated message.</p>" +
                "</td></tr>" +
                "</table></body></html>";
    }
}
