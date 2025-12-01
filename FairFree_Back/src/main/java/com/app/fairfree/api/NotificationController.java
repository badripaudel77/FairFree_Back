package com.app.fairfree.api;

import com.app.fairfree.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "api/v1/public/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/test-email")
    public ResponseEntity<String> testEmail(@RequestParam String to) {
        try {
            notificationService.sendEmailNotification(
                    to,
                    "Test Email",
                    "This is a test email from your Spring Boot application!"
            );
            return ResponseEntity.ok("Email sent successfully to " + to);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to send email: " + e.getMessage());
        }
    }
}
