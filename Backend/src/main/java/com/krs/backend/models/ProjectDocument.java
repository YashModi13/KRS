package com.krs.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "project_documents", schema = "krs_schema")
public class ProjectDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private Project project;

    @Column(name = "document_name")
    private String documentName;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "notes")
    private String notes;

    @Column(name = "uploaded_date")
    private LocalDateTime uploadedDate;

    @Column(name = "location_path")
    private String locationPath;

    @Column(name = "create_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (uploadedDate == null) uploadedDate = LocalDateTime.now();
        if (createdDate == null) createdDate = LocalDateTime.now();
        if (updateDate == null) updateDate = LocalDateTime.now();
        if (isActive == null) isActive = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }
}
