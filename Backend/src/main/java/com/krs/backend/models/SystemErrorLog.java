package com.krs.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "system_error_logs", schema = "krs_schema")
public class SystemErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "error_type", nullable = false)
    private String errorType;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "client_ip")
    private String clientIp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
