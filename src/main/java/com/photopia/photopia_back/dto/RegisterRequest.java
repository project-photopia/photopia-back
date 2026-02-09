package com.photopia.photopia_back.dto;

public record RegisterRequest(
        String username,
        String email,
        String password) {
}
