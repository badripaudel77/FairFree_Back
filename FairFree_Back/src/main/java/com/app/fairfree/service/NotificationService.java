package com.app.fairfree.service;
import com.app.fairfree.dto.NotificationResponse;
import com.app.fairfree.enums.NotificationType;
import com.app.fairfree.model.Notification;
import com.app.fairfree.model.User;
import com.app.fairfree.repository.NotificationRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;
    private final NotificationRepository notificationRepository;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public NotificationService(JavaMailSender mailSender, NotificationRepository notificationRepository) {
        this.mailSender = mailSender;
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponse> getNotificationsForUser(User user) {
        List<Notification> notifications =  notificationRepository.getAllNotificationsForTheUser(user.getId(), LocalDateTime.now().minusYears(1));

        return notifications.stream()
                .map(notification -> NotificationResponse.builder()
                        .id(notification.getId())
                        .itemId(notification.getItemId())
                        .type(notification.getType())
                        .message(notification.getMessage())
                        .isRead(notification.isRead())
                        .createdAt(notification.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void readNotification(User user) {
        List<Notification> unreadNotifications = notificationRepository.getAllNotificationsForTheUser(user.getId(), null);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    public Notification pushNotification(User user, Long itemId, NotificationType type, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .itemId(itemId)
                .type(type)
                .message(message)
                .read(false) // just to be explicit
                .build();

        return notificationRepository.save(notification);
    }

    public void sendEmailNotification(String to, String subject, String message) {
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
