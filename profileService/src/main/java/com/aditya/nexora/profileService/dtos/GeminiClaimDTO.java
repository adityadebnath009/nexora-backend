package com.aditya.nexora.profileService.dtos;

public record GeminiClaimDTO(
        String claimType,
        String claimValue,
        int confidence,
        String explanation
) {
}
