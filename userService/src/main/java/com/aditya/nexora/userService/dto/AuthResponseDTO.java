package com.aditya.nexora.userService.dto;

public record AuthResponseDTO(
    String accessToken,
    String refreshToken,
    UserDTO user
) {}
