package com.photopia.photopia_back.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String content,
        UUID userId,
        Instant createdAt) {
}
