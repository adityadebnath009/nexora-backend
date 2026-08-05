package com.aditya.nexora.profileService.dtos;

import java.time.Instant;
import java.util.List;

public record PublicProjectDTO(
        String title,
        String description,
        String repositoryUrl,
        String liveUrl,
        List<String> techStack,
        String userRole,
        String contributionSummary,
        String aiSummary,
        String architectureSummary,
        String evidenceSummary,
        Instant lastUpdatedDate

) {
}
