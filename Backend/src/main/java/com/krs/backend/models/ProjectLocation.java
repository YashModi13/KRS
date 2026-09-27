package com.krs.backend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
    
    @Column(name = "created_at") private LocalDateTime createdAt;
    @Column(name = "updated_at") private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
    public String getHead() { return head; }
    public void setHead(String head) { this.head = head; }
    public String getRepairing() { return repairing; }
    public void setRepairing(String repairing) { this.repairing = repairing; }
    public String getNewAcr() { return newAcr; }
    public void setNewAcr(String newAcr) { this.newAcr = newAcr; }
    public String getNewMdmSqm() { return newMdmSqm; }
    public void setNewMdmSqm(String newMdmSqm) { this.newMdmSqm = newMdmSqm; }
    public String getNewCwRmt() { return newCwRmt; }
    public void setNewCwRmt(String newCwRmt) { this.newCwRmt = newCwRmt; }
    public String getGtb() { return gtb; }
    public void setGtb(String gtb) { this.gtb = gtb; }
    public String getBtb() { return btb; }
    public void setBtb(String btb) { this.btb = btb; }
    public String getCwsnToilet() { return cwsnToilet; }
    public void setCwsnToilet(String cwsnToilet) { this.cwsnToilet = cwsnToilet; }
    public String getShed() { return shed; }
    public void setShed(String shed) { this.shed = shed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
