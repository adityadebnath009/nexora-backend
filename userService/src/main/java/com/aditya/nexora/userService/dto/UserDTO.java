package com.aditya.nexora.userService.dto;

import java.time.Instant;

public record UserDTO(
        Long id,
        String username,
        String email,
        String name,
        String headLine,
        String about,
        String profilePictureUrl,

        Instant createdAt,
        Instant updatedAt
) {


}
