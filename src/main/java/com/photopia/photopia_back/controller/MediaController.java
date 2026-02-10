package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.model.Media;
import com.photopia.photopia_back.service.MediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import com.photopia.photopia_back.model.User;
import org.springframework.security.core.context.SecurityContextHolder;

import com.photopia.photopia_back.dto.PresignedUrlResponse;
import com.photopia.photopia_back.dto.MediaResponse;
import com.photopia.photopia_back.dto.MediaRegisterRequest;
import com.photopia.photopia_back.dto.CommentResponse;
import com.photopia.photopia_back.dto.CommentRequest;
import com.photopia.photopia_back.dto.ReactionRequest;
import com.photopia.photopia_back.service.CommentService;
import com.photopia.photopia_back.service.ReactionService;
import java.util.List;

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
    public ResponseEntity<PresignedUrlResponse> getUploadUrl(
            @RequestParam("contentType") String contentType) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(mediaService.getPresignedUrl(contentType, currentUser.getId()));
    }

    @PostMapping("/register")
    public ResponseEntity<MediaResponse> registerMedia(
            @RequestBody MediaRegisterRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Media media = mediaService.registerMedia(request, currentUser);
        return ResponseEntity.ok(mapToResponse(media));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MediaResponse> getMedia(@PathVariable UUID id) {
        Media media = mediaService.getMediaById(id);
        return ResponseEntity.ok(mapToResponse(media));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable UUID id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        mediaService.deleteMedia(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // --- Comments ---

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable UUID id,
            @RequestBody CommentRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(commentService.addComment(id, currentUser, request.content()));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable UUID id) {
        return ResponseEntity.ok(commentService.getCommentsByMedia(id));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.noContent().build();
    }

    // --- Reactions ---

    @PostMapping("/{id}/react")
    public ResponseEntity<Void> reactToMedia(
            @PathVariable UUID id,
            @RequestBody ReactionRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        reactionService.toggleReaction(id, currentUser, request.emoji());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/capsule/{capsuleId}")
    public ResponseEntity<List<MediaResponse>> getCapsuleMedias(
            @PathVariable UUID capsuleId) {

        List<Media> medias = mediaService.getMediasByCapsuleId(capsuleId);

        List<MediaResponse> responses = medias.stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(responses);
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
