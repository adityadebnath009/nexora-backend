package com.aditya.nexora.profileService.client;

import com.aditya.nexora.profileService.dtos.UserResponseDTO;
import com.aditya.nexora.profileService.exception.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @GetMapping("/user/{userId}")
    ApiResponse<UserResponseDTO> getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/user/username/{username}")
    ApiResponse<UserResponseDTO> getUserByUsername(@PathVariable("username") String username);
}