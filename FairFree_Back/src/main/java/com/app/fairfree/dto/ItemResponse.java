package com.app.fairfree.dto;

import com.app.fairfree.enums.ItemStatus;
import lombok.Builder;

import java.util.List;

@Builder
public record ItemResponse(
        Long id,
        String name,
        String description,
        Integer quantity,
        LocationResponse location,
        List<String> imageUrls,
        ItemStatus status,
        UserResponse owner,         //  owner
        UserResponse receiver,      // receiver, null if not claimed
        Integer expiresAfterDays,
        Boolean neverExpires,
        List<ClaimResponse> claims
) {}