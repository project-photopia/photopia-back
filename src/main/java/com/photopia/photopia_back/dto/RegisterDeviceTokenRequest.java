package com.photopia.photopia_back.dto;

import com.photopia.photopia_back.model.DeviceToken.Platform;

public record RegisterDeviceTokenRequest(String token, Platform platform, Boolean enabled) {}
