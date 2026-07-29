package com.aditya.nexora.profileService.entity;
import com.aditya.nexora.profileService.enums.EvidenceType;
import com.aditya.nexora.profileService.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "evidence_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvidenceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connected_source_id")
    private ConnectedSource connectedSource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EvidenceType type;

    @Column(nullable = false, length = 200)
    private String label;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "source_path", length = 500)
    private String sourcePath;

    @Column(name = "source_reference", length = 255)
    private String sourceReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }


}