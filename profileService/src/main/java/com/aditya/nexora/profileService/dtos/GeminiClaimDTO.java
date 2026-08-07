package com.aditya.nexora.profileService.dtos;

import java.util.List;

public record GeminiClaimDTO(
        String claimType,
        String claimValue,
        int confidence,
        String explanation,
        List<String> supportingEvidencePaths
) {
}
