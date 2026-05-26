package com.photopia.photopia_back.service;

import com.photopia.photopia_back.dto.RegisterDeviceTokenRequest;
import com.photopia.photopia_back.model.DeviceToken;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.DeviceTokenRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DeviceTokenService {

  private final DeviceTokenRepository deviceTokenRepository;

  public DeviceTokenService(DeviceTokenRepository deviceTokenRepository) {
    this.deviceTokenRepository = deviceTokenRepository;
  }

  public DeviceToken registerToken(User user, RegisterDeviceTokenRequest request) {
    boolean enabled = request.enabled() != null ? request.enabled() : true;
    return deviceTokenRepository
        .findByToken(request.token())
        .map(
            existing -> {
              existing.setUser(user);
              existing.setPlatform(request.platform());
              existing.setNotificationsEnabled(enabled);
              return deviceTokenRepository.save(existing);
            })
        .orElseGet(
            () ->
                deviceTokenRepository.save(
                    DeviceToken.builder()
                        .user(user)
                        .token(request.token())
                        .platform(request.platform())
                        .notificationsEnabled(enabled)
                        .build()));
  }

  public Optional<DeviceToken> updateNotificationsEnabled(
      User user, String token, boolean enabled) {
    return deviceTokenRepository
        .findByUserIdAndToken(user.getId(), token)
        .map(
            deviceToken -> {
              deviceToken.setNotificationsEnabled(enabled);
              return deviceTokenRepository.save(deviceToken);
            });
  }
}
