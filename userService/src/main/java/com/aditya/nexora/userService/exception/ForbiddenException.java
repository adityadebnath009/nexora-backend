package com.aditya.nexora.userService.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException{


    public ForbiddenException(String message, HttpStatus status) {
        super(message, status);
    }

    protected ForbiddenException(String message, Throwable cause, HttpStatus status) {
        super(message, cause, status);
    }
}
