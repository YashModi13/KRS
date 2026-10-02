package com.krs.backend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

@Service
public class S3StorageService {

    @Value("${aws.s3.endpoint:https://br-soft-pine-b3d1as2g.storage.c-4.ap-southeast-1.aws.neon.tech}")
    private String endpointUrl;

    @Value("${aws.s3.access-key:nak_live_c2400b905def460e8cacb7cdb3e2bc33}")
    private String accessKey;

    @Value("${aws.s3.secret-key:nsk_live_8d88984f809900e141ce8c9ccdaaa6c3bcdf0af9a40850b9783c5094b3f9bfa5}")
    private String secretKey;

    @Value("${aws.s3.region:ap-southeast-1}")
    private String region;

    @Value("${aws.s3.enabled:true}")
    private boolean enabled;

    @Value("${aws.s3.bucket:assets}")
    private String bucketName;

    private S3Client s3Client;
    private S3Presigner s3Presigner;

    public boolean isEnabled() {
        return enabled && s3Client != null;
    }

    @PostConstruct
    public void init() {
        if (!enabled) {
            return;
        }
        if (endpointUrl != null && !endpointUrl.isBlank()) {

            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

            S3Configuration serviceConfiguration = S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build();

            this.s3Client = S3Client.builder()
                    .endpointOverride(URI.create(endpointUrl))
                    .region(Region.of(region))
                    .credentialsProvider(credentialsProvider)
                    .serviceConfiguration(serviceConfiguration)
                    .build();

            this.s3Presigner = S3Presigner.builder()
                    .endpointOverride(URI.create(endpointUrl))
                    .region(Region.of(region))
                    .credentialsProvider(credentialsProvider)
                    .serviceConfiguration(serviceConfiguration)
                    .build();
        }
    }

    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType != null ? contentType : "application/octet-stream")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
        return generatePresignedUrl(key);
    }

    public String generatePresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        return presignedRequest.url().toString();
    }
}
