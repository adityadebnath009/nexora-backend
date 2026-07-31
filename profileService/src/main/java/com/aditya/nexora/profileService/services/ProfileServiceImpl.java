package com.aditya.nexora.profileService.services;

import com.aditya.nexora.profileService.dtos.*;
import com.aditya.nexora.profileService.entity.*;
import com.aditya.nexora.profileService.enums.ApprovalState;
import com.aditya.nexora.profileService.enums.OwnershipStatus;
import com.aditya.nexora.profileService.enums.SourceProvider;
import com.aditya.nexora.profileService.exception.BadRequestException;
import com.aditya.nexora.profileService.exception.ForbiddenException;
import com.aditya.nexora.profileService.exception.ResourceNotFoundException;
import com.aditya.nexora.profileService.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProfileServiceImpl implements ProfileService{

    private final ConnectedSourceRepository connectedSourceRepository;
    private final ProjectRepository projectRepository;
    private final GithubService githubService;
    private final ExperienceRepository experienceRepository;
    private final EducationRepository educationRepository;
    private final DerivedClaimRepository derivedClaimRepository;
    private final CertificationRepository certificationRepository;

    public ProfileServiceImpl(ConnectedSourceRepository connectedSourceRepository, ProjectRepository projectRepository, GithubService githubService, ExperienceRepository experienceRepository, EducationRepository educationRepository, DerivedClaimRepository derivedClaimRepository, CertificationRepository certificationRepository) {
        this.connectedSourceRepository = connectedSourceRepository;
        this.projectRepository = projectRepository;
        this.githubService = githubService;
        this.experienceRepository = experienceRepository;
        this.educationRepository = educationRepository;
        this.derivedClaimRepository = derivedClaimRepository;
        this.certificationRepository = certificationRepository;
    }


    @Transactional
    @Override
    public ConnectedSource connectGithub(Long userId, String authorizationCode) {
        String accessToken = githubService.exchangeCodeForAccessToken(authorizationCode);
        if(accessToken == null)
        {
            log.error("Failed to exchange code for access token");
            return null;
        }
        GitHubProfileDTO   gitHubProfileDTO =  githubService.fetchProfile(accessToken);
        ConnectedSource connectedSource = connectedSourceRepository.findByUserIdAndProvider(userId, SourceProvider.GITHUB).orElse(null);

        if(connectedSource!=null)
        {
            connectedSource.setAccessToken(accessToken);
            connectedSource.setProviderUserId(gitHubProfileDTO.id());
            connectedSource.setOwnershipStatus(OwnershipStatus.OWNERSHIP_VERIFIED);
            return connectedSourceRepository.save(connectedSource);
        }
        ConnectedSource connectedSourceNew = ConnectedSource.builder()
                .userId(userId)
                .provider(SourceProvider.GITHUB)
                .providerUserId(gitHubProfileDTO.id())
                .accessToken(accessToken)
                .ownershipStatus(OwnershipStatus.OWNERSHIP_VERIFIED)
                .build();

        connectedSourceRepository.save(connectedSourceNew);
        return connectedSourceNew;

    }


    @Transactional
    @Override
    public void syncGithubRepositories(Long userId) {

        ConnectedSource connectedSource = connectedSourceRepository.findByUserIdAndProvider(userId, SourceProvider.GITHUB).orElse(null);
        if(connectedSource==null)
        {
            log.error("Connected source not found for user id: {}", userId);
            throw new ResourceNotFoundException("Connected source not found for user id: " + userId);

        }
        List<RepositoryDTO> repoList = githubService.fetchRepositories(connectedSource.getAccessToken());
        List<Project>  currentProjects = projectRepository.findByUserId(userId);

        Map<Long, Project> existingProject = currentProjects.stream().
                collect(java.util.stream.Collectors.
                        toMap(Project::getProviderRepositoryId, project -> project));

        List<Project> toSave = new ArrayList<>();
        for(RepositoryDTO repoDTO: repoList)
        {
            Project project = existingProject.get(repoDTO.id());
            if(project!=null)
            {
                project.setTitle(repoDTO.name());
                project.setRepoUrl(repoDTO.htmlUrl());
                project.setVisible(true);
                project.setUpdatedAt(Instant.now());

                toSave.add(project);


            }
            else
            {
                Project newProject = Project.builder().
                        userId(userId).
                        connectedSource(connectedSource).
                        providerRepositoryId(repoDTO.id()).
                        title(repoDTO.name()).
                        repoUrl(repoDTO.htmlUrl())
                        .techStack(repoDTO.primaryLanguage()!=null ? List.of(repoDTO.primaryLanguage()) : List.of())
                        .isVisible(true)
                        .build();

                toSave.add(newProject);
            }

        }
        projectRepository.saveAll(toSave);
    }

    @Transactional
    @Override
    public List<Experience> getExperiences(Long userId) {
        List<Experience> experienceList = experienceRepository.findByUserIdOrderByStartDateDesc(userId);
        return experienceList;
    }


    @Transactional
    @Override
    public Experience addExperience(Long userId, ExperienceRequestDTO request) {
        log.info("Adding experience for user id: {}", userId);
        Experience experience = Experience.builder().
                userId(userId).
                title(request.title()).
                company(request.company()).
                location(request.location()).
                startDate(request.startDate()).
                endDate(request.endDate()).
                isCurrent(request.isCurrent()).
                description(request.description()).
                build();
        return experienceRepository.save(experience);
    }


    @Transactional
    @Override
    public Experience updateExperience(Long userId, Long experienceId, ExperienceRequestDTO request) {
        log.info("Updating experience for user id: {}", userId);
        Experience experience = experienceRepository.
                findById(experienceId).
                orElseThrow(() -> new ResourceNotFoundException("Experience not found for id: " + experienceId));

        if(!experience.getUserId().equals(userId))
        {
            log.error("User is not authorized to update experience for id: {}", experienceId);
            throw new ForbiddenException("You are not authorized to update this experience");
        }
        experience.setTitle(request.title());
        experience.setCompany(request.company());
        experience.setLocation(request.location());
        experience.setStartDate(request.startDate());
        experience.setEndDate(request.endDate());
        experience.setDescription(request.description());
        log.info("Experience updated successfully for id: {}", experienceId);
        return experienceRepository.save(experience);
    }


    @Transactional
    @Override
    public void deleteExperience(Long userId, Long experienceId) {
        Experience experience = experienceRepository.findById(experienceId).orElseThrow(() -> new ResourceNotFoundException("Experience not found for id: " + experienceId));
        if(!experience.getUserId().equals(userId))
        {
            log.error("User is not authorized to delete experience for id: {}", experienceId);
            throw new ForbiddenException("You are not authorized to delete this experience");
        }
        log.info("Experience deleted successfully for id: {}", experienceId);
        experienceRepository.deleteById(experienceId);

    }


    @Transactional
    @Override
    public List<Education> getEducations(Long userId) {

        List<Education> educationList = educationRepository.findByUserIdOrderByStartDateDesc(userId);
        return educationList;
    }


    @Transactional
    @Override
    public Education addEducation(Long userId, EducationRequestDTO request) {
        log.info("Adding education for user id: {}", userId);

        Education education = Education.builder().
                userId(userId).
                school(request.school()).
                degree(request.degree()).
                fieldOfStudy(request.fieldOfStudy()).
                startDate(request.startDate()).
                endDate(request.endDate()).
                build();

        return educationRepository.save(education);
    }

    @Transactional
    @Override
    public Education updateEducation(Long userId, Long educationId, EducationRequestDTO request) {
        Education education = educationRepository
                .findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found for id: " + educationId));
        if(!education.getUserId().equals(userId))
        {
            log.error("User is not authorized to update education for id: {}", educationId);
            throw new ForbiddenException("You are not authorized to update this education");
        }

        education.setSchool(request.school());
        education.setDegree(request.degree());
        education.setFieldOfStudy(request.fieldOfStudy());
        education.setStartDate(request.startDate());
        education.setEndDate(request.endDate());
        return educationRepository.save(education);

    }


    @Transactional
    @Override
    public void deleteEducation(Long userId, Long educationId) {

        Education education = educationRepository.findById(educationId).orElseThrow(() -> new ResourceNotFoundException("Education not found for id: " + educationId));
        if(!education.getUserId().equals(userId))
        {
            log.error("User is not authorized to delete education for id: {}", educationId);
            throw new ForbiddenException("You are not authorized to delete this education");
        }
        log.info("Education deleted successfully for id: {}", educationId);
        educationRepository.deleteById(educationId);
    }


    @Transactional
    @Override
    public List<Certification> getCertifications(Long userId) {
        List<Certification> certificationList = certificationRepository.findByUserIdOrderByIssueDateDesc(userId);
        return certificationList;
    }


    @Transactional
    @Override
    public Certification addCertification(Long userId, CertificationRequestDTO request) {
        Certification  certification = Certification.builder()
                .userId(userId)
                .name(request.name())
                .issuingOrg(request.issuingOrg())
                .issueDate(request.issueDate())
                .expirationDate(request.expirationDate())
                .credentialId(request.credentialId())
                .credentialUrl(request.credentialUrl())
                .build()
        ;
        log.info("Adding certification for user id: {}", userId);
        return certificationRepository.save(certification);

    }

    @Transactional
    @Override
    public Certification updateCertification(Long userId, Long certificationId, CertificationRequestDTO request) {
        Certification certification = certificationRepository.findById(certificationId).orElseThrow(() -> new ResourceNotFoundException("Certification not found for id: " + certificationId));
        if(!certification.getUserId().equals(userId))
        {
            log.error("User is not authorized to update certification for id: {}", certificationId);
            throw new ForbiddenException("You are not authorized to update this certification");
        }

        certification.setName(request.name());
        certification.setIssuingOrg(request.issuingOrg());
        certification.setIssueDate(request.issueDate());
        certification.setExpirationDate(request.expirationDate());
        certification.setCredentialId(request.credentialId());
        certification.setCredentialUrl(request.credentialUrl());
        return certificationRepository.save(certification);

    }


    @Transactional
    @Override
    public void deleteCertification(Long userId, Long certificationId) {
        Certification certification = certificationRepository.findById(certificationId).orElseThrow(() -> new ResourceNotFoundException("Certification not found for id: " + certificationId));
        if(!certification.getUserId().equals(userId))
        {
            log.error("User is not authorized to delete certification for id: {}", certificationId);
            throw new ForbiddenException("You are not authorized to delete this certification");
        }
        log.info("Certification deleted successfully for id: {}", certificationId);
        certificationRepository.deleteById(certificationId);

    }


    @Transactional(readOnly = true)
    @Override
    public List<DerivedClaim> getDerivedClaims(Long userId) {
        return derivedClaimRepository.findByUserId(userId);
    }

    @Transactional
    @Override
    public DerivedClaim updateClaimApproval(Long userId, Long claimId, String approvalState) {
        DerivedClaim claim = derivedClaimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Derived claim not found"));
        if (!claim.getUserId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to update this claim");
        }
        try {
            ApprovalState state = ApprovalState.valueOf(approvalState.toUpperCase());
            claim.setApprovalState(state);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid approval state: " + approvalState);
        }
        return derivedClaimRepository.save(claim);
    }
}
