package com.app.fairfree.dto;

import com.app.fairfree.enums.ItemStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
@Builder
public record NotificationResponse (
    Long id,
    String message,
    LocalDateTime createdAt
){}