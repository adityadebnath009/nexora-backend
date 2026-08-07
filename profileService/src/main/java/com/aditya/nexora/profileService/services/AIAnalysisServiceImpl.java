package com.aditya.nexora.profileService.services;


import com.aditya.nexora.profileService.dtos.GitHubTreeItemDTO;
import com.aditya.nexora.profileService.entity.EvidenceItem;
import com.aditya.nexora.profileService.entity.Project;
import com.aditya.nexora.profileService.enums.AnalysisStatus;
import com.aditya.nexora.profileService.enums.ClaimType;
import com.aditya.nexora.profileService.enums.ConfidenceLevel;
import com.aditya.nexora.profileService.enums.EvidenceType;
import com.aditya.nexora.profileService.exception.ResourceNotFoundException;
import com.aditya.nexora.profileService.repository.DerivedClaimRepository;
import com.aditya.nexora.profileService.repository.EvidenceItemRepository;
import com.aditya.nexora.profileService.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Array;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class AIAnalysisServiceImpl implements AIAnalysisService{

    private static final Set<String> TARGET_FILES = Set.of(
            "readme.md", "readme.txt",
            "pom.xml", "build.gradle", "build.gradle.kts",
            "package.json", "tsconfig.json", "next.config.js", "next.config.mjs", "vite.config.js", "vite.config.ts",
            "go.mod", "cargo.toml", "gemfile", "composer.json",
            "requirements.txt", "pyproject.toml", "pipfile", "setup.py",
            "appsettings.json",
            "dockerfile", "docker-compose.yml", "docker-compose.yaml",
            "jenkinsfile", ".gitlab-ci.yml", "schema.sql"
    );

    private final GithubService githubService;
    private final ProjectRepository projectRepository;
    private final EvidenceItemRepository evidenceItemRepository;
    private final DerivedClaimRepository derivedClaimRepository;
    private final RestClient restClient;
    @Value("${gemini.api-key}")
    private String geminiApiKey;
    public AIAnalysisServiceImpl(GithubService githubService, ProjectRepository projectRepository, EvidenceItemRepository evidenceItemRepository, DerivedClaimRepository derivedClaimRepository, RestClient restClient) {
        this.githubService = githubService;
        this.projectRepository = projectRepository;
        this.evidenceItemRepository = evidenceItemRepository;
        this.derivedClaimRepository = derivedClaimRepository;
        this.restClient = restClient;
    }



    private boolean isTargetFile(String path) {
        if (path == null) return false;
        String lowerPath = path.toLowerCase();

        // Extract filename from the path
        String fileName = lowerPath.substring(lowerPath.lastIndexOf('/') + 1);
        if (fileName.startsWith(".github/workflows/") && (fileName.endsWith(".yml") || fileName.endsWith(".yaml"))) {
            return true;
        }
        if (lowerPath.startsWith("docs/") && (fileName.endsWith(".md") || fileName.endsWith(".txt"))) {
            return true;
        }
        if (fileName.endsWith(".csproj") || fileName.endsWith(".fsproj")) {
            return true;
        }
        if (TARGET_FILES.contains(fileName)) {
            return true;
        }
        return false;
    }

    private ConfidenceLevel parseConfidenceLevel(String level) {
        if (level == null) return ConfidenceLevel.MEDIUM;
        try {
            return ConfidenceLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid confidence level from AI: {}, defaulting to MEDIUM", level);
            return ConfidenceLevel.MEDIUM;
        }
    }

    private EvidenceType parseEvidenceType(String type, String sourcePath) {
        if (type != null) {
            try {
                return EvidenceType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid evidence type: {}, guessing from path: {}", type, sourcePath);
            }
        }
        if (sourcePath == null) return EvidenceType.DEPENDENCY_FILE;
        String lowerPath = sourcePath.toLowerCase();
        if (lowerPath.endsWith("readme.md") || lowerPath.endsWith("readme.txt")) {
            return EvidenceType.README;
        }
        if (lowerPath.contains("dockerfile") || lowerPath.contains("docker-compose")) {
            return EvidenceType.DOCKER_CONFIGURATION;
        }
        if (lowerPath.contains(".github/workflows/") || lowerPath.contains("jenkinsfile") || lowerPath.contains(".gitlab-ci.yml")) {
            return EvidenceType.CI_WORKFLOW;
        }
        return EvidenceType.DEPENDENCY_FILE;
    }

    private ClaimType parseClaimType(String type) {
        if (type == null) return ClaimType.FRAMEWORK;
        try {
            return ClaimType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid claim type: {}, defaulting to FRAMEWORK", type);
            return ClaimType.FRAMEWORK;
        }
    }


    @Override
    public void analyzeProjectAsync(Long projectId, String accessToken) {

    }
}