package com.aditya.nexora.profileService.controllers;


import com.aditya.nexora.profileService.dtos.*;
import com.aditya.nexora.profileService.entity.*;

import com.aditya.nexora.profileService.services.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/experiences")
    public ResponseEntity<List<Experience>> getExperiences(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(profileService.getExperiences(userId));
    }
    @PostMapping("/experiences")
    public ResponseEntity<Experience> addExperience(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid ExperienceRequestDTO request) {
        return ResponseEntity.ok(profileService.addExperience(userId, request));
    }
    @PutMapping("/experiences/{experienceId}")
    public ResponseEntity<Experience> updateExperience(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long experienceId,
            @RequestBody @Valid ExperienceRequestDTO request) {
        return ResponseEntity.ok(profileService.updateExperience(userId, experienceId, request));
    }
    @DeleteMapping("/experiences/{experienceId}")
    public ResponseEntity<Map<String, String>> deleteExperience(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long experienceId) {
        profileService.deleteExperience(userId, experienceId);
        return ResponseEntity.ok(Map.of("message", "Experience deleted successfully"));
    }

    @GetMapping("/educations")
    public ResponseEntity<List<Education>> getEducations(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(profileService.getEducations(userId));
    }
    @PostMapping("/educations")
    public ResponseEntity<Education> addEducation(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid EducationRequestDTO request) {
        return ResponseEntity.ok(profileService.addEducation(userId, request));
    }
    @PutMapping("/educations/{educationId}")
    public ResponseEntity<Education> updateEducation(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long educationId,
            @RequestBody @Valid EducationRequestDTO request) {
        return ResponseEntity.ok(profileService.updateEducation(userId, educationId, request));
    }
    @DeleteMapping("/educations/{educationId}")
    public ResponseEntity<Map<String, String>> deleteEducation(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long educationId) {
        profileService.deleteEducation(userId, educationId);
        return ResponseEntity.ok(Map.of("message", "Education deleted successfully"));
    }

    @GetMapping("/certifications")
    public ResponseEntity<List<Certification>> getCertifications(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(profileService.getCertifications(userId));
    }
    @PostMapping("/certifications")
    public ResponseEntity<Certification> addCertification(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid CertificationRequestDTO request) {
        return ResponseEntity.ok(profileService.addCertification(userId, request));
    }
    @PutMapping("/certifications/{certificationId}")
    public ResponseEntity<Certification> updateCertification(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long certificationId,
            @RequestBody @Valid CertificationRequestDTO request) {
        return ResponseEntity.ok(profileService.updateCertification(userId, certificationId, request));
    }
    @DeleteMapping("/certifications/{certificationId}")
    public ResponseEntity<Map<String, String>> deleteCertification(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long certificationId) {
        profileService.deleteCertification(userId, certificationId);
        return ResponseEntity.ok(Map.of("message", "Certification deleted successfully"));
    }

    @GetMapping("/claims")
    public ResponseEntity<List<DerivedClaim>> getDerivedClaims(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(profileService.getDerivedClaims(userId));
    }
    @PutMapping("/claims/{claimId}/approval")
    public ResponseEntity<DerivedClaim> updateClaimApproval(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long claimId,
            @RequestBody @Valid ClaimApprovalRequestDTO request) {
        DerivedClaim claim = profileService.updateClaimApproval(userId, claimId, request.approvalState());
        return ResponseEntity.ok(claim);
    }

}
