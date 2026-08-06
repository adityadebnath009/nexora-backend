package com.aditya.nexora.profileService.dtos;

public record GitHubTreeItemDTO(
        String path,
        String type,
        Long size,
        String sha,
        String url
) {
}
