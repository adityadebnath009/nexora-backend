package com.aditya.nexora.postService.controller;


import com.aditya.nexora.postService.entity.PostLike;
import com.aditya.nexora.postService.repository.PostLikeRepository;
import com.aditya.nexora.postService.service.PostLikeService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post")
public class PostLikeController {

    private final PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    @DeleteMapping("/{postId}/unlike")
    public ResponseEntity<?> unlikePost(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("postId") Long postId) {
        Long userId = jwt.getClaim("userId");
        postLikeService.unlikePost(postId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> toggleLike(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        boolean isLiked = postLikeService.toggleLikePost(postId, userId);

        String message = isLiked ? "Post liked successfully" : "Post unliked successfully";
        return ResponseEntity.ok(message);
    }
}
