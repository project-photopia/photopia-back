package com.photopia.photopia_back.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@Lazy(false)
public class FirebaseConfig {

  private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

  @Value("${firebase.service-account-json:}")
  private String serviceAccountJson;

  @Value("${firebase.service-account-path:}")
  private String serviceAccountPath;

  @PostConstruct
  public void init() {
    boolean hasJson = serviceAccountJson != null && !serviceAccountJson.isBlank();
    boolean hasPath = serviceAccountPath != null && !serviceAccountPath.isBlank();
    log.info("[Firebase] FIREBASE_SERVICE_ACCOUNT_JSON={}, FIREBASE_SERVICE_ACCOUNT_PATH={}",
        hasJson ? "set" : "empty", hasPath ? "set" : "empty");

    if (hasJson) {
      initFromJson(serviceAccountJson);
      return;
    }
    if (hasPath) {
      initFromFile(serviceAccountPath);
      return;
    }
    log.info("[Firebase] No credentials configured. Set FIREBASE_SERVICE_ACCOUNT_JSON (production) or FIREBASE_SERVICE_ACCOUNT_PATH (local). Push disabled.");
  }

  private void initFromJson(String json) {
    try {
      var stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(stream))
              .build();
      FirebaseApp.initializeApp(options);
      log.info("[Firebase] Initialized successfully from FIREBASE_SERVICE_ACCOUNT_JSON");
    } catch (IOException e) {
      log.warn("[Firebase] Init from JSON failed: {}", e.getMessage());
    }
  }

  private void initFromFile(String path) {
    try {
      FileInputStream serviceAccount = new FileInputStream(path);
      FirebaseOptions options =
          FirebaseOptions.builder()
              .setCredentials(GoogleCredentials.fromStream(serviceAccount))
              .build();
      FirebaseApp.initializeApp(options);
      log.info("[Firebase] Initialized successfully from file");
    } catch (IOException e) {
      log.warn("[Firebase] Init from file failed: {}", e.getMessage());
    }
  }
}
