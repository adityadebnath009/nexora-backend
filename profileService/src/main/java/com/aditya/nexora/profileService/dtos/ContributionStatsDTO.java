package com.aditya.nexora.profileService.dtos;

public record ContributionStatsDTO(
    Integer totalContributions,
    Integer commitStreak,
    Integer yearlyContributions,
    Integer totalCommits,
    Integer totalPullRequests,
    Integer totalIssues
) {}