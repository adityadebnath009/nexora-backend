package com.aditya.nexora.profileService.dtos;

import java.util.List;

public record GeminiAnalysisResponseDTO(
        String aiSummary,
        String architectureSummary,
        String confidenceLevel,
        List<GeminiClaimDTO> claims,
        List<GeminiEvidenceDTO> evidenceItems
) {
}
