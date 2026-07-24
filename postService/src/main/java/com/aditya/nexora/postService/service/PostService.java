package com.aditya.nexora.postService.service;


import com.aditya.nexora.postService.dto.PostCreateRequestDTO;
import com.aditya.nexora.postService.dto.PostDTO;
import com.aditya.nexora.postService.entity.Post;
import com.aditya.nexora.postService.exception.ResourceNotFoundException;
import com.aditya.nexora.postService.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;



@Slf4j
@Service
public class PostService {

    private final PostRepository postRepository;
    private final CloudinaryService cloudinaryService;
    public PostService(PostRepository postRepository, CloudinaryService cloudinaryService) {
        this.postRepository = postRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public PostDTO createPost(PostCreateRequestDTO postCreateRequestDTO, Long userId) {
        log.info("Creating post for user id: {}", userId);
        Post post = new Post();
        List<String> imageUrls = cloudinaryService.uploadImages(postCreateRequestDTO.images());
        post.setContent(postCreateRequestDTO.content());
        post.setUserId(userId);
        post.setImages(imageUrls);
        Post savedPost = postRepository.save(post);
        log.info("Post created successfully: {}", savedPost);
        return new PostDTO(
            savedPost.getId(),
            savedPost.getUserId(),
            savedPost.getContent(),
            savedPost.getImages(),
            savedPost.getCreatedAt(),
            savedPost.getUpdatedAt()
        );
    }

    public List<PostDTO> getAllPosts(Long userId) {
        log.info("Fetching all posts for user id: {}", userId);
        return postRepository.findByUserId(userId).stream()
                .map(post -> new PostDTO(
                    post.getId(),
                    post.getUserId(),
                    post.getContent(),
                    post.getImages(),
                    post.getCreatedAt(),
                    post.getUpdatedAt()
                )).toList();
    }

    public PostDTO getPostById(Long postId) {
        log.info("Fetching post with id: {}", postId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with ID:" + postId));
        return new PostDTO(
            post.getId(),
            post.getUserId(),
            post.getContent(),
            post.getImages(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        );
    }
}
