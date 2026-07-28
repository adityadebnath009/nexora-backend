package com.aditya.nexora.userService.exception;

import org.springframework.http.HttpStatus;

public class InvalidFileException extends ApiException
{
    public InvalidFileException(String message) {
        super(message, HttpStatus.BAD_REQUEST);

    }
}
