package com.app.fairfree.dto;

import com.app.fairfree.enums.ItemStatus;

public record ItemResponse(
        Long id,
        String name,
        String location,
        String imageUrl,
        ItemStatus status,
        String ownerEmail,
        int expiresAfterDays) { }