package com.krs.backend.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ra_bills", schema = "krs_schema")
public class RaBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public BigDecimal getTotalRaBillAmount() { return totalRaBillAmount; }
    public void setTotalRaBillAmount(BigDecimal totalRaBillAmount) { this.totalRaBillAmount = totalRaBillAmount; }

    public Boolean getInvoiceSubmitted() { return invoiceSubmitted; }
    public void setInvoiceSubmitted(Boolean invoiceSubmitted) { this.invoiceSubmitted = invoiceSubmitted; }

    public String getBillCheckPersonName() { return billCheckPersonName; }
    public void setBillCheckPersonName(String billCheckPersonName) { this.billCheckPersonName = billCheckPersonName; }

    public String getDeptRaBillCopyFile() { return deptRaBillCopyFile; }
    public void setDeptRaBillCopyFile(String deptRaBillCopyFile) { this.deptRaBillCopyFile = deptRaBillCopyFile; }

    public Boolean getBillDeposited() { return billDeposited; }
    public void setBillDeposited(Boolean billDeposited) { this.billDeposited = billDeposited; }

    public String getRaBillNumber() { return raBillNumber; }
    public void setRaBillNumber(String raBillNumber) { this.raBillNumber = raBillNumber; }

    public String getNameOfAgency() { return nameOfAgency; }
    public void setNameOfAgency(String nameOfAgency) { this.nameOfAgency = nameOfAgency; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public BigDecimal getLastBillPaidAmount() { return lastBillPaidAmount; }
    public void setLastBillPaidAmount(BigDecimal lastBillPaidAmount) { this.lastBillPaidAmount = lastBillPaidAmount; }

    public LocalDate getDateOfLastPayment() { return dateOfLastPayment; }
    public void setDateOfLastPayment(LocalDate dateOfLastPayment) { this.dateOfLastPayment = dateOfLastPayment; }

    public BigDecimal getAmountPaidUptoPreviousBills() { return amountPaidUptoPreviousBills; }
    public void setAmountPaidUptoPreviousBills(BigDecimal amountPaidUptoPreviousBills) { this.amountPaidUptoPreviousBills = amountPaidUptoPreviousBills; }

    public BigDecimal getTotalAmountOfCurrentRaBill() { return totalAmountOfCurrentRaBill; }
    public void setTotalAmountOfCurrentRaBill(BigDecimal totalAmountOfCurrentRaBill) { this.totalAmountOfCurrentRaBill = totalAmountOfCurrentRaBill; }

    public BigDecimal getTotalAmountUptoThisBill() { return totalAmountUptoThisBill; }
    public void setTotalAmountUptoThisBill(BigDecimal totalAmountUptoThisBill) { this.totalAmountUptoThisBill = totalAmountUptoThisBill; }

    public LocalDate getDateOfBill() { return dateOfBill; }
    public void setDateOfBill(LocalDate dateOfBill) { this.dateOfBill = dateOfBill; }

    public BigDecimal getGrossBillAmount() { return grossBillAmount; }
    public void setGrossBillAmount(BigDecimal grossBillAmount) { this.grossBillAmount = grossBillAmount; }

    public BigDecimal getCgst9Percent() { return cgst9Percent; }
    public void setCgst9Percent(BigDecimal cgst9Percent) { this.cgst9Percent = cgst9Percent; }

    public BigDecimal getSgst9Percent() { return sgst9Percent; }
    public void setSgst9Percent(BigDecimal sgst9Percent) { this.sgst9Percent = sgst9Percent; }

    public BigDecimal getTotalGst() { return totalGst; }
    public void setTotalGst(BigDecimal totalGst) { this.totalGst = totalGst; }

    public BigDecimal getNetBillAmount() { return netBillAmount; }
    public void setNetBillAmount(BigDecimal netBillAmount) { this.netBillAmount = netBillAmount; }

    public BigDecimal getRm5Percent() { return rm5Percent; }
    public void setRm5Percent(BigDecimal rm5Percent) { this.rm5Percent = rm5Percent; }

    public BigDecimal getTds2Percent() { return tds2Percent; }
    public void setTds2Percent(BigDecimal tds2Percent) { this.tds2Percent = tds2Percent; }

    public BigDecimal getLabourCess1Percent() { return labourCess1Percent; }
    public void setLabourCess1Percent(BigDecimal labourCess1Percent) { this.labourCess1Percent = labourCess1Percent; }

    public BigDecimal getCgst1Percent() { return cgst1Percent; }
    public void setCgst1Percent(BigDecimal cgst1Percent) { this.cgst1Percent = cgst1Percent; }

    public BigDecimal getSgst1Percent() { return sgst1Percent; }
    public void setSgst1Percent(BigDecimal sgst1Percent) { this.sgst1Percent = sgst1Percent; }

    public BigDecimal getWithheldAmount() { return withheldAmount; }
    public void setWithheldAmount(BigDecimal withheldAmount) { this.withheldAmount = withheldAmount; }

    public BigDecimal getLiquidityDamage() { return liquidityDamage; }
    public void setLiquidityDamage(BigDecimal liquidityDamage) { this.liquidityDamage = liquidityDamage; }

    public BigDecimal getNetPayment() { return netPayment; }
    public void setNetPayment(BigDecimal netPayment) { this.netPayment = netPayment; }

    public String getPreAuditRemarks() { return preAuditRemarks; }
    public void setPreAuditRemarks(String preAuditRemarks) { this.preAuditRemarks = preAuditRemarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
