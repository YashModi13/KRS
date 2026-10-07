package com.krs.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Column(name = "department_name_id", nullable = false, length = 2000)
    private String departmentName;

    @Column(name = "date_of_sub")
    private LocalDate dateOfSub;

    @Column(name = "package_no", length = 2000)
    private String packageNo;

    @Column(name = "notice_no", length = 2000)
    private String noticeNo;

    @Column(name = "tender_id", nullable = false)
    private String tenderId;

    @Column(name = "name_of_work", columnDefinition = "TEXT")
    private String nameOfWork;

    @Column(name = "related_to_id")
    private String relatedTo;

    @Column(name = "tender_fee")
    private BigDecimal tenderFee;

    @Column(name = "tender_fee_no", length = 2000)
    private String tenderFeeNo;

    @Column(name = "dd_no")
    private String ddNo;

    @Column(name = "emd_amt")
    private BigDecimal emdAmt;

    @Column(name = "emd_no", length = 2000)
    private String emdNo;

    @Column(name = "estimated_tender_cost")
    private BigDecimal estimatedTenderCost;

    @Column(name = "tendered_cost")
    private BigDecimal tenderedCost;

    @Column(name = "above_below_percentage")
    private BigDecimal aboveBelowPercentage;

    /**
     * Variance % calculated in the SELECT query (not stored).
     * Uses the manually entered percentage if present, otherwise
     * (tenderedCost - estimatedTenderCost) / estimatedTenderCost * 100.
     */
    @org.hibernate.annotations.Formula(
        "COALESCE(above_below_percentage, " +
        "CASE WHEN estimated_tender_cost IS NOT NULL AND estimated_tender_cost <> 0 AND tendered_cost IS NOT NULL " +
        "THEN ROUND((tendered_cost - estimated_tender_cost) * 100.0 / estimated_tender_cost, 2) END)"
    )
    private BigDecimal variancePct;

    @Column(name = "ref_person_id")
    private String refPerson;

    @Column(name = "work_awarded_status")
    private String workAwardedStatus;

    @Column(name = "work_order_date")
    private LocalDate workOrderDate;

    @Column(name = "time_limit", length = 2000)
    private String timeLimit;

    @Column(name = "sd_fdr_no", length = 2000)
    private String sdFdrNo;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "sd_rab_deduction")
    private BigDecimal sdRabDeduction;

    @Column(name = "sd_rab_return_amount")
    private BigDecimal sdRabReturnAmount;

    @Column(name = "additional_deduction", length = 2000)
    private String additionalDeduction;

    @Column(name = "work_completed_amount")
    private BigDecimal workCompletedAmount;

    @Column(name = "pending_work_amount")
    private BigDecimal pendingWorkAmount;

    @Column(name = "defects_liability_period")
    private String defectsLiabilityPeriod;

    @Column(name = "dlp_ended_on")
    private LocalDate dlpEndedOn;

    @Column(name = "security_deposit_date")
    private LocalDate securityDepositDate;

    @Column(name = "emd_return_status")
    private String emdReturnStatus;

    @Column(name = "sd_return_status")
    private String sdReturnStatus;

    @Column(name = "sd_rm_rab_return_status")
    private String sdRmRabReturnStatus;

    @Column(name = "status")
    private String status;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectLocation> locations;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RaBill> raBills;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Approval> approvals;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ProjectDocument> documents;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { 
        createdAt = LocalDateTime.now(ZoneId.systemDefault()); 
        updatedAt = LocalDateTime.now(ZoneId.systemDefault()); 
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(ZoneId.systemDefault()); }
}
