package com.aditya.nexora.userService.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequestDTO(
        @NotNull
        String email,

        @NotNull
        String password
){

}
