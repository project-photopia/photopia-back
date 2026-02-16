package com.photopia.photopia_back.health;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Component
@RequiredArgsConstructor
public class R2HealthIndicator implements HealthIndicator {

    private final S3Client s3Client;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Override
    public Health health() {
        try {
            // Check if the bucket exists and is accessible
            HeadBucketRequest request = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.headBucket(request);

            return Health.up()
                    .withDetail("bucket", bucketName)
                    .withDetail("service", "R2 Storage")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("bucket", bucketName)
                    .withDetail("service", "R2 Storage")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
