package com.photopia.photopia_back.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.photopia.photopia_back.model.DeviceToken;
import com.photopia.photopia_back.repository.CapsuleMemberRepository;
import com.photopia.photopia_back.repository.DeviceTokenRepository;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

  private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

  private final DeviceTokenRepository deviceTokenRepository;
  private final CapsuleMemberRepository capsuleMemberRepository;
  private final EmailService emailService;

  public PushNotificationService(
      DeviceTokenRepository deviceTokenRepository,
      CapsuleMemberRepository capsuleMemberRepository,
      EmailService emailService) {
    this.deviceTokenRepository = deviceTokenRepository;
    this.capsuleMemberRepository = capsuleMemberRepository;
    this.emailService = emailService;
  }

  public void notifyCapsuleMembersNewMedia(
      UUID capsuleId, UUID excludeUserId, String uploaderName, String capsuleName) {
    if (!isFirebaseInitialized()) {
      log.info("[Push] Firebase not initialized, skipping");
      return;
    }

    List<UUID> memberUserIds = capsuleMemberRepository.findByCapsuleId(capsuleId).stream()
        .map((m) -> m.getUserId())
        .filter((id) -> !id.equals(excludeUserId))
        .toList();

    if (memberUserIds.isEmpty()) {
      return;
    }

    List<String> tokens = memberUserIds.stream()
        .flatMap(
            (userId) -> deviceTokenRepository.findByUserIdAndNotificationsEnabledTrue(userId).stream())
        .map(DeviceToken::getToken)
        .toList();

    if (tokens.isEmpty()) {
      log.info("[Push] No device tokens found for capsule members (users need to enable notifications)");
    } else {
      log.info("[Push] Sending to {} device(s) for capsule {}", tokens.size(), capsuleId);

      String title = "New media 📸";
      String body = uploaderName != null && !uploaderName.isBlank()
          ? String.format("%s added a photo in %s", uploaderName, capsuleName)
          : String.format("New photo in %s", capsuleName);

      for (String token : tokens) {
        sendToToken(token, title, body, capsuleId.toString());
      }
    }

    // Send Emails
    try {
      List<String> emails = capsuleMemberRepository.findMemberEmails(capsuleId, excludeUserId);
      emailService.sendNewMediaNotification(emails, capsuleName, uploaderName);
    } catch (Exception e) {
      log.error("[Email] Failed to send emails", e);
    }
  }

  private void sendToToken(String token, String title, String body, String dataCapsuleId) {
    try {
      Message message = Message.builder()
          .setToken(token)
          .setNotification(
              Notification.builder().setTitle(title).setBody(body).build())
          .putData("capsuleId", dataCapsuleId)
          .putData("type", "new_media")
          .build();

      String messageId = FirebaseMessaging.getInstance().send(message);
      log.info("[Push] Sent successfully, messageId={}", messageId);
    } catch (FirebaseMessagingException e) {
      log.warn("[Push] Failed to send: {}", e.getMessage());
    }
  }

  // Protected for testing
  protected boolean isFirebaseInitialized() {
    return !FirebaseApp.getApps().isEmpty();
  }
}
