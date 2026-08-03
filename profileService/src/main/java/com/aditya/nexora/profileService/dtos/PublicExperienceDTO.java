package com.aditya.nexora.profileService.dtos;

import java.time.Instant;

public record PublicExperienceDTO(
    String title,
    String company,
    String location,
    Instant startDate,
    Instant endDate,
    boolean isCurrent,
    String description
) {}