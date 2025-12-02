package com.app.fairfree.dto;

import com.app.fairfree.enums.ClaimStatus;
import com.app.fairfree.model.Claim;

public record ClaimResponse(
        Long id,
        Long userId,
        String userName,
        ClaimStatus status,
        String message
) {
    public static ClaimResponse from(Claim claim) {
        return new ClaimResponse(
                claim.getId(),
                claim.getUser().getId(),
                claim.getUser().getFullName(),
                claim.getStatus(),
                claim.getMessage()
        );
    }
}
