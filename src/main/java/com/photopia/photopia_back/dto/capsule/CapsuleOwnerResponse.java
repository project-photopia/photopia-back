package com.photopia.photopia_back.dto.capsule;

import java.util.UUID;

public record CapsuleOwnerResponse(
        UUID id,
        String username,
        String email,
        String avatarUrl
) {}
