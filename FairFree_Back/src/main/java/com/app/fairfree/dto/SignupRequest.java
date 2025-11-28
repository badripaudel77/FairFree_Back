package com.app.fairfree.dto;

public record SignupRequest(
        String fullName, String email, String password) {

        public SignupRequest{
            if(fullName.isBlank() || email.isBlank() || password.isEmpty()) {
                throw new RuntimeException("Invalid Credentials.");
            }
        }
}

