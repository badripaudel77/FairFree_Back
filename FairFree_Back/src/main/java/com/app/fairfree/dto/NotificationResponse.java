package com.app.fairfree.dto;

import com.app.fairfree.enums.NotificationType;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record NotificationResponse (
    Long id,
    String message,
    Long itemId,
    LocalDateTime createdAt,
    NotificationType type,
    boolean isRead
){}