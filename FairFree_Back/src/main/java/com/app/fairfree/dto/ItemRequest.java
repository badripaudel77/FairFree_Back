package com.app.fairfree.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record ItemRequest(
        String name,
        String description,
        String location,
        Double latitude,
        Double longitude,
        List<MultipartFile> images, // up to 3 files
        Integer expiresAfterDays,
        Boolean neverExpires // if item doesn't expire, like clothes
) {}
