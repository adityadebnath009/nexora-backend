package com.aditya.nexora.profileService.dtos;

import java.time.Instant;

public record ConnectedSourceDTO(
    String provider,
    String githubUsername,
    String ownershipStatus,
    Instant connectedAt
) {}