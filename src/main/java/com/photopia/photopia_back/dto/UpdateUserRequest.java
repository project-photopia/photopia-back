package com.photopia.photopia_back.dto;

public record UpdateUserRequest(
    String username,
    String avatarUrl
) {}