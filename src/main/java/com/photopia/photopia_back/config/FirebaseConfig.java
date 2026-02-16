package com.photopia.photopia_back.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy(false)
public class FirebaseConfig {

  private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

  @Value("${firebase.service-account-path:}")
  private String serviceAccountPath;

  @PostConstruct
  public void init() {
    log.info("[Firebase] Config loading, path={}", serviceAccountPath != null ? "set" : "null");
    if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
      log.info("[Firebase] No service account path configured (set FIREBASE_SERVICE_ACCOUNT_PATH in .env), push disabled");
      return;
    }
    try {
      FileInputStream serviceAccount = new FileInputStream(serviceAccountPath);
      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .build();
      FirebaseApp.initializeApp(options);
      log.info("Firebase initialized successfully");
    } catch (IOException e) {
      log.warn("Firebase initialization failed: {}", e.getMessage());
    }
  }
}
