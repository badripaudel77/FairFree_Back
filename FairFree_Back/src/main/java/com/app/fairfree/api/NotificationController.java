package com.app.fairfree.api;

import com.app.fairfree.dto.NotificationResponse;
import com.app.fairfree.model.User;
import com.app.fairfree.repository.UserRepository;
import com.app.fairfree.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(notificationService.getNotificationsForUser(user));
    }

    @PutMapping("/mark-read")
    public ResponseEntity<String> readNotification(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        notificationService.readNotification(user);
        return ResponseEntity.ok("Notifications read updated");
    }
}
