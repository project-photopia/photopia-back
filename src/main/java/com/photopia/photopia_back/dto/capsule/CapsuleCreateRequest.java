package com.photopia.photopia_back.dto.capsule;

public record CapsuleCreateRequest(
        String name,
        Boolean isPrivate,
        String eventTypeName) {
}
