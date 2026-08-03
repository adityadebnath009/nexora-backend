package com.aditya.nexora.profileService.dtos;

import java.time.Instant;

public record PublicCertificationDTO(
    String name,
    String issuingOrg,
    Instant issueDate,
    Instant expirationDate,
    String credentialId,
    String credentialUrl
) {}