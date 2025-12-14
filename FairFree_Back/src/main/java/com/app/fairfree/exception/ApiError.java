package com.app.fairfree.exception;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String path,
        Map<String, String> validationErrors
) {}
