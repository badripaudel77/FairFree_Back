package com.app.fairfree.dto;

public record UserResponse(
        Long id,
        String fullName,
        String email
) {
    // Optional helper method to convert from User entity
    public static UserResponse from(com.app.fairfree.model.User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail()
        );
    }
}
