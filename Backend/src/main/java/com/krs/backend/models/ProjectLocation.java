package com.krs.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "project_locations", schema = "krs_schema")
public class ProjectLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private Project project;

    @Column(name = "village_name") private String villageName;
    @Column(name = "taluka") private String taluka;
    @Column(name = "district") private String district;
    @Column(name = "block") private String block;
    @Column(name = "school_id") private String schoolId;
    @Column(name = "school_name") private String schoolName;
    @Column(name = "head") private String head;
    @Column(name = "repairing") private String repairing;
    @Column(name = "new_acr") private String newAcr;
    @Column(name = "new_mdm_sqm") private String newMdmSqm;
    @Column(name = "new_cw_rmt") private String newCwRmt;
    @Column(name = "gtb") private String gtb;
    @Column(name = "btb") private String btb;
    @Column(name = "cwsn_toilet") private String cwsnToilet;
    @Column(name = "shed") private String shed;
    @Column(name = "status") private String status;
    @Column(name = "physical_progress") private BigDecimal physicalProgress;
    @Column(name = "financial_progress") private BigDecimal financialProgress;
    @Column(name = "time_limit") private String timeLimit;
    @Column(name = "start_date") private java.time.LocalDate startDate;
    @Column(name = "closed_date") private java.time.LocalDate closedDate;
    
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
