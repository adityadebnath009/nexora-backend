package com.aditya.nexora.postService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record PostCreateRequestDTO(
        @NotNull
        @NotBlank
        String content,
        List<MultipartFile> images
) {}
