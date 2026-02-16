package com.photopia.photopia_back.repository;

import com.photopia.photopia_back.model.DeviceToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {

  Optional<DeviceToken> findByToken(String token);

  Optional<DeviceToken> findByUserIdAndToken(UUID userId, String token);

  List<DeviceToken> findByUserIdAndNotificationsEnabledTrue(UUID userId);
}
