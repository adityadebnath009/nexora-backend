package com.aditya.nexora.postService.dto;

import java.time.Instant;
import java.util.List;

public record PostDTO(
        Long id,
        Long userId,
        String content,
        List<String> images,
        Instant createdAt,
        Instant updatedAt
        ) {
}
