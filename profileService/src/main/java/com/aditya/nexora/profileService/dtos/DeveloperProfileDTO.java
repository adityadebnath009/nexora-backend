package com.aditya.nexora.profileService.dtos;

import com.aditya.nexora.profileService.entity.*;
import java.util.List;

public record DeveloperProfileDTO(
    UserResponseDTO userDetails,
    List<Experience> experiences,
    List<Education> educations,
    List<Certification> certifications,
    List<DerivedClaim> claims
) {}