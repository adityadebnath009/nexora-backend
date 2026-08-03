package com.aditya.nexora.profileService.entity;


import com.aditya.nexora.profileService.config.AttributeEncryptor;
import com.aditya.nexora.profileService.enums.OwnershipStatus;
import com.aditya.nexora.profileService.enums.SourceProvider;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "connected_sources")
public class ConnectedSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long providerUserId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceProvider provider;

    @Column(name = "github_username")
    private String githubUsername;

    @Convert(converter = AttributeEncryptor.class)
    @Column(nullable = false, length = 1024)
    private String accessToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OwnershipStatus ownershipStatus;

    @Column(name = "connected_at", nullable = false)
    private Instant connectedAt;

    @Column(name = "last_sync_time")
    private Instant lastSyncTime;

    @Column(name = "last_analysis_time")
    private Instant lastAnalysisTime;



    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;
    @PrePersist
    public void createdAt() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    @PreUpdate
    public void updatedAt() {
        this.updatedAt = Instant.now();
    }


}
