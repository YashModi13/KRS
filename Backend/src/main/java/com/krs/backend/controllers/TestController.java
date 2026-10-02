package com.krs.backend.controllers;

import com.krs.backend.services.S3StorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final S3StorageService s3StorageService;

    public TestController(S3StorageService s3StorageService) {
        this.s3StorageService = s3StorageService;
    }

    /**
     * Public test endpoint bypassing authentication.
     * Returns boolean true statically.
     */
    @GetMapping
    public ResponseEntity<Boolean> testEndpoint() {
        return ResponseEntity.ok(true);
    }

    /**
     * Detailed status test endpoint.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> testStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("success", true);
        response.put("message", "KRS Backend is live and running!");
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint to verify Neon Object Storage (S3) upload and URL generation
     */
    @GetMapping("/s3")
    public ResponseEntity<Map<String, Object>> testS3() {
        Map<String, Object> response = new HashMap<>();
        if (s3StorageService == null || !s3StorageService.isEnabled()) {
            response.put("status", "DISABLED");
            response.put("message", "S3 Storage is currently disabled (aws.s3.enabled=false)");
            return ResponseEntity.ok(response);
        }
        try {
            String testContent = "KRS Construction Neon S3 Storage Test - " + System.currentTimeMillis();
            byte[] bytes = testContent.getBytes(StandardCharsets.UTF_8);
            String key = "uploads/test_" + System.currentTimeMillis() + ".txt";

            String presignedUrl = s3StorageService.uploadFile(
                    key,
                    new ByteArrayInputStream(bytes),
                    bytes.length,
                    "text/plain"
            );

            response.put("status", "SUCCESS");
            response.put("bucket", "assets");
            response.put("fileKey", key);
            response.put("presignedUrl", presignedUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
