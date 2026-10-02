package com.krs.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "approvals", schema = "krs_schema")
public class Approval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnoreProperties({"locations", "raBills", "approvals"})
    private Project project;

    @Column(name = "approval_type")
    private String approvalType;

    @Column(name = "approval_number")
    private String approvalNumber;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "time_limit_extension_date")
    private LocalDate timeLimitExtensionDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "approval_letter_file")
    private String approvalLetterFile;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
