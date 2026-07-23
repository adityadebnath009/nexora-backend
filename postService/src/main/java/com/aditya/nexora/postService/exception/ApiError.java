package com.aditya.nexora.postService.exception;


import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
        int status,         //HTTP StatusCode
        String message,     //Message about the
        String path,        //The path where the error has occurred
        LocalDateTime timestamp,        //When the error has occurred
        Map<String, String> validationErrors
) {
    public ApiError(int status, String message, String path, LocalDateTime timestamp) {
        this(status, message, path, timestamp, null);
    }
}
