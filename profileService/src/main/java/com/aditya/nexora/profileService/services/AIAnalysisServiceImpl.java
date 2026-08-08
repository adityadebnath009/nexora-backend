package com.aditya.nexora.profileService.services;

import com.aditya.nexora.profileService.dtos.GitHubCommitDTO;
import com.aditya.nexora.profileService.dtos.GitHubPullRequestDTO;
import com.aditya.nexora.profileService.dtos.GitHubTreeItemDTO;
import com.aditya.nexora.profileService.dtos.GeminiAnalysisResponseDTO;
import com.aditya.nexora.profileService.dtos.GeminiClaimDTO;
import com.aditya.nexora.profileService.dtos.GeminiEvidenceDTO;
import com.aditya.nexora.profileService.entity.DerivedClaim;
import com.aditya.nexora.profileService.entity.EvidenceItem;
import com.aditya.nexora.profileService.entity.Project;
import com.aditya.nexora.profileService.enums.AnalysisStatus;
import com.aditya.nexora.profileService.enums.ClaimType;
import com.aditya.nexora.profileService.enums.ConfidenceLevel;
import com.aditya.nexora.profileService.enums.EvidenceType;
import com.aditya.nexora.profileService.exception.ResourceNotFoundException;
import com.aditya.nexora.profileService.entity.ConnectedSource;
import com.aditya.nexora.profileService.enums.SourceProvider;
import com.aditya.nexora.profileService.repository.ConnectedSourceRepository;
import com.aditya.nexora.profileService.repository.DerivedClaimRepository;
import com.aditya.nexora.profileService.repository.EvidenceItemRepository;
import com.aditya.nexora.profileService.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;


@Service
@Slf4j
public class AIAnalysisServiceImpl implements AIAnalysisService {

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
    private final ConnectedSourceRepository connectedSourceRepository;
    private final ChatClient chatClient;

    public AIAnalysisServiceImpl(GithubService githubService,
                                 ProjectRepository projectRepository,
                                 EvidenceItemRepository evidenceItemRepository,
                                 DerivedClaimRepository derivedClaimRepository,
                                 ConnectedSourceRepository connectedSourceRepository,
                                 ChatClient.Builder chatClientBuilder) {
        this.githubService = githubService;
        this.projectRepository = projectRepository;
        this.evidenceItemRepository = evidenceItemRepository;
        this.derivedClaimRepository = derivedClaimRepository;
        this.connectedSourceRepository = connectedSourceRepository;
        this.chatClient = chatClientBuilder.build();
    }
    private boolean isTargetFile(String path) {
        if (path == null) return false;
        String lowerPath = path.toLowerCase();
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
        return TARGET_FILES.contains(fileName);
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

    private Map<String, String> fetchTargetFileContents(Project project, List<GitHubTreeItemDTO> tree, String accessToken) {
        Map<String, String> contents = new HashMap<>();
        if (tree == null) return contents;

        List<GitHubTreeItemDTO> targets = tree.stream()
                .filter(item -> "blob".equals(item.type()) && isTargetFile(item.path()))
                .limit(10)
                .toList();

        for (GitHubTreeItemDTO file : targets) {
            try {
                String content = githubService.fetchFileContent(
                        project.getOwnerLogin(),
                        project.getTitle(),
                        file.sha(),
                        accessToken
                );
                if (content != null && !content.isBlank()) {
                    String truncated = content.length() > 1000 ? content.substring(0, 1000) + "\n...[TRUNCATED]..." : content;
                    contents.put(file.path(), truncated);
                }
            } catch (Exception e) {
                log.warn("Failed to fetch file content for {} in project {}", file.path(), project.getTitle(), e);
            }
        }
        return contents;
    }

    private List<String> extractDependencies(String fileName, String content) {
        if (content == null || content.isBlank()) return List.of();
        List<String> deps = new ArrayList<>();
        String lowerName = fileName.toLowerCase();
        
        try {
            if (lowerName.endsWith("pom.xml")) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<artifactId>(.*?)</artifactId>");
                java.util.regex.Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    String dep = matcher.group(1).trim();
                    if (!deps.contains(dep)) {
                        deps.add(dep);
                    }
                }
            } else if (lowerName.endsWith("package.json")) {
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"([a-zA-Z0-9@/._-]+)\"\\s*:\\s*\"[^\"]+\"");
                java.util.regex.Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    String dep = matcher.group(1).trim();
                    if (!dep.startsWith("^") && !dep.startsWith("~") && !deps.contains(dep)) {
                        deps.add(dep);
                    }
                }
            } else {
                for (String line : content.split("\n")) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("//")) {
                        deps.add(trimmed);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error extracting dependencies from {}", fileName, e);
        }
        return deps;
    }

    private Map<String, String> cleanConfiguration(String fileName, String content) {
        if (content == null || content.isBlank()) return Map.of();
        Map<String, String> safeConfigs = new HashMap<>();
        String lowerName = fileName.toLowerCase();
        
        for (String line : content.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) continue;
            
            if (trimmed.contains("=") && lowerName.endsWith(".properties")) {
                String[] parts = trimmed.split("=", 2);
                if (parts.length > 1) {
                    String key = parts[0].trim();
                    String val = parts[1].trim();
                    if (isSecretKey(key)) {
                        safeConfigs.put(key, "[REDACTED_SECRET]");
                    } else {
                        safeConfigs.put(key, val);
                    }
                }
            } else if (trimmed.contains(":") && (lowerName.endsWith(".yml") || lowerName.endsWith(".yaml") || lowerName.endsWith(".json"))) {
                String[] parts = trimmed.split(":", 2);
                if (parts.length > 1) {
                    String key = parts[0].replaceAll("[\"'{}]", "").trim();
                    String val = parts[1].replaceAll("[\"'{}]", "").trim();
                    if (!val.isEmpty()) {
                        if (isSecretKey(key)) {
                            safeConfigs.put(key, "[REDACTED_SECRET]");
                        } else {
                            safeConfigs.put(key, val);
                        }
                    }
                }
            }
        }
        return safeConfigs;
    }

    private boolean isSecretKey(String key) {
        String lower = key.toLowerCase();
        return lower.contains("pass") // Catches password, pass, passphrase
                || lower.contains("secret")
                || lower.contains("key") // Catches apiKey, client-key
                || lower.contains("token") // Catches access-token, oauth-token
                || lower.contains("credential")
                || lower.contains("private")
                || lower.contains("pwd") // Catches db.pwd
                || lower.contains("salt") // Catches hash salts
                || lower.contains("cert") // Catches certificates
                || lower.contains("sign"); // Catches signature secrets
    }

    private String buildGeminiPrompt(Project project, List<String> treePaths, Map<String, String> fileContents, List<GitHubCommitDTO> commits, List<GitHubPullRequestDTO> pulls, String candidateLogin) {
        // 1. Gather tree metadata
        int totalFiles = treePaths.size();
        long testFilesCount = treePaths.stream()
                .filter(path -> {
                    String lower = path.toLowerCase();
                    return lower.contains("test") || lower.contains("spec");
                })
                .count();
        List<String> workflows = treePaths.stream()
                .filter(path -> path.contains(".github/workflows/"))
                .toList();

        // 2. Parse dependencies ONLY for pom.xml and package.json to save tokens
        List<String> extractedDeps = new ArrayList<>();
        // 3. Keep sanitized raw contents for other configuration files (Dockerfile, requirements.txt, go.mod, etc.)
        StringBuilder rawConfigContents = new StringBuilder();

        fileContents.forEach((fileName, content) -> {
            String lowerName = fileName.toLowerCase();
            if (lowerName.endsWith("pom.xml") || lowerName.endsWith("package.json")) {
                extractedDeps.addAll(extractDependencies(fileName, content));
            } else {
                // Sanitized pass-through
                Map<String, String> cleaned = cleanConfiguration(fileName, content);
                rawConfigContents.append("File: ").append(fileName).append("\nContent:\n");
                if (cleaned.isEmpty()) {
                    // For non-properties files, sanitize line-by-line
                    for (String line : content.split("\n")) {
                        String trimmed = line.trim();
                        if (isSecretKey(trimmed)) {
                            rawConfigContents.append("[REDACTED_SECRET]\n");
                        } else {
                            rawConfigContents.append(line).append("\n");
                        }
                    }
                } else {
                    // For properties/yaml files
                    cleaned.forEach((k, v) -> rawConfigContents.append(k).append("=").append(v).append("\n"));
                }
                rawConfigContents.append("---\n");
            }
        });

        // 4. Git statistics computed against the CANDIDATE (not repo owner)
        int totalCommits = commits != null ? commits.size() : 0;
        long candidateCommits = 0;
        String earliestCommit = "N/A";
        String latestCommit = "N/A";
        long contributionDays = 0;

        if (commits != null && !commits.isEmpty() && candidateLogin != null) {
            String candidate = candidateLogin.trim();
            List<GitHubCommitDTO> candidateList = commits.stream()
                    .filter(c -> c.authorLogin() != null && c.authorLogin().equalsIgnoreCase(candidate))
                    .sorted(Comparator.comparing(GitHubCommitDTO::date))
                    .toList();
            
            candidateCommits = candidateList.size();
            if (!candidateList.isEmpty()) {
                earliestCommit = candidateList.get(0).date().toLocalDate().toString();
                latestCommit = candidateList.get(candidateList.size() - 1).date().toLocalDate().toString();
                contributionDays = java.time.temporal.ChronoUnit.DAYS.between(
                        candidateList.get(0).date(), 
                        candidateList.get(candidateList.size() - 1).date()
                ) + 1;
            }
        }

        StringBuilder recentCommitsText = new StringBuilder();
        if (commits != null) {
            commits.stream().limit(15).forEach(c -> 
                recentCommitsText.append("- Message: ").append(c.message().replace("\n", " "))
                                 .append(" (By: ").append(c.authorLogin() != null ? c.authorLogin() : "unknown").append(")\n")
            );
        }

        int totalPulls = pulls != null ? pulls.size() : 0;
        long candidatePulls = 0;
        long candidateMergedPulls = 0;

        if (pulls != null && !pulls.isEmpty() && candidateLogin != null) {
            String candidate = candidateLogin.trim();
            candidatePulls = pulls.stream()
                    .filter(p -> p.authorLogin() != null && p.authorLogin().equalsIgnoreCase(candidate))
                    .count();
            
            candidateMergedPulls = pulls.stream()
                    .filter(p -> p.authorLogin() != null && p.authorLogin().equalsIgnoreCase(candidate) && p.mergedAt() != null)
                    .count();
        }

        StringBuilder recentPullsText = new StringBuilder();
        if (pulls != null) {
            pulls.stream().limit(10).forEach(p -> 
                recentPullsText.append("- PR: ").append(p.title())
                               .append(" (State: ").append(p.state()).append(", Author: ").append(p.authorLogin() != null ? p.authorLogin() : "unknown").append(")\n")
            );
        }

        // 5. Construct Dossier Prompt
        return "You are an expert technical resume and codebase analyzer.\n"
                + "Analyze the following project engineering dossier (facts, metadata, dependency lists, raw configuration files, and commit summaries) to extract a technical summary, architecture breakdown, skill claims, and evidence items.\n\n"
                + "=== PROJECT DOSSIER ===\n"
                + "Project Name: " + project.getTitle() + "\n"
                + "Metadata:\n"
                + "- Owner/Org: " + project.getOwnerLogin() + "\n"
                + "- Repository: " + project.getFullName() + "\n"
                + "- Default Branch: " + (project.getDefaultBranch() != null ? project.getDefaultBranch() : "main") + "\n"
                + "- Stars: " + (project.getStars() != null ? project.getStars() : 0) + ", Forks: " + (project.getForks() != null ? project.getForks() : 0) + "\n\n"
                + "Structure Statistics:\n"
                + "- Total Repository Files: " + totalFiles + "\n"
                + "- Test Files Found: " + testFilesCount + "\n"
                + "- CI/CD Pipelines: " + (workflows.isEmpty() ? "None" : String.join(", ", workflows)) + "\n\n"
                + "Extracted Big Manifest Dependencies (Java/npm):\n"
                + (extractedDeps.isEmpty() ? "- None\n" : String.join("\n- ", extractedDeps.stream().distinct().limit(35).toList())) + "\n\n"
                + "=== SANITIZED CONFIGURATION & STACK FILES ===\n"
                + (rawConfigContents.length() == 0 ? "- None\n" : rawConfigContents.toString()) + "\n"
                + "Git Contribution Statistics (Evaluated for Candidate: " + candidateLogin + "):\n"
                + "- Total Commits Evaluated: " + totalCommits + "\n"
                + "- Commits Authored By Candidate: " + candidateCommits + " (" + (totalCommits > 0 ? (candidateCommits * 100 / totalCommits) : 0) + "% ownership)\n"
                + "- Candidate Active Contribution Period: " + earliestCommit + " to " + latestCommit + " (" + contributionDays + " days)\n"
                + "- Total Pull Requests Evaluated: " + totalPulls + "\n"
                + "- Pull Requests Opened By Candidate: " + candidatePulls + " (" + candidateMergedPulls + " successfully merged)\n\n"
                + "Recent Commit Messages:\n" + recentCommitsText.toString() + "\n"
                + "Recent Pull Requests:\n" + recentPullsText.toString() + "\n"
                + "=== INSTRUCTIONS ===\n"
                + "1. Generate a 2-3 sentence 'aiSummary' of the project.\n"
                + "2. Generate an 'architectureSummary' describing structural patterns (e.g. Layered, Microservices, MVC) and main database/authentication mechanisms.\n"
                + "3. Determine the 'confidenceLevel' (HIGH, MEDIUM, LOW) based on candidate code ownership statistics. If candidate commits are extremely low (<20%) or the history is just one initial import not authored by them, set to LOW.\n"
                + "4. Extract 'claims' (languages, frameworks, databases, infrastructure). Assign a confidence score (0-100) and specify which paths support it.\n"
                + "5. Extract 'evidenceItems' showing specific file paths as proof (e.g. pom.xml as dependency file, build.yml as CI workflow, README.md as readme).\n\n"
                + "Respond ONLY with a valid JSON matching this schema:\n"
                + "{\n"
                + "  \"aiSummary\": \"string\",\n"
                + "  \"architectureSummary\": \"string\",\n"
                + "  \"confidenceLevel\": \"HIGH or MEDIUM or LOW\",\n"
                + "  \"claims\": [\n"
                + "    { \"claimType\": \"LANGUAGE or FRAMEWORK or DATABASE or INFRASTRUCTURE\", \"claimValue\": \"string\", \"explanation\": \"string\", \"confidence\": 95, \"supportingEvidencePaths\": [\"string\"] }\n"
                + "  ],\n"
                + "  \"evidenceItems\": [\n"
                + "    { \"label\": \"string\", \"type\": \"README or SOURCE_CODE or DEPENDENCY_FILE or DOCKER_CONFIGURATION or CI_WORKFLOW\", \"summary\": \"string\", \"sourcePath\": \"string\" }\n"
                + "  ]\n"
                + "}";
    }

    @Transactional
    protected void saveAnalysisResults(Project project, GeminiAnalysisResponseDTO analysis, Set<String> actualPaths) {
        // 1. Update Project Summaries and Status
        project.setAiSummary(analysis.aiSummary());
        project.setArchitectureSummary(analysis.architectureSummary());
        project.setConfidenceLevel(parseConfidenceLevel(analysis.confidenceLevel()));
        project.setAnalysisStatus(AnalysisStatus.COMPLETED);
        project.setAnalyzedAt(Instant.now());

        // 2. Clear old evidence and associated claims for this project
        List<EvidenceItem> oldEvidence = evidenceItemRepository.findByProjectId(project.getId());
        for (EvidenceItem ev : oldEvidence) {
            if (ev.getClaims() != null) {
                for (DerivedClaim claim : ev.getClaims()) {
                    claim.getEvidenceItems().clear();
                    derivedClaimRepository.delete(claim);
                }
                ev.getClaims().clear();
            }
        }
        evidenceItemRepository.deleteAll(oldEvidence);
        evidenceItemRepository.flush();
        derivedClaimRepository.flush();

        // 3. Save new evidence items (Section 8 Validation)
        Map<String, EvidenceItem> savedEvidenceMap = new HashMap<>();
        if (analysis.evidenceItems() != null) {
            for (GeminiEvidenceDTO evDto : analysis.evidenceItems()) {
                // Section 8 Ground Truth Check: Ensure path exists in GitHub file tree
                if (evDto.sourcePath() == null || !actualPaths.contains(evDto.sourcePath())) {
                    log.warn("Gemini returned invalid/hallucinated path: {} for project: {}. Skipping.", 
                            evDto.sourcePath(), project.getTitle());
                    continue;
                }

                EvidenceItem evidence = new EvidenceItem();
                evidence.setProject(project);
                evidence.setLabel(evDto.label());
                evidence.setType(parseEvidenceType(evDto.type(), evDto.sourcePath()));
                evidence.setSummary(evDto.summary());
                evidence.setSourcePath(evDto.sourcePath());
                evidence.setVisibility(com.aditya.nexora.profileService.enums.Visibility.PUBLIC);
                
                EvidenceItem saved = evidenceItemRepository.save(evidence);
                savedEvidenceMap.put(evDto.sourcePath(), saved);
            }
        }

        // 4. Save new derived claims and link only validated evidence
        if (analysis.claims() != null) {
            for (GeminiClaimDTO claimDto : analysis.claims()) {
                DerivedClaim claim = new DerivedClaim();
                claim.setUserId(project.getUserId());
                claim.setClaimType(parseClaimType(claimDto.claimType()));
                claim.setClaimValue(claimDto.claimValue());
                claim.setExplanation(claimDto.explanation());
                claim.setConfidence(claimDto.confidence() > 0 ? claimDto.confidence() : 80);
                claim.setApprovalState(com.aditya.nexora.profileService.enums.ApprovalState.PENDING);
                claim.setEvidenceItems(new ArrayList<>());

                // Link supporting evidence items via the Join table
                if (claimDto.supportingEvidencePaths() != null) {
                    for (String path : claimDto.supportingEvidencePaths()) {
                        EvidenceItem supportingItem = savedEvidenceMap.get(path);
                        if (supportingItem != null) {
                            claim.getEvidenceItems().add(supportingItem);
                            if (supportingItem.getClaims() == null) {
                                supportingItem.setClaims(new ArrayList<>());
                            }
                            supportingItem.getClaims().add(claim);
                        }
                    }
                }

                derivedClaimRepository.save(claim);
            }
        }

        projectRepository.save(project);
        log.info("Successfully saved Spring AI analysis results for: {}", project.getTitle());
    }

    @Override
    @Async("taskExecutor")
    @Transactional
    public void analyzeProjectAsync(Long projectId, String accessToken) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        log.info("Starting background Spring AI analysis for project: {}", project.getTitle());
        project.setAnalysisStatus(AnalysisStatus.PROCESSING);
        projectRepository.saveAndFlush(project);

        try {
            // Load Candidate GitHub Username for stats verification
            ConnectedSource source = connectedSourceRepository.findByUserIdAndProvider(project.getUserId(), SourceProvider.GITHUB)
                    .orElseThrow(() -> new ResourceNotFoundException("GitHub connected source not found for user: " + project.getUserId()));
            String candidateLogin = source.getGithubUsername();

            // Step 1: Fetch Repository Tree
            List<GitHubTreeItemDTO> tree = githubService.fetchRepositoryTree(
                    project.getOwnerLogin(),
                    project.getTitle(),
                    project.getDefaultBranch() != null ? project.getDefaultBranch() : "main",
                    accessToken
            );

            List<String> treePaths = tree != null
                    ? tree.stream().map(GitHubTreeItemDTO::path).toList()
                    : Collections.emptyList();

            // Step 2: Fetch Target File Contents
            Map<String, String> fileContents = fetchTargetFileContents(project, tree, accessToken);

            // Step 3: Fetch Repository Commit History
            List<GitHubCommitDTO> commits = githubService.fetchCommits(
                    project.getOwnerLogin(),
                    project.getTitle(),
                    accessToken
            );

            // Step 4: Fetch Repository Pull Requests
            List<GitHubPullRequestDTO> pulls = githubService.fetchPullRequests(
                    project.getOwnerLogin(),
                    project.getTitle(),
                    accessToken
            );

            // Step 5: Build Gemini Prompt
            String prompt = buildGeminiPrompt(project, treePaths, fileContents, commits, pulls, candidateLogin);

            // Step 6 & 7: Call Spring AI
            GeminiAnalysisResponseDTO response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(GeminiAnalysisResponseDTO.class);

            // Step 8: Save to database (with Section 8 Validation)
            Set<String> actualPaths = new HashSet<>(treePaths);
            saveAnalysisResults(project, response, actualPaths);

        } catch (Exception e) {
            log.error("Failed to perform project analysis for ID: {}", projectId, e);
            project.setAnalysisStatus(AnalysisStatus.FAILED);
            projectRepository.save(project);
        }
    }
}
