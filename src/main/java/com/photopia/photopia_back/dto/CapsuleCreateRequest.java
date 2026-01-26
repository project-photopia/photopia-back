package com.photopia.photopia_back.dto;

import java.util.UUID;

public record CapsuleCreateRequest(
        String name,
        UUID userId,
        Boolean isPrivate) {
}
