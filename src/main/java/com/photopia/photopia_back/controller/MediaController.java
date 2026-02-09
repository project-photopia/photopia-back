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

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
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

    @GetMapping("/capsule/{capsuleId}")
    public ResponseEntity<java.util.List<com.photopia.photopia_back.dto.MediaResponse>> getCapsuleMedias(
            @PathVariable UUID capsuleId) {

        java.util.List<Media> medias = mediaService.getMediasByCapsuleId(capsuleId);

        java.util.List<com.photopia.photopia_back.dto.MediaResponse> responses = medias.stream()
                .map(media -> new com.photopia.photopia_back.dto.MediaResponse(
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
                        media.getUser() != null ? media.getUser().getId() : null))
                .toList();

        return ResponseEntity.ok(responses);
    }
}
