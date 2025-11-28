package com.app.fairfree.dto;

import java.time.LocalDateTime;

public record ItemRequest(
        String name,
        String location,
        String imageUrl,
        int expiresAfterDays,
        boolean neverExpires // if item doesn't expire, like clothes
) {}
