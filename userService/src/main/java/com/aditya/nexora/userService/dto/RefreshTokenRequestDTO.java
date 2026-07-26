package com.aditya.nexora.userService.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
    @NotBlank(message = "Refresh token cannot be blank")
    String refreshToken
) {}
