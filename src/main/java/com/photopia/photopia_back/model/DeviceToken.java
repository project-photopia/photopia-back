package com.photopia.photopia_back.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "device_tokens",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"token"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceToken {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore
  private User user;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String token;

  @Column(nullable = false, length = 10)
  @Enumerated(EnumType.STRING)
  private Platform platform;

  @Column(name = "notifications_enabled", nullable = false, columnDefinition = "boolean default true")
  @Builder.Default
  private boolean notificationsEnabled = true;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  public enum Platform {
    IOS,
    ANDROID
  }
}
