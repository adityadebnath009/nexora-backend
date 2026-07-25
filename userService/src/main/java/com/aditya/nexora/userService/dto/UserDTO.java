package com.aditya.nexora.userService.dto;

import com.aditya.nexora.userService.entity.User;
import com.aditya.nexora.userService.enums.Role;

import java.time.Instant;
import java.util.Set;

public record UserDTO(
        Long id,
        String username,
        String email,
        String name,
        String headLine,
        String about,
        String profilePictureUrl,
        Set<Role> roles,

        Instant createdAt,
        Instant updatedAt
) {

}
