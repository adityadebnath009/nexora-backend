package com.aditya.nexora.postService.repository;


import com.aditya.nexora.postService.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    @Transactional
    void deleteByPostIdAndUserId(@NonNull Long postId, @NonNull Long userId);

    long countByPostId(@NonNull Long postId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostId(Long postId);
}
