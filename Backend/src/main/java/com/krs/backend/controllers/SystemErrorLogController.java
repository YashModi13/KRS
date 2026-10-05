package com.krs.backend.controllers;

import com.krs.backend.models.SystemErrorLog;
import com.krs.backend.services.SystemErrorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/error-logs")
@RequiredArgsConstructor
public class SystemErrorLogController {

    private final SystemErrorLogService errorLogService;

    @GetMapping
    public ResponseEntity<Page<SystemErrorLog>> getErrorLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer statusCode) {
        return ResponseEntity.ok(errorLogService.getErrorLogs(page, size, search, statusCode));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearLogs() {
        errorLogService.clearAllLogs();
        return ResponseEntity.noContent().build();
    }
}
