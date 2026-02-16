package com.photopia.photopia_back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import java.net.URI;

@Configuration
public class R2Config {

        @Value("${r2.account-id}")
        private String accountId;

        @Value("${r2.access-key}")
        private String accessKey;

        @Value("${r2.secret-key}")
        private String secretKey;

        @Bean
        public software.amazon.awssdk.services.s3.presigner.S3Presigner s3Presigner() {
                AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

                return software.amazon.awssdk.services.s3.presigner.S3Presigner.builder()
                                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                                .region(Region.US_EAST_1)
                                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                                .build();
        }

        @Bean
        public software.amazon.awssdk.services.s3.S3Client s3Client() {
                AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

                return software.amazon.awssdk.services.s3.S3Client.builder()
                                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                                .region(Region.US_EAST_1)
                                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                                .build();
        }
}
