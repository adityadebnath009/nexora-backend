package com.aditya.nexora.profileService.entity;

import com.aditya.nexora.profileService.enums.AnalysisStatus;
import com.aditya.nexora.profileService.enums.ConfidenceLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "projects",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_source_repository",
                columnNames = {"connected_source_id", "provider_repository_id"}
        )
)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connected_source_id")
    private ConnectedSource connectedSource;

    @Column(name = "provider_repository_id")
    private Long providerRepositoryId; // GitHub repository ID

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description; // User-authored description

    @Column(name = "github_description", columnDefinition = "TEXT")
    private String githubDescription; // Synced description from GitHub

    @Column(name = "repo_url", length = 500)
    private String repoUrl;

    @Column(name = "live_url", length = 500)
    private String liveUrl;

    @Column(name = "stated_role", length = 150)
    private String statedRole;

    @Column(name = "contribution_summary", columnDefinition = "TEXT")
    private String contributionSummary; // User-authored contribution summary

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "project_tech_stack",
            joinColumns = @JoinColumn(name = "project_id")
    )
    @Column(name = "technology", nullable = false, length = 100)
    private List<String> techStack;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "architecture_summary", columnDefinition = "TEXT")
    private String architectureSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_status", nullable = false)
    private AnalysisStatus analysisStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "confidence_level")
    private ConfidenceLevel confidenceLevel;

    @Column(name = "analyzed_at")
    private Instant analyzedAt;

    @Column(name = "analysis_version")
    private String analysisVersion;

    @Column(name = "is_visible", nullable = false)
    private boolean isVisible;

    @Column(name = "is_fork", nullable = false)
    private boolean isFork;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived;

    @Column(name = "default_branch", length = 100)
    private String defaultBranch;

    @Column(name = "owner_login", length = 150)
    private String ownerLogin;

    @Column(name = "full_name", length = 250)
    private String fullName;

    @Column(name = "stars")
    private Integer stars;

    @Column(name = "forks")
    private Integer forks;

    @Column(name="repository_visibility", length=50)
    private String repositoryVisibility;

    @Column(name = "repo_created_at")
    private LocalDateTime repoCreatedAt;

    @Column(name = "repo_updated_at")
    private LocalDateTime repoUpdatedAt;

    @Column(name = "pushed_at")
    private LocalDateTime pushedAt;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.analysisStatus == null) {
            this.analysisStatus = AnalysisStatus.PENDING;
        }
        // Force new projects to default to invisible until the developer selects them
        this.isVisible = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}