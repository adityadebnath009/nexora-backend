package com.aditya.nexora.userService.exception;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error
) {}