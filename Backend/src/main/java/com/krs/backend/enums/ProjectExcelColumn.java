package com.krs.backend.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum ProjectExcelColumn {
    TENDER_SUB_DATE("Tender Sub. Date", 0, true, DataType.DATE, Arrays.asList("tender sub date", "sub date", "date of sub", "tender sub. date")),
    DEPARTMENT_NAME("Department Name", 1, true, DataType.STRING, Arrays.asList("department", "dept name", "department_name", "department name")),
    TENDER_ID("Tender ID", 2, true, DataType.STRING, Arrays.asList("tender id", "tender_id")),
    NOTICE_NO("Notice No.", 3, true, DataType.STRING, Arrays.asList("notice no", "notice_no", "notice number", "notice no.")),
    NAME_OF_WORK("Name of Work", 4, true, DataType.STRING, Arrays.asList("name of work", "work name", "name_of_work")),
    RELATED_TO("Related to", 5, true, DataType.STRING, Arrays.asList("related to", "related_to")),
    TENDER_FEE("Tender Fee", 6, true, DataType.NUMBER, Arrays.asList("tender fee", "fee")),
    TENDER_FEE_NO("Tender Fee No.", 7, true, DataType.STRING, Arrays.asList("tender fee no", "fee no", "tender fee no.")),
    EMD("EMD", 8, true, DataType.NUMBER, Arrays.asList("emd amount", "emd amt", "emd")),
    EMD_NO("Emd No.", 9, true, DataType.STRING, Arrays.asList("emd no", "emd number", "emd no.")),
    ESTIMATED_COST("EST. COST", 10, true, DataType.NUMBER, Arrays.asList("est cost", "estimated cost", "estimated tender cost", "est. cost")),
    TENDERED_COST("TED COST", 11, true, DataType.NUMBER, Arrays.asList("ted cost", "tendered cost")),
    ABOVE_BELOW("Above/Below", 12, true, DataType.NUMBER, Arrays.asList("above/below", "above below %", "above below")),
    REF("Ref", 13, true, DataType.STRING, Arrays.asList("ref person", "ref")),
    WORK_AWARDED_STATUS("Work Awarded /or Not", 14, true, DataType.STRING, Arrays.asList("work awarded", "work awarded status", "work awarded /or not")),
    WORK_ORDER_NO("Work Order No.", 15, true, DataType.STRING, Arrays.asList("work order no", "work order number", "work order no.")),
    WORK_ORDER_DATE("Work Order Date", 16, true, DataType.DATE, Arrays.asList("work order date")),
    TIME_LIMIT("Time Limit", 17, true, DataType.STRING, Arrays.asList("time limit")),
    SECURITY_DEPOSIT("SD", 18, true, DataType.NUMBER, Arrays.asList("security deposit", "sd amount", "sd")),
    SD_FDR_NO("SD FDR NO", 19, true, DataType.STRING, Arrays.asList("sd fdr no", "fdr no")),
    REMARKS("REMARKS", 20, true, DataType.STRING, Arrays.asList("remarks")),
    SD_RAB_DEDUCTION("SD RAB DEDUCTION", 21, true, DataType.NUMBER, Arrays.asList("sd rab deduction")),
    SD_RAB_RETURN_AMOUNT("SD RAB RETURN AMOUNT", 22, true, DataType.NUMBER, Arrays.asList("sd rab return amount")),
    ANY_ADDITIONAL_DEDUCTION("ANY ADDITIONAL DEDUCTION", 23, true, DataType.STRING, Arrays.asList("additional deduction", "any additional deduction")),
    WORK_COMPLETED_AMOUNT("Work Completed Amount", 24, true, DataType.NUMBER, Arrays.asList("work completed amount")),
    PENDING_WORK_AMOUNT("Pending Work Amount", 25, true, DataType.NUMBER, Arrays.asList("pending work amount")),
    COMPLETION_DATE_ACTUAL("DATE OF COMPLETION OF WORK", 26, true, DataType.DATE, Arrays.asList("completion date", "date of completion of work", "date of completion")),
    DEFECTS_LIABILITY_PERIOD("Defects Liability Period", 27, true, DataType.STRING, Arrays.asList("defects liability period", "dlp")),
    DLP_ENDED_ON("Defects Liability Period Ended on", 28, true, DataType.DATE, Arrays.asList("dlp ended on", "dlp end date", "defects liability period ended on")),
    EMD_RETURN_STATUS("EMD RETURN STATUS", 29, true, DataType.STRING, Arrays.asList("emd return status")),
    SD_RETURN_STATUS("SD RETURN STATUS", 30, true, DataType.STRING, Arrays.asList("sd return status")),
    SD_RM_RAB_RETURN_STATUS("SD RM/RAB RETURN STATUS", 31, true, DataType.STRING, Arrays.asList("sd rm rab return status", "sd rm/rab return status")),
    STATUS("STATUS", 32, true, DataType.STRING, Arrays.asList("status"));

    private final String headerName;
    private final int defaultIndex;
    private final boolean allowNull;
    private final DataType dataType;
    private final List<String> aliases;

    ProjectExcelColumn(String headerName, int defaultIndex, boolean allowNull, DataType dataType, List<String> aliases) {
        this.headerName = headerName;
        this.defaultIndex = defaultIndex;
        this.allowNull = allowNull;
        this.dataType = dataType;
        this.aliases = aliases;
    }

    public boolean matchesHeader(String rawHeader) {
        if (rawHeader == null || rawHeader.trim().isEmpty()) {
            return false;
        }
        String clean = rawHeader.replaceAll("\\s+", " ").trim().toLowerCase();
        if (clean.equals(this.headerName.toLowerCase())) {
            return true;
        }
        for (String alias : this.aliases) {
            if (clean.equals(alias.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public enum DataType {
        STRING, NUMBER, DATE
    }
}
