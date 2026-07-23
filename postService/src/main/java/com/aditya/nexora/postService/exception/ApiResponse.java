package com.aditya.nexora.postService.exception;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error
) {}