package com.aditya.nexora.profileService.entity;


import com.aditya.nexora.profileService.enums.ApprovalState;
import com.aditya.nexora.profileService.enums.ClaimType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "derived_claims")
public class DerivedClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name="user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name="claim_type")
    private ClaimType claimType;


    @Column(nullable = false, name="claim_value", length = 150)
    private String claimValue;

    @Column(nullable = false, name="explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false, name="confidence")
    private int confidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name="approval_state")
    private ApprovalState approvalState;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "claim_evidence",
            joinColumns = @JoinColumn(name = "claim_id"),
            inverseJoinColumns = @JoinColumn(name = "evidence_id")
    )
    private List<EvidenceItem> evidenceItems;

    private Instant createdAt;
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
