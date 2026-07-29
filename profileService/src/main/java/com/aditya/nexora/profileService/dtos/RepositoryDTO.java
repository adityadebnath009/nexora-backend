package com.aditya.nexora.profileService.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record RepositoryDTO(
    String name,
    String description,
    Integer stars,
    Integer forks,
    String primaryLanguage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String visibility,
    List<String> topics
) {}