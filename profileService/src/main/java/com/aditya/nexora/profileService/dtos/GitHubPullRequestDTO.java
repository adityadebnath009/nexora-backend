package com.aditya.nexora.profileService.dtos;

import java.time.LocalDateTime;

public record GitHubPullRequestDTO(
        Long id,
        String title,
        String body,
        String state,
        String authorLogin,
        LocalDateTime createdAt,
        LocalDateTime mergedAt


) {
}
