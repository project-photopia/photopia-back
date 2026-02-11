package com.photopia.photopia_back.dto.capsule;

import com.photopia.photopia_back.model.CapsuleMember;

import java.util.UUID;

public record CapsuleMemberResponse(
        UUID userId,
        String username,
        String avatarUrl,
        CapsuleMember.Role role
) {}
