package com.aditya.nexora.profileService.dtos;

import jakarta.validation.constraints.NotNull;

public record ClaimApprovalRequestDTO(
    @NotNull(message = "Approval state is required (APPROVED or REJECTED)")
    String approvalState
) {}