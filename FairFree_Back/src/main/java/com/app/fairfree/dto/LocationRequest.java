package com.app.fairfree.dto;

public record LocationRequest(
        String address,
        String city,
        String state,
        String country,
        Double latitude,
        Double longitude
) {}