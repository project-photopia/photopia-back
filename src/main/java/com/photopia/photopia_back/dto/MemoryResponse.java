package com.photopia.photopia_back.dto;

import java.util.List;
import java.util.UUID;

public record MemoryResponse(
        String id,
        String title,
        String subtitle,
        String trigger,
        UUID capsuleId,
        String capsuleName,
        List<MemoryMediaResponse> medias) {
}
