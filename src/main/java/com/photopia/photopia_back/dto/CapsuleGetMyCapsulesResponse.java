package com.photopia.photopia_back.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CapsuleGetMyCapsulesResponse(
        UUID id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String joinToken,
        Boolean isPrivate,
        Boolean isArchived,
        Integer memberCount
){}
