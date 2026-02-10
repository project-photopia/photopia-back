package com.photopia.photopia_back.service;

import com.photopia.photopia_back.dto.PresignedUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class R2ServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    private R2Service r2Service;

    private final String SECRET_KEY = "mySecretKey";
    private final String BUCKET_NAME = "myBucket";

    @BeforeEach
    void setUp() {
        r2Service = new R2Service(s3Presigner);
        ReflectionTestUtils.setField(r2Service, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(r2Service, "secretKey", SECRET_KEY);
    }

    @Test
    void sign_ShouldGenerateValidHMAC() {
        String data = "test-data";
        String signature = r2Service.sign(data);

        assertNotNull(signature);
        assertFalse(signature.isEmpty());
    }

    @Test
    void verifySignature_ShouldReturnTrueForValidSignature() {
        String key = "test-key";
        String userId = UUID.randomUUID().toString();
        String signature = r2Service.sign(key + ":" + userId);

        boolean isValid = r2Service.verifySignature(key, userId, signature);

        assertTrue(isValid);
    }

    @Test
    void verifySignature_ShouldReturnFalseForInvalidSignature() {
        String key = "test-key";
        String userId = UUID.randomUUID().toString();
        String signature = "invalid-signature";

        boolean isValid = r2Service.verifySignature(key, userId, signature);

        assertFalse(isValid);
    }

    @Test
    void generatePresignedUrl_ShouldReturnResponseWithSignatures() throws Exception {
        // Mock S3Presigner behavior
        PresignedPutObjectRequest presignedRequest = mock(PresignedPutObjectRequest.class);
        when(presignedRequest.url()).thenReturn(new URL("https://example.com/upload"));
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedRequest);

        UUID userId = UUID.randomUUID();
        PresignedUrlResponse response = r2Service.generatePresignedUrl("image/jpeg", userId);

        assertNotNull(response);
        assertNotNull(response.uploadUrl());
        assertNotNull(response.key());
        assertNotNull(response.signature());
        assertNotNull(response.previewUploadUrl());
        assertNotNull(response.previewKey());
        assertNotNull(response.previewSignature());

        // Verify that generated signatures are valid
        assertTrue(r2Service.verifySignature(response.key(), userId.toString(), response.signature()));
        assertTrue(r2Service.verifySignature(response.previewKey(), userId.toString(), response.previewSignature()));

        // Verify extensions
        assertTrue(response.key().endsWith(".jpg"));
        assertTrue(response.previewKey().endsWith(".jpg"));
    }
}
