package com.aditya.nexora.profileService.services;

import com.aditya.nexora.profileService.client.UserClient;
import com.aditya.nexora.profileService.dtos.*;
import com.aditya.nexora.profileService.entity.*;
import com.aditya.nexora.profileService.enums.AnalysisStatus;
import com.aditya.nexora.profileService.enums.ApprovalState;
import com.aditya.nexora.profileService.enums.OwnershipStatus;
import com.aditya.nexora.profileService.enums.SourceProvider;
import com.aditya.nexora.profileService.exception.ApiResponse;
import com.aditya.nexora.profileService.exception.BadRequestException;
import com.aditya.nexora.profileService.exception.ForbiddenException;
import com.aditya.nexora.profileService.exception.ResourceNotFoundException;
import com.aditya.nexora.profileService.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class ProfileServiceImpl implements ProfileService{

    private final UserClient userClient;
    private final ConnectedSourceRepository connectedSourceRepository;
    private final ProjectRepository projectRepository;
    private final GithubService githubService;
    private final ExperienceRepository experienceRepository;
    private final EducationRepository educationRepository;
    private final DerivedClaimRepository derivedClaimRepository;
    private final CertificationRepository certificationRepository;

    public ProfileServiceImpl(UserClient userClient, ConnectedSourceRepository connectedSourceRepository, ProjectRepository projectRepository, GithubService githubService, ExperienceRepository experienceRepository, EducationRepository educationRepository, DerivedClaimRepository derivedClaimRepository, CertificationRepository certificationRepository) {
        this.userClient = userClient;
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
    public ConnectedSourceDTO connectGithub(Long userId, String authorizationCode) {

        String accessToken = githubService.exchangeCodeForAccessToken(authorizationCode);

        if(accessToken==null)
        {
            log.error("Failed to exchange code for access token");
            throw new BadRequestException("Failed to exchange code for access token");
        }
        GitHubProfileDTO gitHubProfileDTO = githubService.fetchProfile(accessToken);

        ConnectedSource connectedSource = connectedSourceRepository.findByUserIdAndProvider(userId, SourceProvider.GITHUB).orElse(null);


        if(connectedSource!=null)
        {
            connectedSource.setAccessToken(accessToken);
            connectedSource.setProviderUserId(gitHubProfileDTO.id());
            connectedSource.setGithubUsername(gitHubProfileDTO.username ());
            connectedSource.setOwnershipStatus(OwnershipStatus.OWNERSHIP_VERIFIED);
            connectedSource.setConnectedAt(Instant.now());
            ConnectedSource saved = connectedSourceRepository.save(connectedSource);
            return new ConnectedSourceDTO(
                    saved.getProvider().name(),
                    saved.getGithubUsername(),
                    saved.getOwnershipStatus().name(),
                    saved.getConnectedAt()
            );
        }
        ConnectedSource connectedSourceNew = ConnectedSource.builder()
                .userId(userId)
                .provider(SourceProvider.GITHUB)
                .providerUserId(gitHubProfileDTO.id())
                .githubUsername(gitHubProfileDTO.username())
                .accessToken(accessToken)
                .ownershipStatus(OwnershipStatus.OWNERSHIP_VERIFIED)
                .connectedAt(Instant.now())
                .build();


        ConnectedSource saved = connectedSourceRepository.save(connectedSourceNew);
        return new ConnectedSourceDTO(
                saved.getProvider().name(),
                saved.getGithubUsername(),
                saved.getOwnershipStatus().name(),
                saved.getConnectedAt()
        );


    }

    @Override
    public void disconnectGithub(Long userId) {

        ConnectedSource connectedSource = connectedSourceRepository.findByUserIdAndProvider(userId, SourceProvider.GITHUB).orElse(null);
        if(connectedSource==null)
        {
            log.error("Github Connection not found for id: {}", userId);
            throw new ResourceNotFoundException("Github Connection not found for id: " + userId);
        }
        connectedSourceRepository.delete(connectedSource);
        log.info("Github Connection deleted successfully for id: {}", userId);
    }


    @Transactional
    @Override
    public void syncGithubRepositories(Long userId) {
        ConnectedSource connectedSource = connectedSourceRepository.findByUserIdAndProvider(userId, SourceProvider.GITHUB)
                .orElseThrow(() -> new ResourceNotFoundException("Connected source not found for user id: " + userId));

        List<RepositoryDTO> repoList = githubService.fetchRepositories(connectedSource.getAccessToken());
        List<Project> currentProjects = projectRepository.findByUserId(userId);

        Map<Long, Project> existingProjects = currentProjects.stream()
                .filter(p -> p.getProviderRepositoryId() != null)
                .collect(java.util.stream.Collectors.toMap(Project::getProviderRepositoryId, p -> p));

        List<Project> toSave = new ArrayList<>();
        Set<Long> syncedRepoIds = new HashSet<>();

        for (RepositoryDTO repoDTO : repoList) {
            syncedRepoIds.add(repoDTO.id());
            Project project = existingProjects.get(repoDTO.id());

            if (project != null) {
                // Update metadata fields without overwriting user-authored fields (liveUrl, statedRole, description, etc.)
                project.setTitle(repoDTO.name());
                project.setRepoUrl(repoDTO.htmlUrl());
                project.setGithubDescription(repoDTO.description());
                project.setFork(repoDTO.isFork());
                project.setArchived(repoDTO.isArchived());
                project.setDefaultBranch(repoDTO.defaultBranch());
                project.setOwnerLogin(repoDTO.ownerLogin());
                project.setFullName(repoDTO.fullName());
                project.setStars(repoDTO.stars());
                project.setForks(repoDTO.forks());
                project.setRepoCreatedAt(repoDTO.createdAt());
                project.setRepoUpdatedAt(repoDTO.updatedAt());
                project.setPushedAt(repoDTO.pushedAt());
                project.setUpdatedAt(Instant.now());
                project.setRepositoryVisibility(repoDTO.visibility());

                // Update techStack by merging new GitHub topics/languages
                List<String> currentStack = project.getTechStack();
                List<String> updatedStack = currentStack != null ? new ArrayList<>(currentStack) : new ArrayList<>();
                if (repoDTO.primaryLanguage() != null && updatedStack.stream().noneMatch(t -> t.equalsIgnoreCase(repoDTO.primaryLanguage()))) {
                    updatedStack.add(repoDTO.primaryLanguage());
                }
                if (repoDTO.topics() != null) {
                    for (String topic : repoDTO.topics()) {
                        final String currentTopic = topic;
                        if (updatedStack.stream().noneMatch(t -> t.equalsIgnoreCase(currentTopic))) {
                            updatedStack.add(topic);
                        }
                    }
                }
                project.setTechStack(updatedStack);

                toSave.add(project);
            } else {
                List<String> techStack = new ArrayList<>();
                if (repoDTO.primaryLanguage() != null) {
                    techStack.add(repoDTO.primaryLanguage());
                }
                if (repoDTO.topics() != null) {
                    for (String topic : repoDTO.topics()) {
                        final String currentTopic = topic;
                        if (techStack.stream().noneMatch(t -> t.equalsIgnoreCase(currentTopic))) {
                            techStack.add(topic);
                        }
                    }
                }

                // Insert new projects - by default isVisible is false, and analysisStatus is PENDING
                Project newProject = Project.builder()
                        .userId(userId)
                        .connectedSource(connectedSource)
                        .providerRepositoryId(repoDTO.id())
                        .title(repoDTO.name())
                        .repoUrl(repoDTO.htmlUrl())
                        .githubDescription(repoDTO.description())
                        .techStack(techStack)
                        .isFork(repoDTO.isFork())
                        .isArchived(repoDTO.isArchived())
                        .defaultBranch(repoDTO.defaultBranch())
                        .ownerLogin(repoDTO.ownerLogin())
                        .fullName(repoDTO.fullName())
                        .stars(repoDTO.stars())
                        .forks(repoDTO.forks())
                        .repoCreatedAt(repoDTO.createdAt())
                        .repoUpdatedAt(repoDTO.updatedAt())
                        .pushedAt(repoDTO.pushedAt())
                        .isVisible(false) // Default to invisible until user chooses to publish
                        .analysisStatus(AnalysisStatus.PENDING)
                        .repositoryVisibility(repoDTO.visibility())
                        .build();

                toSave.add(newProject);
            }
        }

        // Handle archiving of projects that disappeared from GitHub (meaning they were deleted or renamed)
        for (Project currentProject : currentProjects) {
            if (currentProject.getProviderRepositoryId() != null && !syncedRepoIds.contains(currentProject.getProviderRepositoryId())) {
                currentProject.setArchived(true);
                currentProject.setVisible(false); // Make invisible if no longer present
                currentProject.setUpdatedAt(Instant.now());
                toSave.add(currentProject);
            }
        }

        projectRepository.saveAll(toSave);

        // Update connected source last sync time
        connectedSource.setLastSyncTime(Instant.now());
        connectedSourceRepository.save(connectedSource);
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
    public List<Project> getProjects(Long userId)
    {
        List<Project> projectList = projectRepository.findByUserId(userId);
        return projectList;
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
    @Override
    public DeveloperProfileDTO getDeveloperProfile(Long userId) {

        ApiResponse<UserResponseDTO> response = userClient.getUserById(userId);
        UserResponseDTO userDetails = response.data();


        List<Experience> experiences = getExperiences(userId);
        List<Education> educations = getEducations(userId);
        List<Certification> certifications = getCertifications(userId);
        List<DerivedClaim> claims = getDerivedClaims(userId);


        return new DeveloperProfileDTO(
                userDetails,
                experiences,
                educations,
                certifications,
                claims
        );
    }

    @Override
    public PublicProfileDTO getPublicProfile(String username) {

        ApiResponse<UserResponseDTO> response = userClient.getUserByUsername(username);
        if (response == null || response.data() == null) {
            throw new ResourceNotFoundException("User not found with username: " + username);
        }
        UserResponseDTO userDetails = response.data();
        Long userId = userDetails.id();

        List<Project> projects = getProjectsByUserId(userId);
        List<Experience> experiences = getExperiences(userId);
        List<Education> educations = getEducations(userId);
        List<Certification> certifications = getCertifications(userId);
        List<DerivedClaim> allClaims = getDerivedClaims(userId);

        List<PublicExperienceDTO> publicExperiences = experiences.stream()
                .map(exp -> new PublicExperienceDTO(
                        exp.getTitle(),
                        exp.getCompany(),
                        exp.getLocation(),
                        exp.getStartDate(),
                        exp.getEndDate(),
                        exp.isCurrent(),
                        exp.getDescription()
                ))
                .toList();

        List<PublicEducationDTO> publicEducations = educations.stream()
                .map(edu -> new PublicEducationDTO(
                        edu.getSchool(),
                        edu.getDegree(),
                        edu.getFieldOfStudy(),
                        edu.getStartDate(),
                        edu.getEndDate()
                ))
                .toList();

        List<PublicCertificationDTO> publicCertifications = certifications.stream()
                .map(cert -> new PublicCertificationDTO(
                        cert.getName(),
                        cert.getIssuingOrg(),
                        cert.getIssueDate(),
                        cert.getExpirationDate(),
                        cert.getCredentialId(),
                        cert.getCredentialUrl()
                ))
                .toList();

        List<PublicClaimDTO> approvedClaims = allClaims.stream()
                .filter(claim -> claim.getApprovalState() == ApprovalState.APPROVED)
                .map(claim -> new PublicClaimDTO(
                        claim.getClaimType(),
                        claim.getClaimValue(),
                        claim.getExplanation(),
                        claim.getConfidence()
                ))
                .toList();

        List<PublicProjectDTO> publicProjects = projects.stream()
                .filter(project -> project.isVisible()==true && project.getRepositoryVisibility().equals("PUBLIC"))
                .map(project -> new PublicProjectDTO(
                        project.getTitle(),
                        project.getDescription()==null?project.getGithubDescription():project.getDescription(),
                        project.getRepoUrl(),
                        project.getLiveUrl(),
                        project.getTechStack(),
                        project.getStatedRole(),
                        project.getContributionSummary(),
                        project.getAiSummary(),
                        project.getArchitectureSummary(),
                        "",
                        project.getUpdatedAt()


                )).toList();


        return new PublicProfileDTO(
                userDetails.username(),
                userDetails.name(),
                userDetails.headLine(),
                userDetails.about(),
                userDetails.profilePictureUrl(),
                publicProjects,
                publicExperiences,
                publicEducations,
                publicCertifications,
                approvedClaims
        );
    }

    @Override
    public List<Project> getProjectsByUserId(Long userId) {
        return projectRepository.findByUserId(userId);
    }


    @Transactional
    @Override
    public Project updateProjectVisibility(Long userId, Long projectId, boolean isVisible) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found for id: " + projectId));
        if (!project.getUserId().equals(userId)) {
            log.error("User is not authorized to update project visibility for id: {}", projectId);
            throw new ForbiddenException("You are not authorized to update this project");
        }
        if(isVisible==true && project.getRepositoryVisibility().equals("PRIVATE"))
        {
            throw new BadRequestException("Private projects cannot be published publicly.");
        }
        project.setVisible(isVisible);
        return projectRepository.save(project);

    }


    @Transactional
    @Override
    public Project updateProject(Long userId, Long projectId, ProjectUpdateRequestDTO request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found for id: " + projectId));

        if(!project.getUserId().equals(userId))
        {
            log.error("User is not authorized to update project for id: {}", projectId);
            throw new ForbiddenException("You are not authorized to update this project");
        }

        project.setTitle(request.title());
        project.setDescription(request.description());
        project.setStatedRole(request.statedRole());
        project.setContributionSummary(request.contributionSummary());
        project.setLiveUrl(request.liveUrl());
        project.setUpdatedAt(Instant.now());

        return projectRepository.save(project);
    }


}
