package com.aditya.nexora.postService.service;


import com.aditya.nexora.postService.entity.PostLike;
import com.aditya.nexora.postService.exception.BadRequestException;
import com.aditya.nexora.postService.exception.ResourceNotFoundException;
import com.aditya.nexora.postService.repository.PostLikeRepository;
import com.aditya.nexora.postService.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new ResourceNotFoundException("Post not found with ID: " + postId);
        }

        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {

            postLikeRepository.deleteByPostIdAndUserId(postId, userId);
            return false;
        } else {
            PostLike postLike = PostLike.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();
            postLikeRepository.save(postLike);
            return true;
        }
    }


    @Transactional
    public void likePost(Long postId, Long userId) {
        if(!postRepository.existsById(postId))
        {
            throw new BadRequestException("Post not found with id: " + postId);
        }
        if(postLikeRepository.existsByPostIdAndUserId(postId, userId))
        {
            throw new BadRequestException("User already liked this post");
        }
        PostLike postLike = PostLike.builder()
                .postId(postId)
                .userId(userId)
                .build();
        postLikeRepository.save(postLike);
    }
    public void unlikePost(Long postId, Long userId) {
        postLikeRepository.deleteByPostIdAndUserId(postId, userId);
    }
    public long countLikes(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }
}
