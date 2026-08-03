package com.aditya.nexora.profileService.dtos;

import com.aditya.nexora.profileService.entity.*;
import java.util.List;

public record PublicProfileDTO(
    String username,
    String name,
    String headline,
    String about,
    String profilePictureUrl,
    List<Experience> experiences,
    List<Education> educations,
    List<Certification> certifications,
    List<DerivedClaim> approvedClaims
) {}