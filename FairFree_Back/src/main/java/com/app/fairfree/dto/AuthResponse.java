package com.app.fairfree.dto;

public record AuthResponse(
        String accessToken, String refreshToken, String tokenType, String message) {

}

