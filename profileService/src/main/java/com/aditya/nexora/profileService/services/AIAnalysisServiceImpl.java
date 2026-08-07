package com.aditya.nexora.profileService.services;


import com.aditya.nexora.profileService.entity.Project;
import com.aditya.nexora.profileService.enums.AnalysisStatus;
import com.aditya.nexora.profileService.exception.ResourceNotFoundException;
import com.aditya.nexora.profileService.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Array;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
public class AIAnalysisServiceImpl implements AIAnalysisService{

    private final GithubService githubService;
    private final ProjectRepository projectRepository;
    public AIAnalysisServiceImpl(GithubService githubService, ProjectRepository projectRepository) {
        this.githubService = githubService;
        this.projectRepository = projectRepository;
    }

    @Transactional
    @Async
    @Override
    public void analyzeProjectAsync(Long projectId, String accessToken) {

        Project project = projectRepository
                .findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found for id: " + projectId));



        try{
            project.setAnalysisStatus(AnalysisStatus.PROCESSING);
            githubService.fetchRepositoryTree(project.getOwnerLogin(),project.getTitle(),project.getDefaultBranch(), accessToken);
            project.setAiSummary("AI summary generated successfully");
            project.setAnalysisStatus(AnalysisStatus.COMPLETED);
            project.setAnalyzedAt(Instant.now());
            project.setAnalysisVersion("v1.0");

            projectRepository.save(project);
        }
        catch(Exception e)
        {
            project.setAnalysisStatus(AnalysisStatus.FAILED);
            projectRepository.save(project);
            log.error("Error analyzing project: {}", e.getMessage());
            throw new ResourceNotFoundException("Error analyzing project: " + e.getMessage());
        }
    }
}
