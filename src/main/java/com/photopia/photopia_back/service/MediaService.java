package com.photopia.photopia_back.service;

import com.photopia.photopia_back.model.Media;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.MediaRepository;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.photopia.photopia_back.repository.CapsuleRepository;
import java.util.UUID;
import java.util.List;

@Service
public class MediaService {

    private final R2Service r2Service;
    private final MediaRepository mediaRepository;
    private final CapsuleRepository capsuleRepository;

    public MediaService(R2Service r2Service, MediaRepository mediaRepository,
            CapsuleRepository capsuleRepository) {
        this.r2Service = r2Service;
        this.mediaRepository = mediaRepository;
        this.capsuleRepository = capsuleRepository;
    }

    @Transactional
    public Media registerMedia(com.photopia.photopia_back.dto.MediaRegisterRequest request, User user) {
        // Fetch Capsule
        com.photopia.photopia_back.model.Capsule capsule = capsuleRepository.findById(request.capsuleId())
                .orElseThrow(() -> new RuntimeException("Capsule not found"));

        Media media = Media.builder()
                .originalUrl(request.originalKey())
                .previewUrl(request.previewKey())
                .thumbnailUrl(request.previewKey()) // Backward compatibility
                .mediaType(determineMediaType(request.mediaType()))
                .user(user)
                .capsule(capsule)
                .width(request.width())
                .height(request.height())
                .takenAt(request.takenAt())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();

        return mediaRepository.save(media);
    }

    public com.photopia.photopia_back.dto.PresignedUrlResponse getPresignedUrl(String contentType) {
        return r2Service.generatePresignedUrl(contentType);
    }

    public String getPresignedGetUrl(String key) {
        return r2Service.generatePresignedGetUrl(key);
    }

    private Media.MediaType determineMediaType(String contentType) {
        if (contentType != null && contentType.startsWith("video/")) {
            return Media.MediaType.VIDEO;
        }
        return Media.MediaType.PHOTO;
    }

    public List<Media> getMediasByCapsuleId(UUID capsuleId) {
        return mediaRepository.findByCapsuleIdOrderByTakenAtDesc(capsuleId);
    }

    public Media getMediaById(UUID id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media not found"));
    }

    @Transactional
    public void deleteMedia(UUID id, User user) {
        Media media = getMediaById(id);

        // Check if user is owner of the media
        if (!media.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this media");
        }

        mediaRepository.delete(media);
    }
}
