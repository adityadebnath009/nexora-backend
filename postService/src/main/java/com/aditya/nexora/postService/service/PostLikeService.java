package com.aditya.nexora.postService.service;


import com.aditya.nexora.postService.entity.PostLike;
import com.aditya.nexora.postService.exception.BadRequestException;
import com.aditya.nexora.postService.exception.ResourceNotFoundException;
import com.aditya.nexora.postService.repository.PostLikeRepository;
import com.aditya.nexora.postService.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    public PostLikeService(PostLikeRepository postLikeRepository, PostRepository postRepository) {
        this.postLikeRepository = postLikeRepository;
        this.postRepository = postRepository;
    }
    @Transactional
    public boolean toggleLikePost(Long postId, Long userId) {

        if (!postRepository.existsById(postId)) {
            log.error("Post not found with ID: {}", postId);
            throw new ResourceNotFoundException("Post not found with ID: " + postId);
        }

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            log.info("Post already liked by user with ID: {}", userId);
            postLikeRepository.deleteByPostIdAndUserId(postId, userId);
            return false;
        } else {
            log.info("Post liked by user with ID: {}", userId);
            PostLike postLike = PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();
            postLikeRepository.save(postLike);
            return true;
        }
    }
    public void unlikePost(Long postId, Long userId) {
        log.info("Unliking post with ID: {} for user with ID: {}", postId, userId);
        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
    }
    public long countLikes(Long postId) {
        log.info("Counting likes for post with ID: {}", postId);
        return postLikeRepository.countByPostId(postId);
    }
}
