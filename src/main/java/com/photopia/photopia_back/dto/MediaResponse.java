package com.photopia.photopia_back.dto;

import java.util.UUID;
import java.time.Instant;

public record MediaResponse(
        UUID id,
        String originalUrl,
        String previewUrl,
        String thumbnailUrl,
        String mediaType,
        Integer width,
        Integer height,
        Integer durationSec,
        Double latitude,
        Double longitude,
        String locationName,
        Instant takenAt,
        Instant uploadedAt,
        Integer reactionCount,
        Integer commentCount,
        UUID capsuleId,
        UUID userId) {
}
