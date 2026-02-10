package com.photopia.photopia_back.service;

import com.photopia.photopia_back.model.Media;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.MediaRepository;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import com.photopia.photopia_back.repository.CapsuleRepository;
import java.util.UUID;
import java.util.List;

@Service
public class MediaService {

    private final R2Service r2Service;
    private final MediaRepository mediaRepository;
    private final CapsuleRepository capsuleRepository;
    private final ImageMetadataService imageMetadataService;

    public MediaService(R2Service r2Service, MediaRepository mediaRepository,
            CapsuleRepository capsuleRepository, ImageMetadataService imageMetadataService) {
        this.r2Service = r2Service;
        this.mediaRepository = mediaRepository;
        this.capsuleRepository = capsuleRepository;
        this.imageMetadataService = imageMetadataService;
    }

    @Transactional
    public Media uploadMedia(MultipartFile file, User user, UUID capsuleId) throws IOException {
        // Fetch Capsule
        com.photopia.photopia_back.model.Capsule capsule = capsuleRepository.findById(capsuleId)
                .orElseThrow(() -> new RuntimeException("Capsule not found"));

        // Upload to R2
        String fileName = r2Service.uploadFile(file);

        // Construct Public URL or Key
        String fileUrl = fileName;

        // Extract Metadata
        ImageMetadataService.ImageMetadata metadata = imageMetadataService.extractMetadata(file);

        Media media = Media.builder()
                .originalUrl(fileUrl)
                .mediaType(determineMediaType(file.getContentType()))
                .user(user)
                .capsule(capsule)
                .width(metadata.width())
                .height(metadata.height())
                .takenAt(metadata.takenAt() != null ? metadata.takenAt().toInstant() : null)
                .latitude(metadata.latitude())
                .longitude(metadata.longitude())
                .build();

        return mediaRepository.save(media);
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

        // In a real world scenario, we might want to delete the file from R2 as well
        // r2Service.deleteFile(media.getOriginalUrl());

        mediaRepository.delete(media);
    }
}
