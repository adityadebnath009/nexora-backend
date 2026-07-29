package com.aditya.nexora.profileService.exception;


import org.springframework.http.HttpStatus;

public class UserAlreadyExistedException extends ApiException {



    public UserAlreadyExistedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);

    }

    public UserAlreadyExistedException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }


}

