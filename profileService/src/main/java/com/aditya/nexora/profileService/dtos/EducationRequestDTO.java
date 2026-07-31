package com.aditya.nexora.profileService.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record EducationRequestDTO(
    @NotBlank(message = "School name is required") 
    String school,
    
    @NotBlank(message = "Degree is required") 
    String degree,
    
    String fieldOfStudy,
    
    @NotNull(message = "Start date is required") 
    Instant startDate,
    
    Instant endDate
) {}