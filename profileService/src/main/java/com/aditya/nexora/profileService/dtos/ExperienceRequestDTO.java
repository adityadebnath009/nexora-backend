package com.aditya.nexora.profileService.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ExperienceRequestDTO(
    @NotBlank(message = "Job title is required") 
    String title,
    
    @NotBlank(message = "Company is required") 
    String company,
    
    String location,
    
    @NotNull(message = "Start date is required") 
    Instant startDate,
    
    Instant endDate,
    
    boolean isCurrent,
    
    String description
) {}