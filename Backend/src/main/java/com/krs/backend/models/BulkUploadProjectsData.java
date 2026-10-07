package com.krs.backend.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "bulk_upload_projects_data", schema = "krs_schema")
public class BulkUploadProjectsData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "master_id", nullable = false)
    private Long masterId;

    @Column(name = "date_of_sub")
    private LocalDate dateOfSub;

    @Column(name = "department_name", length = 2000)
    private String departmentName;

    @Column(name = "tender_id")
    private String tenderId;

    @Column(name = "notice_no", length = 2000)
    private String noticeNo;

    @Column(name = "package_no", length = 2000)
    private String packageNo;

    @Column(name = "name_of_work", columnDefinition = "TEXT")
    private String nameOfWork;

    @Column(name = "related_to")
    private String relatedTo;

    @Column(name = "tender_fee")
    private BigDecimal tenderFee;

    @Column(name = "tender_fee_no", length = 2000)
    private String tenderFeeNo;

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

    @Column(name = "ref_person")
    private String refPerson;

    @Column(name = "work_awarded_status")
    private String workAwardedStatus;

    @Column(name = "work_order_number", length = 2000)
    private String workOrderNumber;

    @Column(name = "work_order_date")
    private LocalDate workOrderDate;

    @Column(name = "time_limit", length = 2000)
    private String timeLimit;

    @Column(name = "security_deposit_amount")
    private BigDecimal securityDepositAmount;

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

    @Column(name = "completion_date_actual")
    private LocalDate completionDateActual;

    @Column(name = "defects_liability_period")
    private String defectsLiabilityPeriod;

    @Column(name = "dlp_ended_on")
    private LocalDate dlpEndedOn;

    @Column(name = "emd_return_status")
    private String emdReturnStatus;

    @Column(name = "sd_return_status")
    private String sdReturnStatus;

    @Column(name = "sd_rm_rab_return_status")
    private String sdRmRabReturnStatus;

    @Column(name = "status")
    private String status;

    @Column(name = "is_failed")
    private Boolean isFailed;

    @Column(name = "failed_reason", columnDefinition = "TEXT")
    private String failedReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneId.systemDefault());
        }
        if (isFailed == null) {
            isFailed = false;
        }
    }
}
