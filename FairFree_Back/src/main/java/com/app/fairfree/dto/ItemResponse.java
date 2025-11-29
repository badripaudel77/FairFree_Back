package com.app.fairfree.dto;

import com.app.fairfree.enums.ItemStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record ItemResponse(
        Long id,
        String name,
        String location,
        List<String> imageUrls,
        ItemStatus status,
        Long ownerId,
        Long receiverId,
        String ownerName,
        Integer expiresAfterDays,
        Boolean neverExpires
) {}