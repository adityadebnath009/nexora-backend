package com.aditya.nexora.userService.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ApiException{


    public ForbiddenException(String message, Throwable cause)
    {
        super(message, cause, HttpStatus.FORBIDDEN);

    }
    public ForbiddenException(String message)
    {
        super(message, HttpStatus.FORBIDDEN);
    }
}
