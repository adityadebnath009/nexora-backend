package com.aditya.nexora.profileService.controller;


import com.aditya.nexora.profileService.dtos.PublicProfileDTO;
import com.aditya.nexora.profileService.services.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/profile")
public class PublicProfileController {

    private final ProfileService profileService;

    public PublicProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<PublicProfileDTO> getPublicProfile(
            @PathVariable("username") String username
    )
    {
        PublicProfileDTO publicProfileDTO = profileService.getPublicProfile(username);
        return ResponseEntity.ok(publicProfileDTO);
    }


}
