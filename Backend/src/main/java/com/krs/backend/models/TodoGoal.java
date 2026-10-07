package com.krs.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "todo_goals", schema = "krs_schema")
@Data
public class TodoGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_description", columnDefinition = "TEXT", nullable = false)
    private String taskDescription;

    public enum Priority {
        HIGH, MEDIUM, LOW
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Priority priority;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "is_done")
    private Boolean isDone = false;
    
    @Column(name = "done_note", columnDefinition = "TEXT")
    private String doneNote;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "done_date")
    private LocalDateTime doneDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "done_by")
    private User doneBy;

    @PrePersist
    protected void onCreate() {
        ZoneId zone = ZoneId.systemDefault();
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now(zone);
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now(zone);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneId.systemDefault());
    }
}
