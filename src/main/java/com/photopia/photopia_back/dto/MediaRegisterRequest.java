package com.photopia.photopia_back.dto;

import java.time.Instant;
import java.util.UUID;

public record MediaRegisterRequest(
        String originalKey,
        String previewKey,
        UUID capsuleId,
        Integer width,
        Integer height,
        Double latitude,
        Double longitude,
        Instant takenAt,
        String mediaType) {
}
