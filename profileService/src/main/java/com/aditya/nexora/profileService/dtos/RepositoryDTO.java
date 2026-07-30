package com.aditya.nexora.profileService.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record RepositoryDTO(
        Long id,
    String name,
    String description,
    String htmlUrl,
    Integer stars,
    Integer forks,
    String primaryLanguage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String visibility,
    List<String> topics
) {}