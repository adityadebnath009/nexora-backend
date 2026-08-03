package com.aditya.nexora.profileService.dtos;

public record UserResponseDTO(
    Long id,
    String username,
    String name,
    String email,
    String headLine,
    String about,
    String profilePictureUrl
) {}
