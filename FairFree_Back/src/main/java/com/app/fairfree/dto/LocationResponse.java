package com.app.fairfree.dto;

import com.app.fairfree.model.ItemLocation;

public record LocationResponse(
        String address,
        String city,
        String state,
        String country,
        Double latitude,
        Double longitude
) {
    public static LocationResponse from(ItemLocation loc) {
        return new LocationResponse(
                loc.getAddress(),
                loc.getCity(),
                loc.getState(),
                loc.getCountry(),
                loc.getLatitude(),
                loc.getLongitude()
        );
    }
}
