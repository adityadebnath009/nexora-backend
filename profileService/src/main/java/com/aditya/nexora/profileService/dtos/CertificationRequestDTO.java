package com.aditya.nexora.profileService.dtos;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record CertificationRequestDTO(
    @NotBlank(message = "Certification name is required") 
    String name,
    
    @NotBlank(message = "Issuing organization is required") 
    String issuingOrg,
    
    Instant issueDate,
    
    Instant expirationDate,
    
    String credentialId,
    
    String credentialUrl
) {}