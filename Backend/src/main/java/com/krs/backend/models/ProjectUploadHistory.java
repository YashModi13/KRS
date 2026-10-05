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
@Table(name = "project_upload_history", schema = "krs_schema")
public class ProjectUploadHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    @Column(name = "total_rows")
    private Integer totalRows;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "failed_count")
    private Integer failedCount;

    @Column(name = "status", nullable = false)
    private String status; // 'SUCCESS', 'PARTIAL_SUCCESS', 'FAILED'

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails; // JSON or formatted text of row failure logs

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (uploadTime == null) {
            uploadTime = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
