package com.aditya.nexora.postService.controller;


import com.aditya.nexora.postService.dto.PostCreateRequestDTO;
import com.aditya.nexora.postService.dto.PostDTO;
import com.aditya.nexora.postService.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDTO> createPost(@ModelAttribute @Valid PostCreateRequestDTO postCreateRequestDTO, @RequestHeader("X-User-Id") Long userId) {

        return ResponseEntity.ok(postService.createPost(postCreateRequestDTO, userId));

    }
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<PostDTO>> getAllPosts(@PathVariable("userId") Long userId)
    {
        return ResponseEntity.ok(postService.getAllPosts(userId));
    }
    @GetMapping("/{postId}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable("postId") Long postId)
    {
        return ResponseEntity.ok(postService.getPostById(postId));
    }

}
