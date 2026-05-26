package com.photopia.photopia_back.dto.capsule;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CapsuleGetMyCapsulesResponse(
        UUID id,
        String name,
        String coverUrl,
        LocalDate startDate,
        LocalDate endDate,
        String color,
        Boolean isPrivate,
        String joinToken,
        CapsuleOwnerResponse owner,
        Boolean isArchived,
        Integer memberCount,
        Double latitude,
        Double longitude,
        String eventTypeName,
        Integer mediaCount,
        Instant createdAt,
        Instant updatedAt
) {}
