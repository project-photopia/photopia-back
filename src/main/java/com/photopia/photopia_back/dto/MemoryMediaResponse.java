package com.photopia.photopia_back.dto;

import java.time.Instant;
import java.util.UUID;

public record MemoryMediaResponse(
        UUID id,
        String originalUrl,
        String previewUrl,
        String thumbnailUrl,
        String mediaType,
        Integer width,
        Integer height,
        Double latitude,
        Double longitude,
        String locationName,
        Instant takenAt,
        String uploaderName,
        String uploaderAvatarUrl) {
}
