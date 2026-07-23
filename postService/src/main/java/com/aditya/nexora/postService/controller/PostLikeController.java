package com.aditya.nexora.postService.controller;


import com.aditya.nexora.postService.entity.PostLike;
import com.aditya.nexora.postService.repository.PostLikeRepository;
import com.aditya.nexora.postService.service.PostLikeService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post")
public class PostLikeController {

    private final PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    @DeleteMapping("/{postId}/unlike")
    public ResponseEntity<?> unlikePost(@RequestHeader("X-User-Id") Long userId, @PathVariable("postId") Long postId) {
        postLikeService.unlikePost(postId, userId);
        return ResponseEntity.noContent().build();

    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> toggleLike(
            @PathVariable("postId") Long postId,
            @RequestHeader("X-User-Id") Long userId) {

        boolean isLiked = postLikeService.toggleLikePost(postId, userId);

        String message = isLiked ? "Post liked successfully" : "Post unliked successfully";
        return ResponseEntity.ok(message);
    }
}
