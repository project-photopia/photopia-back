package com.photopia.photopia_back.service;

import com.photopia.photopia_back.dto.MediaRegisterRequest;
import com.photopia.photopia_back.model.Capsule;
import com.photopia.photopia_back.model.Media;
import com.photopia.photopia_back.model.User;
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
        private PushNotificationService pushNotificationService;

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
                when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
}
