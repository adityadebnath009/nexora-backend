package com.aditya.nexora.profileService.dtos;

public record GitHubProfileDTO(
        Long id,
        String username,
        String avatarUrl,
        String bio,
        Integer followers,
        Integer following,
        Integer publicRepos,
        String location,
        String company,
        String blog
) {

}
