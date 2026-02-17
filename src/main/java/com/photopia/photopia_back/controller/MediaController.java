package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.dto.CommentRequest;
import com.photopia.photopia_back.dto.CommentResponse;
import com.photopia.photopia_back.dto.MediaRegisterRequest;
import com.photopia.photopia_back.dto.MediaResponse;
import com.photopia.photopia_back.dto.PresignedUrlResponse;
import com.photopia.photopia_back.dto.ReactionRequest;
import com.photopia.photopia_back.model.ApiResponse;
import com.photopia.photopia_back.model.ApiSuccessResponse;
import com.photopia.photopia_back.model.Media;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.service.CommentService;
import com.photopia.photopia_back.service.MediaService;
import com.photopia.photopia_back.service.ReactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;
    private final CommentService commentService;
    private final ReactionService reactionService;

    public MediaController(MediaService mediaService,
            CommentService commentService,
            ReactionService reactionService) {
        this.mediaService = mediaService;
        this.commentService = commentService;
        this.reactionService = reactionService;
    }

    @GetMapping("/upload-url")
    public ResponseEntity<ApiResponse> getUploadUrl(
            @RequestParam("contentType") String contentType) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PresignedUrlResponse presignedUrl = mediaService.getPresignedUrl(contentType, currentUser.getId());
        return ResponseEntity.ok(ApiSuccessResponse.of(presignedUrl, "Upload URL generated"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerMedia(
            @RequestBody MediaRegisterRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Media media = mediaService.registerMedia(request, currentUser);
        return ResponseEntity.ok(ApiSuccessResponse.of(mapToResponse(media), "Media registered"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getMedia(@PathVariable UUID id) {
        Media media = mediaService.getMediaById(id);
        return ResponseEntity.ok(ApiSuccessResponse.of(mapToResponse(media), "Media fetched"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteMedia(@PathVariable UUID id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mediaService.deleteMedia(id, currentUser);
        return ResponseEntity.ok(ApiSuccessResponse.of(null, "Media deleted"));
    }

    // --- Comments ---

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse> addComment(
            @PathVariable UUID id,
            @RequestBody CommentRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CommentResponse comment = commentService.addComment(id, currentUser, request.content());
        return ResponseEntity.ok(ApiSuccessResponse.of(comment, "Comment added"));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse> getComments(
            @PathVariable UUID id) {
        List<CommentResponse> comments = commentService.getCommentsByMedia(id);
        return ResponseEntity.ok(ApiSuccessResponse.of(comments, "Comments fetched"));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable UUID commentId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.ok(ApiSuccessResponse.of(null, "Comment deleted"));
    }

    // --- Reactions ---

    @PostMapping("/{id}/react")
    public ResponseEntity<ApiResponse> reactToMedia(
            @PathVariable UUID id,
            @RequestBody ReactionRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        reactionService.toggleReaction(id, currentUser, request.emoji());
        return ResponseEntity.ok(ApiSuccessResponse.of(null, "Reaction toggled"));
    }

    @GetMapping("/capsule/{capsuleId}")
    public ResponseEntity<ApiResponse> getCapsuleMedias(
            @PathVariable UUID capsuleId) {

        List<Media> medias = mediaService.getMediasByCapsuleId(capsuleId);

        List<MediaResponse> responses = medias.stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(ApiSuccessResponse.of(responses, "Capsule medias fetched"));
    }

    private MediaResponse mapToResponse(Media media) {
        // Transform stored keys into Presigned GET URLs
        String originalUrl = mediaService.getPresignedGetUrl(media.getOriginalUrl());
        String previewUrl = mediaService.getPresignedGetUrl(media.getPreviewUrl());
        String thumbnailUrl = mediaService.getPresignedGetUrl(media.getThumbnailUrl());

        return new MediaResponse(
                media.getId(),
                originalUrl,
                previewUrl,
                thumbnailUrl,
                media.getMediaType() != null ? media.getMediaType().name() : null,
                media.getWidth(),
                media.getHeight(),
                media.getDurationSec(),
                media.getLatitude(),
                media.getLongitude(),
                media.getLocationName(),
                media.getTakenAt(),
                media.getUploadedAt(),
                media.getReactionCount(),
                media.getCommentCount(),
                media.getCapsule() != null ? media.getCapsule().getId() : null,
                media.getUser() != null ? media.getUser().getId() : null);
    }
}