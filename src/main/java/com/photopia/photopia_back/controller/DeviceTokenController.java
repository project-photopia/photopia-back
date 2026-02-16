package com.photopia.photopia_back.controller;

import com.photopia.photopia_back.dto.RegisterDeviceTokenRequest;
import com.photopia.photopia_back.dto.UpdateDeviceTokenRequest;
import com.photopia.photopia_back.model.ApiError;
import com.photopia.photopia_back.model.ApiErrorResponse;
import com.photopia.photopia_back.model.ApiResponse;
import com.photopia.photopia_back.model.ApiSuccessResponse;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.service.DeviceTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device-tokens")
public class DeviceTokenController {

  private final DeviceTokenService deviceTokenService;

  public DeviceTokenController(DeviceTokenService deviceTokenService) {
    this.deviceTokenService = deviceTokenService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse> registerToken(
      @RequestBody RegisterDeviceTokenRequest request) {
    if (request.token() == null || request.token().isBlank()) {
      return ResponseEntity.badRequest()
          .body(
              ApiErrorResponse.of(
                  ApiError.builder()
                      .status(400)
                      .message("Token is required")
                      .code("INVALID_DEVICE_TOKEN_REQUEST")
                      .build()));
    }
    if (request.platform() == null) {
      return ResponseEntity.badRequest()
          .body(
              ApiErrorResponse.of(
                  ApiError.builder()
                      .status(400)
                      .message("Platform is required (IOS or ANDROID)")
                      .code("INVALID_DEVICE_TOKEN_REQUEST")
                      .build()));
    }
    User currentUser = (User) getAuthentication().getPrincipal();
    var saved = deviceTokenService.registerToken(currentUser, request);
    return ResponseEntity.ok(ApiSuccessResponse.of(saved, "Device token registered"));
  }

  @PatchMapping
  public ResponseEntity<ApiResponse> updateNotificationsEnabled(
      @RequestBody UpdateDeviceTokenRequest request) {
    if (request.token() == null || request.token().isBlank()) {
      return ResponseEntity.badRequest()
          .body(
              ApiErrorResponse.of(
                  ApiError.builder()
                      .status(400)
                      .message("Token is required")
                      .code("INVALID_DEVICE_TOKEN_REQUEST")
                      .build()));
    }
    User currentUser = (User) getAuthentication().getPrincipal();
    var updated =
        deviceTokenService.updateNotificationsEnabled(
            currentUser, request.token(), request.notificationsEnabled());
    if (updated.isEmpty()) {
      return ResponseEntity.status(404)
          .body(
              ApiErrorResponse.of(
                  ApiError.builder()
                      .status(404)
                      .message("Device token not found for this user")
                      .code("DEVICE_TOKEN_NOT_FOUND")
                      .build()));
    }
    return ResponseEntity.ok(
        ApiSuccessResponse.of(updated.get(), "Notification preference updated"));
  }

  private Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }
}
