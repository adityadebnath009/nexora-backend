package com.aditya.nexora.userService.dto;

import java.util.List;

public record UsernameCheckResponseDTO(
        boolean isAvailable,
        List<String> suggestions
) {
}
