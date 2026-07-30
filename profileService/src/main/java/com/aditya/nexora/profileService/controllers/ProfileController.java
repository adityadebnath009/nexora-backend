package com.aditya.nexora.profileService.controllers;


import com.aditya.nexora.profileService.dtos.GithubConnectRequestDTO;
import com.aditya.nexora.profileService.entity.ConnectedSource;
import com.aditya.nexora.profileService.services.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;


    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/sources/github/connect")
    public ResponseEntity<ConnectedSource> connectGithub(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid GithubConnectRequestDTO request) {

       ConnectedSource connectedSource = profileService.connectGithub(userId, request.code());
       return ResponseEntity.ok(connectedSource);

    }

    @PostMapping("/sources/github/sync")
    public ResponseEntity<Map<String, String>> syncRepositories(
            @RequestHeader("X-User-Id") Long userId) {

        profileService.syncGithubRepositories(userId);
        return ResponseEntity.ok(Map.of("message", "Repositories synced successfully"));

    }

}
