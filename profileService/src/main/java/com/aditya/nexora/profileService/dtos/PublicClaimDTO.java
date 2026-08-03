package com.aditya.nexora.profileService.dtos;

import com.aditya.nexora.profileService.enums.ClaimType;

public record PublicClaimDTO(
    ClaimType claimType,
    String claimValue,
    String explanation,
    int confidence
) {}