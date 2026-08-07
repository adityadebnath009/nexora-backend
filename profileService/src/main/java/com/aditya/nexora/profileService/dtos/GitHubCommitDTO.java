package com.aditya.nexora.profileService.dtos;

import java.time.Instant;
import java.time.LocalDateTime;

public record GitHubCommitDTO(
        String sha,
        String message,
        String authorName,
        String authorLogin,
        LocalDateTime date
) {
}
