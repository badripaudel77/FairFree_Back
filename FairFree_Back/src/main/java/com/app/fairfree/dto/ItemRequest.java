package com.app.fairfree.dto;

public record ItemRequest(
        String title,
        String description,
        Integer quantity,
        Boolean neverExpires,
        Integer expiresAfterDays,
        LocationRequest location
) {}
