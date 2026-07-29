package com.aditya.nexora.profileService.exception;

import com.aditya.nexora.profileService.exception.ApiError;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error
) {}