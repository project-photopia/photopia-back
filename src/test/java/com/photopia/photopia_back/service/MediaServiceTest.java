package com.photopia.photopia_back.service;

import com.photopia.photopia_back.dto.MediaRegisterRequest;
import com.photopia.photopia_back.model.Capsule;
import com.photopia.photopia_back.model.Media;
import com.photopia.photopia_back.model.User;
import com.photopia.photopia_back.repository.CapsuleMemberRepository;
import com.photopia.photopia_back.repository.CapsuleRepository;
import com.photopia.photopia_back.repository.MediaRepository;
import com.photopia.photopia_back.service.PushNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

        @Mock
        private MediaRepository mediaRepository;

        @Mock
        private CapsuleRepository capsuleRepository;

        @Mock
        private R2Service r2Service;

        @Mock
        private CapsuleMemberRepository capsuleMemberRepository;

        @Mock
        private PushNotificationService pushNotificationService;

        @Mock
        private io.micrometer.core.instrument.MeterRegistry meterRegistry;

        @InjectMocks
        private MediaService mediaService;

        @Test
        void registerMedia_ShouldSucceed_WhenSignaturesAreValid() {
                // Arrange
                User user = new User();
                user.setId(UUID.randomUUID());

                UUID capsuleId = UUID.randomUUID();
                Capsule capsule = new Capsule();
                capsule.setId(capsuleId);

                MediaRegisterRequest request = new MediaRegisterRequest(
                                "key.jpg",
                                "preview.jpg",
                                "valid-sig",
                                "valid-preview-sig",
                                capsuleId,
                                1920,
                                1080,
                                48.8,
                                2.3,
                                Instant.now(),
                                "image/jpeg");

                when(r2Service.verifySignature(request.originalKey(), user.getId().toString(), request.signature()))
                                .thenReturn(true);
                when(r2Service.verifySignature(request.previewKey(), user.getId().toString(),
                                request.previewSignature()))
                                .thenReturn(true);
                when(capsuleRepository.findById(capsuleId)).thenReturn(Optional.of(capsule));
                when(capsuleMemberRepository.existsByCapsuleIdAndUserId(capsuleId, user.getId()))
                                .thenReturn(true);
                when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

                io.micrometer.core.instrument.Counter mockCounter = mock(io.micrometer.core.instrument.Counter.class);
                when(meterRegistry.counter(eq("photopia.media.uploaded"), eq("type"), anyString()))
                                .thenReturn(mockCounter);

                // Act
                Media result = mediaService.registerMedia(request, user);

                // Assert
                assertNotNull(result);
                assertEquals(request.originalKey(), result.getOriginalUrl());
                assertEquals(user, result.getUser());
                assertEquals(capsule, result.getCapsule());
                verify(mediaRepository).save(any(Media.class));
        }

        @Test
        void registerMedia_ShouldThrowException_WhenOriginalSignatureIsInvalid() {
                // Arrange
                User user = new User();
                user.setId(UUID.randomUUID());

                MediaRegisterRequest request = new MediaRegisterRequest(
                                "key.jpg",
                                "preview.jpg",
                                "invalid-sig",
                                "valid-preview-sig",
                                UUID.randomUUID(),
                                1920,
                                1080,
                                48.8,
                                2.3,
                                Instant.now(),
                                "image/jpeg");

                when(r2Service.verifySignature(anyString(), anyString(), eq("invalid-sig"))).thenReturn(false);

                // Act & Assert
                assertThrows(RuntimeException.class, () -> mediaService.registerMedia(request, user));
                verify(mediaRepository, never()).save(any(Media.class));
        }

        @Test
        void registerMedia_ShouldThrowException_WhenPreviewSignatureIsInvalid() {
                // Arrange
                User user = new User();
                user.setId(UUID.randomUUID());

                MediaRegisterRequest request = new MediaRegisterRequest(
                                "key.jpg",
                                "preview.jpg",
                                "valid-sig",
                                "invalid-preview-sig",
                                UUID.randomUUID(),
                                1920,
                                1080,
                                48.8,
                                2.3,
                                Instant.now(),
                                "image/jpeg");

                when(r2Service.verifySignature(eq("key.jpg"), anyString(), anyString())).thenReturn(true);
                when(r2Service.verifySignature(eq("preview.jpg"), anyString(), eq("invalid-preview-sig")))
                                .thenReturn(false);

                // Act & Assert
                assertThrows(RuntimeException.class, () -> mediaService.registerMedia(request, user));
                verify(mediaRepository, never()).save(any(Media.class));
        }

        @Test
        void registerMedia_ShouldThrowException_WhenCapsuleNotFound() {
                // Arrange
                User user = new User();
                user.setId(UUID.randomUUID());
                UUID capsuleId = UUID.randomUUID();

                MediaRegisterRequest request = new MediaRegisterRequest(
                                "key.jpg",
                                "preview.jpg",
                                "valid-sig",
                                "valid-preview-sig",
                                capsuleId,
                                1920,
                                1080,
                                48.8,
                                2.3,
                                Instant.now(),
                                "image/jpeg");

                when(r2Service.verifySignature(anyString(), anyString(), anyString())).thenReturn(true);
                when(capsuleRepository.findById(capsuleId)).thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(RuntimeException.class, () -> mediaService.registerMedia(request, user));
                verify(mediaRepository, never()).save(any(Media.class));
        }

        @Test
        void getMediaById_ShouldReturnMedia_WhenFound() {
                // Arrange
                UUID mediaId = UUID.randomUUID();
                Media media = new Media();
                media.setId(mediaId);

                when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(media));

                // Act
                Media result = mediaService.getMediaById(mediaId);

                // Assert
                assertNotNull(result);
                assertEquals(mediaId, result.getId());
        }

        @Test
        void getMediaById_ShouldThrowException_WhenNotFound() {
                // Arrange
                UUID mediaId = UUID.randomUUID();
                when(mediaRepository.findById(mediaId)).thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(RuntimeException.class, () -> mediaService.getMediaById(mediaId));
        }

        @Test
        void deleteMedia_ShouldSucceed_WhenUserIsOwner() {
                // Arrange
                UUID mediaId = UUID.randomUUID();
                User user = new User();
                user.setId(UUID.randomUUID());

                Media media = new Media();
                media.setId(mediaId);
                media.setUser(user);

                when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(media));

                // Act
                mediaService.deleteMedia(mediaId, user);

                // Assert
                verify(mediaRepository).delete(media);
        }

        @Test
        void deleteMedia_ShouldThrowException_WhenUserIsNotOwner() {
                // Arrange
                UUID mediaId = UUID.randomUUID();
                User owner = new User();
                owner.setId(UUID.randomUUID());

                User otherUser = new User();
                otherUser.setId(UUID.randomUUID());

                Media media = new Media();
                media.setId(mediaId);
                media.setUser(owner);

                when(mediaRepository.findById(mediaId)).thenReturn(Optional.of(media));

                // Act & Assert
                assertThrows(RuntimeException.class, () -> mediaService.deleteMedia(mediaId, otherUser));
                verify(mediaRepository, never()).delete(any(Media.class));
        }

        @Test
        void getMediasByCapsuleId_ShouldReturnList() {
                // Arrange
                UUID capsuleId = UUID.randomUUID();
                UUID userId = UUID.randomUUID();
                when(capsuleMemberRepository.existsByCapsuleIdAndUserId(capsuleId, userId))
                                .thenReturn(true);
                when(mediaRepository.findByCapsuleIdOrderByTakenAtDesc(capsuleId))
                                .thenReturn(java.util.Collections.emptyList());

                // Act
                var result = mediaService.getMediasByCapsuleId(capsuleId, userId);

                // Assert
                assertNotNull(result);
                assertTrue(result.isEmpty());
                verify(mediaRepository).findByCapsuleIdOrderByTakenAtDesc(capsuleId);
        }

        @Test
        void getPresignedUrl_ShouldReturnUrl() {
                // Arrange
                UUID userId = UUID.randomUUID();
                String contentType = "image/jpeg";
                com.photopia.photopia_back.dto.PresignedUrlResponse response = new com.photopia.photopia_back.dto.PresignedUrlResponse(
                                "url", "key", "sig", "previewUrl", "previewKey", "previewSig");

                when(r2Service.generatePresignedUrl(contentType, userId)).thenReturn(response);

                // Act
                var result = mediaService.getPresignedUrl(contentType, userId);

                // Assert
                assertNotNull(result);
                assertEquals("url", result.uploadUrl());
        }
}
