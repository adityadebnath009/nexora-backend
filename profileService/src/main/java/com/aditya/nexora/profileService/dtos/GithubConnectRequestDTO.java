package com.aditya.nexora.profileService.dtos;

import jakarta.validation.constraints.NotBlank;

public record GithubConnectRequestDTO(
        @NotBlank(message = "GitHub authorization code is required")
      String code
) {}
