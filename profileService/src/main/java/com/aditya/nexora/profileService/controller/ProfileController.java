package com.aditya.nexora.profileService.controller;

import com.aditya.nexora.profileService.dtos.*;
import com.aditya.nexora.profileService.entity.*;
import com.aditya.nexora.profileService.services.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    // ==========================================
    // GITHUB OAUTH & SYNC ENDPOINTS
    // ==========================================

    @PostMapping("/sources/github/connect")
    public ResponseEntity<ConnectedSource> connectGithub(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid GithubConnectRequestDTO request) {
        Long userId = jwt.getClaim("userId");
        ConnectedSource source = profileService.connectGithub(userId, request.code());
        return ResponseEntity.ok(source);
    }

    @PostMapping("/sources/github/sync")
    public ResponseEntity<Map<String, String>> syncRepositories(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        profileService.syncGithubRepositories(userId);
        return ResponseEntity.ok(Map.of("message", "GitHub repositories synchronized successfully"));
    }

    // ==========================================
    // WORK EXPERIENCE CRUD
    // ==========================================

    @GetMapping("/experiences")
    public ResponseEntity<List<Experience>> getExperiences(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(profileService.getExperiences(userId));
    }

    @PostMapping("/experiences")
    public ResponseEntity<Experience> addExperience(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid ExperienceRequestDTO request) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(profileService.addExperience(userId, request));
    }

    @PutMapping("/experiences/{experienceId}")
    public ResponseEntity<Experience> updateExperience(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long experienceId,
            @RequestBody @Valid ExperienceRequestDTO request) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(profileService.updateExperience(userId, experienceId, request));
    }

    @DeleteMapping("/experiences/{experienceId}")
    public ResponseEntity<Map<String, String>> deleteExperience(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long experienceId) {
        Long userId = jwt.getClaim("userId");
        profileService.deleteExperience(userId, experienceId);
        return ResponseEntity.ok(Map.of("message", "Experience deleted successfully"));
    }

    // ==========================================
    // EDUCATION CRUD
    // ==========================================

    @GetMapping("/educations")
    public ResponseEntity<List<Education>> getEducations(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(profileService.getEducations(userId));
    }

    @PostMapping("/educations")
    public ResponseEntity<Education> addEducation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid EducationRequestDTO request) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(profileService.addEducation(userId, request));
    }

    @PutMapping("/educations/{educationId}")
    public ResponseEntity<Education> updateEducation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long educationId,
            @RequestBody @Valid EducationRequestDTO request) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(profileService.updateEducation(userId, educationId, request));
    }

    @DeleteMapping("/educations/{educationId}")
    public ResponseEntity<Map<String, String>> deleteEducation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long educationId) {
        Long userId = jwt.getClaim("userId");
        profileService.deleteEducation(userId, educationId);
        return ResponseEntity.ok(Map.of("message", "Education deleted successfully"));
    }

    // ==========================================
    // CERTIFICATION CRUD
    // ==========================================

    @GetMapping("/certifications")
    public ResponseEntity<List<Certification>> getCertifications(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(profileService.getCertifications(userId));
    }

    @PostMapping("/certifications")
    public ResponseEntity<Certification> addCertification(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid CertificationRequestDTO request) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(profileService.addCertification(userId, request));
    }

    @PutMapping("/certifications/{certificationId}")
    public ResponseEntity<Certification> updateCertification(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long certificationId,
            @RequestBody @Valid CertificationRequestDTO request) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(profileService.updateCertification(userId, certificationId, request));
    }

    @DeleteMapping("/certifications/{certificationId}")
    public ResponseEntity<Map<String, String>> deleteCertification(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long certificationId) {
        Long userId = jwt.getClaim("userId");
        profileService.deleteCertification(userId, certificationId);
        return ResponseEntity.ok(Map.of("message", "Certification deleted successfully"));
    }

    // ==========================================
    // AI-VERIFIED SKILLS (DERIVED CLAIMS)
    // ==========================================

    @GetMapping("/claims")
    public ResponseEntity<List<DerivedClaim>> getDerivedClaims(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(profileService.getDerivedClaims(userId));
    }

    @PutMapping("/claims/{claimId}/approval")
    public ResponseEntity<DerivedClaim> updateClaimApproval(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long claimId,
            @RequestBody @Valid ClaimApprovalRequestDTO request) {
        Long userId = jwt.getClaim("userId");
        DerivedClaim claim = profileService.updateClaimApproval(userId, claimId, request.approvalState());
        return ResponseEntity.ok(claim);
    }


    @GetMapping("/me")
    public ResponseEntity<DeveloperProfileDTO> getMyProfile(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        return ResponseEntity.ok(profileService.getDeveloperProfile(userId));
    }
}
