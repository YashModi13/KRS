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
@Table(name = "ra_bills", schema = "krs_schema")
public class RaBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnoreProperties({"locations", "raBills", "approvals"})
    private Project project;

    @Column(name = "total_ra_bill_amount")
    private BigDecimal totalRaBillAmount;

    @Column(name = "invoice_submitted")
    private Boolean invoiceSubmitted;

    @Column(name = "bill_check_person_name")
    private String billCheckPersonName;

    @Column(name = "dept_ra_bill_copy_file")
    private String deptRaBillCopyFile;

    @Column(name = "bill_deposited")
    private Boolean billDeposited;

    // --- Extended Fields from RA Bill Checklist ---
    @Column(name = "ra_bill_number")
    private String raBillNumber;

    @Column(name = "name_of_agency")
    private String nameOfAgency;

    @Column(name = "pan_number")
    private String panNumber;

    @Column(name = "last_bill_paid_amount")
    private BigDecimal lastBillPaidAmount;

    @Column(name = "date_of_last_payment")
    private LocalDate dateOfLastPayment;

    @Column(name = "amount_paid_upto_previous_bills")
    private BigDecimal amountPaidUptoPreviousBills;

    @Column(name = "total_amount_of_current_ra_bill")
    private BigDecimal totalAmountOfCurrentRaBill;

    @Column(name = "total_amount_upto_this_bill")
    private BigDecimal totalAmountUptoThisBill;

    @Column(name = "date_of_bill")
    private LocalDate dateOfBill;

    @Column(name = "gross_bill_amount")
    private BigDecimal grossBillAmount;

    @Column(name = "cgst_9_percent")
    private BigDecimal cgst9Percent;

    @Column(name = "sgst_9_percent")
    private BigDecimal sgst9Percent;

    @Column(name = "total_gst")
    private BigDecimal totalGst;

    @Column(name = "net_bill_amount")
    private BigDecimal netBillAmount;

    @Column(name = "rm_5_percent")
    private BigDecimal rm5Percent;

    @Column(name = "tds_2_percent")
    private BigDecimal tds2Percent;

    @Column(name = "labour_cess_1_percent")
    private BigDecimal labourCess1Percent;

    @Column(name = "cgst_1_percent")
    private BigDecimal cgst1Percent;

    @Column(name = "sgst_1_percent")
    private BigDecimal sgst1Percent;

    @Column(name = "withheld_amount")
    private BigDecimal withheldAmount;

    @Column(name = "liquidity_damage")
    private BigDecimal liquidityDamage;

    @Column(name = "net_payment")
    private BigDecimal netPayment;

    @Column(name = "pre_audit_remarks", columnDefinition = "TEXT")
    private String preAuditRemarks;

    @Column(name = "status")
    private String status;

    @Column(name = "passed_date")
    private LocalDate passedDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
