package com.aditya.nexora.profileService.dtos;

import java.time.Instant;

public record PublicEducationDTO(
    String school,
    String degree,
    String fieldOfStudy,
    Instant startDate,
    Instant endDate
) {}