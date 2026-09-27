package com.krs.backend.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "projects", schema = "krs_schema")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_order_number")
    private String workOrderNumber;

    @Column(name = "negotiation_letter_file")
    private String negotiationLetterFile;

    @Column(name = "village_name")
    private String villageName;

    @Column(name = "security_deposit_amount")
    private BigDecimal securityDepositAmount;

    @Column(name = "security_deposit_type")
    private String securityDepositType;

    @Column(name = "security_deposit_file")
    private String securityDepositFile;

    @Column(name = "retention_money_per_bill")
    private BigDecimal retentionMoneyPerBill;

    @Column(name = "extra_excess_amount")
    private BigDecimal extraExcessAmount;

    @Column(name = "time_limit_extension")
    private LocalDate timeLimitExtension;

    @Column(name = "completion_date_actual")
    private LocalDate completionDateActual;

    @Column(name = "completion_date_extended")
    private LocalDate completionDateExtended;

    @Column(name = "letter_by_krs_file")
    private String letterByKrsFile;

    @Column(name = "letter_by_dept_file")
    private String letterByDeptFile;

    // --- Extended Fields from Tender Details ---
    @Column(name = "department_name")
    private String departmentName;

    @Column(name = "date_of_sub")
    private LocalDate dateOfSub;

    @Column(name = "package_no")
    private String packageNo;

    @Column(name = "tender_id")
    private String tenderId;

    @Column(name = "name_of_work", columnDefinition = "TEXT")
    private String nameOfWork;

    @Column(name = "dd_no")
    private String ddNo;

    @Column(name = "emd_amt")
    private BigDecimal emdAmt;

    @Column(name = "estimated_tender_cost")
    private BigDecimal estimatedTenderCost;

    @Column(name = "tendered_cost")
    private BigDecimal tenderedCost;

    @Column(name = "work_order_date")
    private LocalDate workOrderDate;

    @Column(name = "defects_liability_period")
    private String defectsLiabilityPeriod;

    @Column(name = "security_deposit_date")
    private LocalDate securityDepositDate;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectLocation> locations;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { 
        createdAt = LocalDateTime.now(); 
        updatedAt = LocalDateTime.now(); 
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }

    public String getNegotiationLetterFile() { return negotiationLetterFile; }
    public void setNegotiationLetterFile(String negotiationLetterFile) { this.negotiationLetterFile = negotiationLetterFile; }

    public String getVillageName() { return villageName; }
    public void setVillageName(String villageName) { this.villageName = villageName; }

    public BigDecimal getSecurityDepositAmount() { return securityDepositAmount; }
    public void setSecurityDepositAmount(BigDecimal securityDepositAmount) { this.securityDepositAmount = securityDepositAmount; }

    public String getSecurityDepositType() { return securityDepositType; }
    public void setSecurityDepositType(String securityDepositType) { this.securityDepositType = securityDepositType; }

    public String getSecurityDepositFile() { return securityDepositFile; }
    public void setSecurityDepositFile(String securityDepositFile) { this.securityDepositFile = securityDepositFile; }

    public BigDecimal getRetentionMoneyPerBill() { return retentionMoneyPerBill; }
    public void setRetentionMoneyPerBill(BigDecimal retentionMoneyPerBill) { this.retentionMoneyPerBill = retentionMoneyPerBill; }

    public BigDecimal getExtraExcessAmount() { return extraExcessAmount; }
    public void setExtraExcessAmount(BigDecimal extraExcessAmount) { this.extraExcessAmount = extraExcessAmount; }

    public LocalDate getTimeLimitExtension() { return timeLimitExtension; }
    public void setTimeLimitExtension(LocalDate timeLimitExtension) { this.timeLimitExtension = timeLimitExtension; }

    public LocalDate getCompletionDateActual() { return completionDateActual; }
    public void setCompletionDateActual(LocalDate completionDateActual) { this.completionDateActual = completionDateActual; }

    public LocalDate getCompletionDateExtended() { return completionDateExtended; }
    public void setCompletionDateExtended(LocalDate completionDateExtended) { this.completionDateExtended = completionDateExtended; }

    public String getLetterByKrsFile() { return letterByKrsFile; }
    public void setLetterByKrsFile(String letterByKrsFile) { this.letterByKrsFile = letterByKrsFile; }

    public String getLetterByDeptFile() { return letterByDeptFile; }
    public void setLetterByDeptFile(String letterByDeptFile) { this.letterByDeptFile = letterByDeptFile; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public LocalDate getDateOfSub() { return dateOfSub; }
    public void setDateOfSub(LocalDate dateOfSub) { this.dateOfSub = dateOfSub; }

    public String getPackageNo() { return packageNo; }
    public void setPackageNo(String packageNo) { this.packageNo = packageNo; }

    public String getTenderId() { return tenderId; }
    public void setTenderId(String tenderId) { this.tenderId = tenderId; }

    public String getNameOfWork() { return nameOfWork; }
    public void setNameOfWork(String nameOfWork) { this.nameOfWork = nameOfWork; }

    public String getDdNo() { return ddNo; }
    public void setDdNo(String ddNo) { this.ddNo = ddNo; }

    public BigDecimal getEmdAmt() { return emdAmt; }
    public void setEmdAmt(BigDecimal emdAmt) { this.emdAmt = emdAmt; }

    public BigDecimal getEstimatedTenderCost() { return estimatedTenderCost; }
    public void setEstimatedTenderCost(BigDecimal estimatedTenderCost) { this.estimatedTenderCost = estimatedTenderCost; }

    public BigDecimal getTenderedCost() { return tenderedCost; }
    public void setTenderedCost(BigDecimal tenderedCost) { this.tenderedCost = tenderedCost; }

    public LocalDate getWorkOrderDate() { return workOrderDate; }
    public void setWorkOrderDate(LocalDate workOrderDate) { this.workOrderDate = workOrderDate; }

    public String getDefectsLiabilityPeriod() { return defectsLiabilityPeriod; }
    public void setDefectsLiabilityPeriod(String defectsLiabilityPeriod) { this.defectsLiabilityPeriod = defectsLiabilityPeriod; }

    public LocalDate getSecurityDepositDate() { return securityDepositDate; }
    public void setSecurityDepositDate(LocalDate securityDepositDate) { this.securityDepositDate = securityDepositDate; }

    public List<ProjectLocation> getLocations() { return locations; }
    public void setLocations(List<ProjectLocation> locations) { this.locations = locations; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
