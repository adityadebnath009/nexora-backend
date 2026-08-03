package com.aditya.nexora.profileService.dtos;

import com.aditya.nexora.profileService.entity.*;
import java.util.List;

public record PublicProfileDTO(
    String username,
    String name,
    String headline,
    String about,
    String profilePictureUrl,
    List<PublicExperienceDTO> experiences,
    List<PublicEducationDTO> educations,
    List<PublicCertificationDTO> certifications,
    List<PublicClaimDTO> approvedClaims
) {}