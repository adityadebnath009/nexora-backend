package com.aditya.nexora.userService.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.dialect.sequence.SpannerSequenceSupport;

public record SignUpRequestDTO(
        @NotNull
        String email,

        @NotNull
        String password,

        @NotNull
        String name,

        String userName

){

}
