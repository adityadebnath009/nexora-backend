package com.aditya.nexora.profileService.entity;


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

    @Column(nullable = false)
    private String accessToken;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OwnershipStatus ownershipStatus;

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
