package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.model.Media;
import com.photopia.photopia_back.service.MediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import com.photopia.photopia_back.model.User;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;
    private final com.photopia.photopia_back.service.CommentService commentService;
    private final com.photopia.photopia_back.service.ReactionService reactionService;

    public MediaController(MediaService mediaService,
            com.photopia.photopia_back.service.CommentService commentService,
            com.photopia.photopia_back.service.ReactionService reactionService) {
        this.mediaService = mediaService;
        this.commentService = commentService;
        this.reactionService = reactionService;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<com.photopia.photopia_back.dto.MediaResponse> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("capsuleId") UUID capsuleId) throws IOException {

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Media media = mediaService.uploadMedia(file, currentUser, capsuleId);

        com.photopia.photopia_back.dto.MediaResponse response = new com.photopia.photopia_back.dto.MediaResponse(
                media.getId(),
                media.getOriginalUrl(),
                media.getPreviewUrl(),
                media.getThumbnailUrl(),
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

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<com.photopia.photopia_back.dto.MediaResponse> getMedia(@PathVariable UUID id) {
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
    public ResponseEntity<com.photopia.photopia_back.dto.CommentResponse> addComment(
            @PathVariable UUID id,
            @RequestBody com.photopia.photopia_back.dto.CommentRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(commentService.addComment(id, currentUser, request.content()));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<java.util.List<com.photopia.photopia_back.dto.CommentResponse>> getComments(
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
            @RequestBody com.photopia.photopia_back.dto.ReactionRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        reactionService.toggleReaction(id, currentUser, request.emoji());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/capsule/{capsuleId}")
    public ResponseEntity<java.util.List<com.photopia.photopia_back.dto.MediaResponse>> getCapsuleMedias(
            @PathVariable UUID capsuleId) {

        java.util.List<Media> medias = mediaService.getMediasByCapsuleId(capsuleId);

        java.util.List<com.photopia.photopia_back.dto.MediaResponse> responses = medias.stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    private com.photopia.photopia_back.dto.MediaResponse mapToResponse(Media media) {
        return new com.photopia.photopia_back.dto.MediaResponse(
                media.getId(),
                media.getOriginalUrl(),
                media.getPreviewUrl(),
                media.getThumbnailUrl(),
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
