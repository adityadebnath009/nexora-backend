package com.aditya.nexora.profileService.services;

import com.aditya.nexora.profileService.dtos.CertificationRequestDTO;
import com.aditya.nexora.profileService.dtos.EducationRequestDTO;
import com.aditya.nexora.profileService.dtos.ExperienceRequestDTO;
import com.aditya.nexora.profileService.entity.*;

import java.util.List;

public interface ProfileService {
    ConnectedSource connectGithub(Long userId, String authorizationCode);
    void syncGithubRepositories(Long userId);


    // Work Experience CRUD
    List<Experience> getExperiences(Long userId);
    Experience addExperience(Long userId, ExperienceRequestDTO request);
    Experience updateExperience(Long userId, Long experienceId, ExperienceRequestDTO request);
    void deleteExperience(Long userId, Long experienceId);

    // Education CRUD
    List<Education> getEducations(Long userId);
    Education addEducation(Long userId, EducationRequestDTO request);
    Education updateEducation(Long userId, Long educationId, EducationRequestDTO request);
    void deleteEducation(Long userId, Long educationId);

    // Certification CRUD
    List<Certification> getCertifications(Long userId);
    Certification addCertification(Long userId, CertificationRequestDTO request);
    Certification updateCertification(Long userId, Long certificationId, CertificationRequestDTO request);
    void deleteCertification(Long userId, Long certificationId);

    // Derived Claims (AI Skills)
    List<DerivedClaim> getDerivedClaims(Long userId);
    DerivedClaim updateClaimApproval(Long userId, Long claimId, String approvalState);


}
