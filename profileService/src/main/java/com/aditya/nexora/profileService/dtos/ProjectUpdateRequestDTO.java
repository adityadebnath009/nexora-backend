package com.aditya.nexora.profileService.dtos;

public record ProjectUpdateRequestDTO (
        String title,
        String description,
        String liveUrl,
        String statedRole,
        String contributionSummary
){
}
