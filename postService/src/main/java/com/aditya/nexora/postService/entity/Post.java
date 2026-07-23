package com.aditya.nexora.postService.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "posts")
public class Post {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Long userId;

    @ElementCollection()
    @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();


    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;

    }
    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();

    }


}
