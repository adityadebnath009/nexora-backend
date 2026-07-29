package com.aditya.nexora.profileService.dtos;

import java.util.Map;

public record DeveloperMetricsDTO(
    Integer totalRepositories,
    Integer starsEarned,
    Integer forksCount,
    Map<String, Long> languagesUsed,
    Integer longestContributionStreak,
    Integer repositoriesCreatedThisYear,
    Double averageCommitFrequency
) {}