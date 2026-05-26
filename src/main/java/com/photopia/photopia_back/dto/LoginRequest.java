package com.photopia.photopia_back.dto;

public record LoginRequest(
        String email,
        String password) {
}
