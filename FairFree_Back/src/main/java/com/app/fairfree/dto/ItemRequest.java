package com.app.fairfree.dto;

import com.app.fairfree.model.Category;

public record ItemRequest(
        String title,
        String description,
        Integer quantity,
        Boolean neverExpires,
        Integer expiresAfterDays,
        LocationRequest location,
        Category category
) {}
