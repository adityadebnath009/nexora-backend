package com.aditya.nexora.postService.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "post_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_post_user_like",
                columnNames = {"post_id", "user_id"}
        ))

public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long userId;

    @CreationTimestamp
    private Instant createdAt;

}
