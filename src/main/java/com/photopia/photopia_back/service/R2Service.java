package com.photopia.photopia_back.service;

import com.photopia.photopia_back.dto.PresignedUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@Service
public class R2Service {

    private final S3Presigner s3Presigner;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Value("${r2.secret-key}")
    private String secretKey;

    public R2Service(S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }

    public PresignedUrlResponse generatePresignedUrl(String contentType, UUID userId) {
        String key = UUID.randomUUID().toString();

        // 1. Determine extensions
        String extension = "";
        String previewExtension = ".jpg"; // Previews are usually JPEGs
        if (contentType.equals("image/jpeg"))
            extension = ".jpg";
        else if (contentType.equals("image/png"))
            extension = ".png";
        else if (contentType.equals("video/mp4"))
            extension = ".mp4";

        String originalKey = key + extension;
        String previewKey = key + "-preview" + previewExtension;

        // 2. Generate Presigned URL for Original File
        String originalUrl = generateSinglePresignedUrl(originalKey, contentType);

        // 3. Generate Presigned URL for Preview File (Assuming JPEG for simplicity)
        String previewUrl = generateSinglePresignedUrl(previewKey, "image/jpeg");

        // 4. Generate Signatures
        String signature = sign(originalKey + ":" + userId);
        String previewSignature = sign(previewKey + ":" + userId);

        return new PresignedUrlResponse(
                originalUrl,
                originalKey,
                signature,
                previewUrl,
                previewKey,
                previewSignature);
    }

    private String generateSinglePresignedUrl(String key, String contentType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return presignedRequest.url().toString();
    }

    public String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signedBytes = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(signedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error signing data", e);
        }
    }

    public boolean verifySignature(String key, String userId, String signature) {
        String expectedSignature = sign(key + ":" + userId);
        return expectedSignature.equals(signature);
    }

    public String generatePresignedGetUrl(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        // If it's already a full URL (legacy), return it as is
        if (key.startsWith("http")) {
            return key;
        }

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1)) // Valid for 1 hour
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }
}
