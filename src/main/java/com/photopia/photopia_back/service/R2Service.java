package com.photopia.photopia_back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class R2Service {

    private final software.amazon.awssdk.services.s3.presigner.S3Presigner s3Presigner;

    @Value("${r2.bucket-name}")
    private String bucketName;

    public R2Service(software.amazon.awssdk.services.s3.presigner.S3Presigner s3Presigner) {
        this.s3Presigner = s3Presigner;
    }

    public com.photopia.photopia_back.dto.PresignedUrlResponse generatePresignedUrl(String contentType) {
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

        return new com.photopia.photopia_back.dto.PresignedUrlResponse(originalUrl, originalKey, previewUrl,
                previewKey);
    }

    private String generateSinglePresignedUrl(String key, String contentType) {
        software.amazon.awssdk.services.s3.model.PutObjectRequest objectRequest = software.amazon.awssdk.services.s3.model.PutObjectRequest
                .builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest presignRequest = software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
                .builder()
                .signatureDuration(java.time.Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest presignedRequest = s3Presigner
                .presignPutObject(presignRequest);

        return presignedRequest.url().toString();
    }

    public String generatePresignedGetUrl(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        // If it's already a full URL (legacy), return it as is
        if (key.startsWith("http")) {
            return key;
        }

        software.amazon.awssdk.services.s3.model.GetObjectRequest objectRequest = software.amazon.awssdk.services.s3.model.GetObjectRequest
                .builder()
                .bucket(bucketName)
                .key(key)
                .build();

        software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest presignRequest = software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
                .builder()
                .signatureDuration(java.time.Duration.ofHours(1)) // Valid for 1 hour
                .getObjectRequest(objectRequest)
                .build();

        software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest presignedRequest = s3Presigner
                .presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }
}
