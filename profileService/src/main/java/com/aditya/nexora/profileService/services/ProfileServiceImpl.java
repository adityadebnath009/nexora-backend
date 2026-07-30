package com.aditya.nexora.profileService.services;

import com.aditya.nexora.profileService.dtos.GitHubProfileDTO;
import com.aditya.nexora.profileService.dtos.RepositoryDTO;
import com.aditya.nexora.profileService.entity.ConnectedSource;
import com.aditya.nexora.profileService.entity.Project;
import com.aditya.nexora.profileService.enums.OwnershipStatus;
import com.aditya.nexora.profileService.enums.SourceProvider;
import com.aditya.nexora.profileService.exception.ResourceNotFoundException;
import com.aditya.nexora.profileService.repository.ConnectedSourceRepository;
import com.aditya.nexora.profileService.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ProfileServiceImpl implements ProfileService{

    private final ConnectedSourceRepository connectedSourceRepository;
    private final ProjectRepository projectRepository;
    private final GithubService githubService;

    public ProfileServiceImpl(ConnectedSourceRepository connectedSourceRepository, ProjectRepository projectRepository, GithubService githubService) {
        this.connectedSourceRepository = connectedSourceRepository;
        this.projectRepository = projectRepository;
        this.githubService = githubService;
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
}
