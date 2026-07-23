package com.aditya.nexora.userService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class StorageException extends ApiException {
    public StorageException(String message, HttpStatusCode statusCode) {
        super(message, (HttpStatus) statusCode);
    }

}
