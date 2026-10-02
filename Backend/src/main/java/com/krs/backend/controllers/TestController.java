package com.krs.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

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
}
