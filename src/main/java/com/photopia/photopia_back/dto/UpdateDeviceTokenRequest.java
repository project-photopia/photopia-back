package com.photopia.photopia_back.dto;

public record UpdateDeviceTokenRequest(String token, boolean notificationsEnabled) {}
