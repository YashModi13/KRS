package com.krs.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krs.backend.enums.ProjectExcelColumn;
import com.krs.backend.models.BulkUploadProjectsData;
import com.krs.backend.models.DepartmentMaster;
import com.krs.backend.models.Project;
import com.krs.backend.models.ProjectUploadHistory;
import com.krs.backend.models.RefPersonMaster;
import com.krs.backend.models.RelatedToMaster;
import com.krs.backend.repositories.BulkUploadProjectsDataRepository;
import com.krs.backend.repositories.ProjectRepository;
import com.krs.backend.repositories.ProjectUploadHistoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProjectExcelService {

    private static final int BATCH_SIZE = 100;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final List<String> HEADERS = Arrays.stream(ProjectExcelColumn.values())
            .map(ProjectExcelColumn::getHeaderName)
            .collect(Collectors.toList());

    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy")
    );

    private final ProjectRepository projectRepository;
    private final ProjectUploadHistoryRepository uploadHistoryRepository;
    private final BulkUploadProjectsDataRepository bulkUploadDataRepository;
    private final RelatedToMasterService relatedToMasterService;
    private final DepartmentMasterService departmentMasterService;
    private final RefPersonMasterService refPersonMasterService;

    @PersistenceContext
    private EntityManager entityManager;

    public ProjectExcelService(ProjectRepository projectRepository,
                               ProjectUploadHistoryRepository uploadHistoryRepository,
                               BulkUploadProjectsDataRepository bulkUploadDataRepository,
                               RelatedToMasterService relatedToMasterService,
                               DepartmentMasterService departmentMasterService,
                               RefPersonMasterService refPersonMasterService) {
        this.projectRepository = projectRepository;
        this.uploadHistoryRepository = uploadHistoryRepository;
        this.bulkUploadDataRepository = bulkUploadDataRepository;
        this.relatedToMasterService = relatedToMasterService;
        this.departmentMasterService = departmentMasterService;
        this.refPersonMasterService = refPersonMasterService;
    }

    /**
     * Generate standard Excel Template for Projects
     */
    public void generateTemplate(OutputStream outputStream) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tender Details Template");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS.get(i));
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < HEADERS.size(); i++) {
                sheet.setColumnWidth(i, 20 * 256);
            }

            workbook.write(outputStream);
        }
    }

    /**
     * Download Excel Report for All Projects in Database
     */
    @Transactional(readOnly = true)
    public void exportProjectsToExcel(OutputStream outputStream) throws Exception {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Projects Export");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS.get(i));
                cell.setCellStyle(headerStyle);
            }

            List<Project> projects = projectRepository.findAll();
            int rowIndex = 1;

            for (Project p : projects) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(formatDate(p.getDateOfSub()));
                row.createCell(1).setCellValue(resolveDepartmentDisplay(p.getDepartmentName()));
                row.createCell(2).setCellValue(p.getTenderId() != null ? p.getTenderId() : "");
                row.createCell(3).setCellValue(p.getNoticeNo() != null ? p.getNoticeNo() : (p.getPackageNo() != null ? p.getPackageNo() : ""));
                row.createCell(4).setCellValue(p.getNameOfWork() != null ? p.getNameOfWork() : "");
                row.createCell(5).setCellValue(resolveRelatedToDisplay(p.getRelatedTo()));
                row.createCell(6).setCellValue(p.getTenderFee() != null ? p.getTenderFee().doubleValue() : 0);
                row.createCell(7).setCellValue(p.getTenderFeeNo() != null ? p.getTenderFeeNo() : "");
                row.createCell(8).setCellValue(p.getEmdAmt() != null ? p.getEmdAmt().doubleValue() : 0);
                row.createCell(9).setCellValue(p.getEmdNo() != null ? p.getEmdNo() : "");
                row.createCell(10).setCellValue(p.getEstimatedTenderCost() != null ? p.getEstimatedTenderCost().doubleValue() : 0);
                row.createCell(11).setCellValue(p.getTenderedCost() != null ? p.getTenderedCost().doubleValue() : 0);
                row.createCell(12).setCellValue(p.getAboveBelowPercentage() != null ? p.getAboveBelowPercentage().toString() : "");
                row.createCell(13).setCellValue(resolveRefPersonDisplay(p.getRefPerson()));
                row.createCell(14).setCellValue(p.getWorkAwardedStatus() != null ? p.getWorkAwardedStatus() : "");
                row.createCell(15).setCellValue(p.getWorkOrderNumber() != null ? p.getWorkOrderNumber() : "");
                row.createCell(16).setCellValue(formatDate(p.getWorkOrderDate()));
                row.createCell(17).setCellValue(p.getTimeLimit() != null ? p.getTimeLimit() : "");
                row.createCell(18).setCellValue(p.getSecurityDepositAmount() != null ? p.getSecurityDepositAmount().doubleValue() : 0);
                row.createCell(19).setCellValue(p.getSdFdrNo() != null ? p.getSdFdrNo() : "");
                row.createCell(20).setCellValue(p.getRemarks() != null ? p.getRemarks() : "");
                row.createCell(21).setCellValue(p.getSdRabDeduction() != null ? p.getSdRabDeduction().doubleValue() : 0);
                row.createCell(22).setCellValue(p.getSdRabReturnAmount() != null ? p.getSdRabReturnAmount().doubleValue() : 0);
                row.createCell(23).setCellValue(p.getAdditionalDeduction() != null ? p.getAdditionalDeduction() : "");
                row.createCell(24).setCellValue(p.getWorkCompletedAmount() != null ? p.getWorkCompletedAmount().doubleValue() : 0);
                row.createCell(25).setCellValue(p.getPendingWorkAmount() != null ? p.getPendingWorkAmount().doubleValue() : 0);
                row.createCell(26).setCellValue(formatDate(p.getCompletionDateActual()));
                row.createCell(27).setCellValue(p.getDefectsLiabilityPeriod() != null ? p.getDefectsLiabilityPeriod() : "");
                row.createCell(28).setCellValue(formatDate(p.getDlpEndedOn()));
                row.createCell(29).setCellValue(p.getEmdReturnStatus() != null ? p.getEmdReturnStatus() : "");
                row.createCell(30).setCellValue(p.getSdReturnStatus() != null ? p.getSdReturnStatus() : "");
                row.createCell(31).setCellValue(p.getSdRmRabReturnStatus() != null ? p.getSdRmRabReturnStatus() : "");
                row.createCell(32).setCellValue(p.getStatus() != null ? p.getStatus() : "");

                entityManager.detach(p);
            }

            workbook.write(outputStream);
            workbook.dispose();
        }
    }

    /**
     * Batch & Stream Bulk Excel Upload storing row records in bulk_upload_projects_data and projects tables.
     */
    @Transactional
    public ProjectUploadHistory uploadProjectsExcel(MultipartFile file, String uploadedBy) throws Exception {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.toLowerCase().endsWith(".xlsx") && !originalFilename.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("Invalid file format. Please upload a valid Excel file (.xlsx or .xls).");
        }

        ProjectUploadHistory history = ProjectUploadHistory.builder()
                .filename(originalFilename)
                .uploadedBy(uploadedBy != null ? uploadedBy : "system")
                .uploadTime(LocalDateTime.now())
                .totalRows(0)
                .successCount(0)
                .failedCount(0)
                .status("PROCESSING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        history = uploadHistoryRepository.saveAndFlush(history);
        Long masterId = history.getId();

        Set<String> existingTenderIds = projectRepository.findAllExistingTenderIds();
        Set<String> fileTenderIds = new HashSet<>();

        List<String> errorLogs = new ArrayList<>();
        List<Project> projectSaveBatch = new ArrayList<>();
        List<BulkUploadProjectsData> detailSaveBatch = new ArrayList<>();

        int totalRows = 0;
        int successCount = 0;
        int failedCount = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                throw new IllegalArgumentException("Excel file is empty or missing data rows.");
            }

            Row headerRow = sheet.getRow(0);
            Map<ProjectExcelColumn, Integer> colMap = buildColumnMap(headerRow);
            int lastRowNum = sheet.getLastRowNum();

            for (int r = 1; r <= lastRowNum; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                totalRows++;
                int displayRow = r + 1;

                LocalDate dateOfSub = getCellDate(row, colMap, ProjectExcelColumn.TENDER_SUB_DATE);
                String rawDepartment = getCellString(row, colMap, ProjectExcelColumn.DEPARTMENT_NAME);
                DepartmentMaster masterDept = departmentMasterService.getOrCreateDepartment(rawDepartment);
                String departmentName = masterDept != null ? masterDept.getId().toString() : (rawDepartment != null ? DepartmentMasterService.toTitleCase(rawDepartment) : null);

                String tenderId = getCellString(row, colMap, ProjectExcelColumn.TENDER_ID);
                String noticeNo = getCellString(row, colMap, ProjectExcelColumn.NOTICE_NO);
                String packageNo = noticeNo;
                String nameOfWork = getCellString(row, colMap, ProjectExcelColumn.NAME_OF_WORK);
                String rawRelatedTo = getCellString(row, colMap, ProjectExcelColumn.RELATED_TO);
                RelatedToMaster masterRelatedTo = relatedToMasterService.getOrCreateRelatedTo(rawRelatedTo);
                String relatedTo = masterRelatedTo != null ? masterRelatedTo.getId().toString() : (rawRelatedTo != null ? RelatedToMasterService.toTitleCase(rawRelatedTo) : null);

                BigDecimal tenderFee = getCellBigDecimal(row, colMap, ProjectExcelColumn.TENDER_FEE);
                String tenderFeeNo = getCellString(row, colMap, ProjectExcelColumn.TENDER_FEE_NO);
                BigDecimal emdAmt = getCellBigDecimal(row, colMap, ProjectExcelColumn.EMD);
                String emdNo = getCellString(row, colMap, ProjectExcelColumn.EMD_NO);
                BigDecimal estimatedTenderCost = getCellBigDecimal(row, colMap, ProjectExcelColumn.ESTIMATED_COST);
                BigDecimal tenderedCost = getCellBigDecimal(row, colMap, ProjectExcelColumn.TENDERED_COST);
                BigDecimal aboveBelowPercentage = getCellBigDecimal(row, colMap, ProjectExcelColumn.ABOVE_BELOW);

                String rawRefPerson = getCellString(row, colMap, ProjectExcelColumn.REF);
                RefPersonMaster masterRef = refPersonMasterService.getOrCreateRefPerson(rawRefPerson);
                String refPerson = masterRef != null ? masterRef.getId().toString() : (rawRefPerson != null ? RefPersonMasterService.toTitleCase(rawRefPerson) : null);

                String workAwardedStatus = getCellString(row, colMap, ProjectExcelColumn.WORK_AWARDED_STATUS);
                String workOrderNumber = getCellString(row, colMap, ProjectExcelColumn.WORK_ORDER_NO);
                LocalDate workOrderDate = getCellDate(row, colMap, ProjectExcelColumn.WORK_ORDER_DATE);
                String timeLimit = getCellString(row, colMap, ProjectExcelColumn.TIME_LIMIT);
                BigDecimal securityDepositAmount = getCellBigDecimal(row, colMap, ProjectExcelColumn.SECURITY_DEPOSIT);
                String sdFdrNo = getCellString(row, colMap, ProjectExcelColumn.SD_FDR_NO);
                String remarks = getCellString(row, colMap, ProjectExcelColumn.REMARKS);
                BigDecimal sdRabDeduction = getCellBigDecimal(row, colMap, ProjectExcelColumn.SD_RAB_DEDUCTION);
                BigDecimal sdRabReturnAmount = getCellBigDecimal(row, colMap, ProjectExcelColumn.SD_RAB_RETURN_AMOUNT);
                String additionalDeduction = getCellString(row, colMap, ProjectExcelColumn.ANY_ADDITIONAL_DEDUCTION);
                BigDecimal workCompletedAmount = getCellBigDecimal(row, colMap, ProjectExcelColumn.WORK_COMPLETED_AMOUNT);
                BigDecimal pendingWorkAmount = getCellBigDecimal(row, colMap, ProjectExcelColumn.PENDING_WORK_AMOUNT);
                LocalDate completionDateActual = getCellDate(row, colMap, ProjectExcelColumn.COMPLETION_DATE_ACTUAL);
                String defectsLiabilityPeriod = getCellString(row, colMap, ProjectExcelColumn.DEFECTS_LIABILITY_PERIOD);
                LocalDate dlpEndedOn = getCellDate(row, colMap, ProjectExcelColumn.DLP_ENDED_ON);
                String emdReturnStatus = getCellString(row, colMap, ProjectExcelColumn.EMD_RETURN_STATUS);
                String sdReturnStatus = getCellString(row, colMap, ProjectExcelColumn.SD_RETURN_STATUS);
                String sdRmRabReturnStatus = getCellString(row, colMap, ProjectExcelColumn.SD_RM_RAB_RETURN_STATUS);
                String status = getCellString(row, colMap, ProjectExcelColumn.STATUS);

                // Duplication and Validation Checks
                boolean isDuplicate = false;
                StringBuilder rowErr = new StringBuilder();

                if (tenderId == null || tenderId.trim().isEmpty()) {
                    isDuplicate = true;
                    rowErr.append(String.format("Row %d: Tender ID is mandatory and cannot be null or empty. ", displayRow));
                } else {
                    String cleanTenderId = tenderId.trim();
                    if (existingTenderIds.contains(cleanTenderId)) {
                        isDuplicate = true;
                        rowErr.append(String.format("Row %d: Tender ID '%s' already exists in system. ", displayRow, cleanTenderId));
                    } else if (fileTenderIds.contains(cleanTenderId)) {
                        isDuplicate = true;
                        rowErr.append(String.format("Row %d: Duplicate Tender ID '%s' found in same file. ", displayRow, cleanTenderId));
                    }
                }

                if (departmentName == null || departmentName.trim().isEmpty()) {
                    isDuplicate = true;
                    rowErr.append(String.format("Row %d: Department Name is mandatory and cannot be null or empty. ", displayRow));
                }

                BulkUploadProjectsData detailRow = BulkUploadProjectsData.builder()
                        .masterId(masterId)
                        .dateOfSub(dateOfSub)
                        .departmentName(departmentName)
                        .tenderId(tenderId)
                        .noticeNo(noticeNo)
                        .packageNo(packageNo != null ? packageNo : noticeNo)
                        .nameOfWork(nameOfWork)
                        .relatedTo(relatedTo)
                        .tenderFee(tenderFee)
                        .tenderFeeNo(tenderFeeNo)
                        .emdAmt(emdAmt)
                        .emdNo(emdNo)
                        .estimatedTenderCost(estimatedTenderCost)
                        .tenderedCost(tenderedCost)
                        .aboveBelowPercentage(aboveBelowPercentage)
                        .refPerson(refPerson)
                        .workAwardedStatus(workAwardedStatus)
                        .workOrderNumber(workOrderNumber)
                        .workOrderDate(workOrderDate)
                        .timeLimit(timeLimit)
                        .securityDepositAmount(securityDepositAmount)
                        .sdFdrNo(sdFdrNo)
                        .remarks(remarks)
                        .sdRabDeduction(sdRabDeduction)
                        .sdRabReturnAmount(sdRabReturnAmount)
                        .additionalDeduction(additionalDeduction)
                        .workCompletedAmount(workCompletedAmount)
                        .pendingWorkAmount(pendingWorkAmount)
                        .completionDateActual(completionDateActual)
                        .defectsLiabilityPeriod(defectsLiabilityPeriod)
                        .dlpEndedOn(dlpEndedOn)
                        .emdReturnStatus(emdReturnStatus)
                        .sdReturnStatus(sdReturnStatus)
                        .sdRmRabReturnStatus(sdRmRabReturnStatus)
                        .status(status)
                        .createdAt(LocalDateTime.now())
                        .build();

                if (isDuplicate) {
                    failedCount++;
                    String errMessage = rowErr.toString().trim();
                    errorLogs.add(errMessage);

                    detailRow.setIsFailed(true);
                    detailRow.setFailedReason(errMessage);
                    detailSaveBatch.add(detailRow);
                } else {
                    try {
                        Project p = Project.builder()
                                .dateOfSub(dateOfSub)
                                .departmentName(departmentName)
                                .tenderId(tenderId)
                                .noticeNo(noticeNo)
                                .packageNo(packageNo != null ? packageNo : noticeNo)
                                .nameOfWork(nameOfWork)
                                .relatedTo(relatedTo)
                                .tenderFee(tenderFee)
                                .tenderFeeNo(tenderFeeNo)
                                .emdAmt(emdAmt)
                                .emdNo(emdNo)
                                .estimatedTenderCost(estimatedTenderCost)
                                .tenderedCost(tenderedCost)
                                .aboveBelowPercentage(aboveBelowPercentage)
                                .refPerson(refPerson)
                                .workAwardedStatus(workAwardedStatus)
                                .workOrderNumber(workOrderNumber)
                                .workOrderDate(workOrderDate)
                                .timeLimit(timeLimit)
                                .securityDepositAmount(securityDepositAmount)
                                .sdFdrNo(sdFdrNo)
                                .remarks(remarks)
                                .sdRabDeduction(sdRabDeduction)
                                .sdRabReturnAmount(sdRabReturnAmount)
                                .additionalDeduction(additionalDeduction)
                                .workCompletedAmount(workCompletedAmount)
                                .pendingWorkAmount(pendingWorkAmount)
                                .completionDateActual(completionDateActual)
                                .defectsLiabilityPeriod(defectsLiabilityPeriod)
                                .dlpEndedOn(dlpEndedOn)
                                .emdReturnStatus(emdReturnStatus)
                                .sdReturnStatus(sdReturnStatus)
                                .sdRmRabReturnStatus(sdRmRabReturnStatus)
                                .status(status)
                                .build();

                        // Create project_locations split by semicolon (;)
                        List<com.krs.backend.models.ProjectLocation> locList = new ArrayList<>();
                        String rawLocSource = (noticeNo != null && noticeNo.contains(";")) ? noticeNo : nameOfWork;
                        if (rawLocSource != null && rawLocSource.contains(";")) {
                            String[] parts = rawLocSource.split(";");
                            for (String part : parts) {
                                String cleanLoc = part.trim();
                                if (!cleanLoc.isEmpty()) {
                                    locList.add(com.krs.backend.models.ProjectLocation.builder()
                                            .project(p)
                                            .villageName(cleanLoc)
                                            .tenderId(tenderId)
                                            .startDate(workOrderDate != null ? workOrderDate : dateOfSub)
                                            .closedDate(completionDateActual)
                                            .status(status != null ? status : "Running")
                                            .createdAt(LocalDateTime.now())
                                            .updatedAt(LocalDateTime.now())
                                            .build());
                                }
                            }
                        } else {
                            String cleanLoc = rawLocSource != null ? rawLocSource.trim() : null;
                            locList.add(com.krs.backend.models.ProjectLocation.builder()
                                    .project(p)
                                    .villageName(cleanLoc)
                                    .tenderId(tenderId)
                                    .startDate(workOrderDate != null ? workOrderDate : dateOfSub)
                                    .closedDate(completionDateActual)
                                    .status(status != null ? status : "Running")
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build());
                        }
                        p.setLocations(locList);

                        projectSaveBatch.add(p);

                        detailRow.setIsFailed(false);
                        detailRow.setFailedReason(null);
                        detailSaveBatch.add(detailRow);

                        successCount++;

                        if (tenderId != null && !tenderId.trim().isEmpty()) {
                            fileTenderIds.add(tenderId.trim());
                        }

                    } catch (Exception ex) {
                        failedCount++;
                        String errMessage = String.format("Row %d: Parse error - %s", displayRow, ex.getMessage());
                        errorLogs.add(errMessage);

                        detailRow.setIsFailed(true);
                        detailRow.setFailedReason(errMessage);
                        detailSaveBatch.add(detailRow);
                    }
                }

                if (projectSaveBatch.size() >= BATCH_SIZE) {
                    projectRepository.saveAll(projectSaveBatch);
                    projectRepository.flush();
                    projectSaveBatch.clear();
                }
                if (detailSaveBatch.size() >= BATCH_SIZE) {
                    bulkUploadDataRepository.saveAll(detailSaveBatch);
                    bulkUploadDataRepository.flush();
                    detailSaveBatch.clear();
                }
            }

            if (!projectSaveBatch.isEmpty()) {
                projectRepository.saveAll(projectSaveBatch);
                projectRepository.flush();
                projectSaveBatch.clear();
            }
            if (!detailSaveBatch.isEmpty()) {
                bulkUploadDataRepository.saveAll(detailSaveBatch);
                bulkUploadDataRepository.flush();
                detailSaveBatch.clear();
            }

            String finalStatus;
            if (failedCount == 0 && successCount > 0) {
                finalStatus = "SUCCESS";
            } else if (successCount > 0 && failedCount > 0) {
                finalStatus = "PARTIAL_SUCCESS";
            } else {
                finalStatus = "FAILED";
            }

            history.setTotalRows(totalRows);
            history.setSuccessCount(successCount);
            history.setFailedCount(failedCount);
            history.setStatus(finalStatus);
            history.setErrorDetails(OBJECT_MAPPER.writeValueAsString(errorLogs));
            history.setUpdatedAt(LocalDateTime.now());

            return uploadHistoryRepository.saveAndFlush(history);

        } catch (Exception e) {
            history.setStatus("FAILED");
            history.setErrorDetails("[\"Failed to process excel file: " + (e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Unknown error") + "\"]");
            history.setUpdatedAt(LocalDateTime.now());
            uploadHistoryRepository.saveAndFlush(history);
            throw e;
        }
    }

    /**
     * Download Excel Report for a specific Bulk Upload execution (ALL, SUCCESS, or FAILED rows)
     */
    @Transactional(readOnly = true)
    public void exportUploadHistoryDataToExcel(Long masterId, String filterType, OutputStream outputStream) throws Exception {
        ProjectUploadHistory history = uploadHistoryRepository.findById(masterId)
                .orElseThrow(() -> new IllegalArgumentException("Upload history record not found with ID: " + masterId));

        List<BulkUploadProjectsData> list;
        if ("SUCCESS".equalsIgnoreCase(filterType)) {
            list = bulkUploadDataRepository.findByMasterIdAndIsFailedOrderByIdAsc(masterId, false);
        } else if ("FAILED".equalsIgnoreCase(filterType)) {
            list = bulkUploadDataRepository.findByMasterIdAndIsFailedOrderByIdAsc(masterId, true);
        } else {
            list = bulkUploadDataRepository.findByMasterIdOrderByIdAsc(masterId);
        }

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Upload Report - " + filterType.toUpperCase());

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            List<String> reportHeaders = new ArrayList<>(HEADERS);
            reportHeaders.add("UPLOAD STATUS");
            reportHeaders.add("FAILURE REASON");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < reportHeaders.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(reportHeaders.get(i));
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (BulkUploadProjectsData p : list) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(formatDate(p.getDateOfSub()));
                row.createCell(1).setCellValue(resolveDepartmentDisplay(p.getDepartmentName()));
                row.createCell(2).setCellValue(p.getTenderId() != null ? p.getTenderId() : "");
                row.createCell(3).setCellValue(p.getNoticeNo() != null ? p.getNoticeNo() : (p.getPackageNo() != null ? p.getPackageNo() : ""));
                row.createCell(4).setCellValue(p.getNameOfWork() != null ? p.getNameOfWork() : "");
                row.createCell(5).setCellValue(resolveRelatedToDisplay(p.getRelatedTo()));
                row.createCell(6).setCellValue(p.getTenderFee() != null ? p.getTenderFee().doubleValue() : 0);
                row.createCell(7).setCellValue(p.getTenderFeeNo() != null ? p.getTenderFeeNo() : "");
                row.createCell(8).setCellValue(p.getEmdAmt() != null ? p.getEmdAmt().doubleValue() : 0);
                row.createCell(9).setCellValue(p.getEmdNo() != null ? p.getEmdNo() : "");
                row.createCell(10).setCellValue(p.getEstimatedTenderCost() != null ? p.getEstimatedTenderCost().doubleValue() : 0);
                row.createCell(11).setCellValue(p.getTenderedCost() != null ? p.getTenderedCost().doubleValue() : 0);
                row.createCell(12).setCellValue(p.getAboveBelowPercentage() != null ? p.getAboveBelowPercentage().toString() : "");
                row.createCell(13).setCellValue(resolveRefPersonDisplay(p.getRefPerson()));
                row.createCell(14).setCellValue(p.getWorkAwardedStatus() != null ? p.getWorkAwardedStatus() : "");
                row.createCell(15).setCellValue(p.getWorkOrderNumber() != null ? p.getWorkOrderNumber() : "");
                row.createCell(16).setCellValue(formatDate(p.getWorkOrderDate()));
                row.createCell(17).setCellValue(p.getTimeLimit() != null ? p.getTimeLimit() : "");
                row.createCell(18).setCellValue(p.getSecurityDepositAmount() != null ? p.getSecurityDepositAmount().doubleValue() : 0);
                row.createCell(19).setCellValue(p.getSdFdrNo() != null ? p.getSdFdrNo() : "");
                row.createCell(20).setCellValue(p.getRemarks() != null ? p.getRemarks() : "");
                row.createCell(21).setCellValue(p.getSdRabDeduction() != null ? p.getSdRabDeduction().doubleValue() : 0);
                row.createCell(22).setCellValue(p.getSdRabReturnAmount() != null ? p.getSdRabReturnAmount().doubleValue() : 0);
                row.createCell(23).setCellValue(p.getAdditionalDeduction() != null ? p.getAdditionalDeduction() : "");
                row.createCell(24).setCellValue(p.getWorkCompletedAmount() != null ? p.getWorkCompletedAmount().doubleValue() : 0);
                row.createCell(25).setCellValue(p.getPendingWorkAmount() != null ? p.getPendingWorkAmount().doubleValue() : 0);
                row.createCell(26).setCellValue(formatDate(p.getCompletionDateActual()));
                row.createCell(27).setCellValue(p.getDefectsLiabilityPeriod() != null ? p.getDefectsLiabilityPeriod() : "");
                row.createCell(28).setCellValue(formatDate(p.getDlpEndedOn()));
                row.createCell(29).setCellValue(p.getEmdReturnStatus() != null ? p.getEmdReturnStatus() : "");
                row.createCell(30).setCellValue(p.getSdReturnStatus() != null ? p.getSdReturnStatus() : "");
                row.createCell(31).setCellValue(p.getSdRmRabReturnStatus() != null ? p.getSdRmRabReturnStatus() : "");
                row.createCell(32).setCellValue(p.getStatus() != null ? p.getStatus() : "");

                row.createCell(33).setCellValue(Boolean.TRUE.equals(p.getIsFailed()) ? "FAILED" : "SUCCESS");
                row.createCell(34).setCellValue(p.getFailedReason() != null ? p.getFailedReason() : "");

                entityManager.detach(p);
            }

            workbook.write(outputStream);
            workbook.dispose();
        }
    }

    public List<ProjectUploadHistory> getUploadHistory() {
        return uploadHistoryRepository.findAllByOrderByUploadTimeDesc();
    }

    // --- Private Helper Methods ---

    private String resolveDepartmentDisplay(String val) {
        if (val == null || val.trim().isEmpty()) return "";
        String trimmed = val.trim();
        try {
            Long id = Long.parseLong(trimmed);
            return departmentMasterService.findById(id)
                    .map(DepartmentMaster::getName)
                    .orElse(trimmed);
        } catch (NumberFormatException e) {
            return DepartmentMasterService.toTitleCase(trimmed);
        }
    }

    private String resolveRefPersonDisplay(String val) {
        if (val == null || val.trim().isEmpty()) return "";
        String trimmed = val.trim();
        try {
            Long id = Long.parseLong(trimmed);
            return refPersonMasterService.findById(id)
                    .map(RefPersonMaster::getName)
                    .orElse(trimmed);
        } catch (NumberFormatException e) {
            return RefPersonMasterService.toTitleCase(trimmed);
        }
    }

    private String resolveRelatedToDisplay(String relatedToVal) {
        if (relatedToVal == null || relatedToVal.trim().isEmpty()) {
            return "";
        }
        String trimmed = relatedToVal.trim();
        try {
            Long id = Long.parseLong(trimmed);
            return relatedToMasterService.findById(id)
                    .map(RelatedToMaster::getName)
                    .orElse(trimmed);
        } catch (NumberFormatException e) {
            return RelatedToMasterService.toTitleCase(trimmed);
        }
    }

    private Map<ProjectExcelColumn, Integer> buildColumnMap(Row headerRow) {
        Map<ProjectExcelColumn, Integer> colMap = new EnumMap<>(ProjectExcelColumn.class);
        if (headerRow == null) return colMap;

        for (Cell cell : headerRow) {
            String val = getRawCellString(cell);
            if (val != null && !val.trim().isEmpty()) {
                for (ProjectExcelColumn col : ProjectExcelColumn.values()) {
                    if (col.matchesHeader(val)) {
                        colMap.put(col, cell.getColumnIndex());
                        break;
                    }
                }
            }
        }

        for (ProjectExcelColumn col : ProjectExcelColumn.values()) {
            if (!colMap.containsKey(col)) {
                colMap.put(col, col.getDefaultIndex());
            }
        }
        return colMap;
    }

    private Cell getCell(Row row, Map<ProjectExcelColumn, Integer> colMap, ProjectExcelColumn col) {
        Integer idx = colMap.get(col);
        return row.getCell(idx != null ? idx : col.getDefaultIndex());
    }

    private String getCellString(Row row, Map<ProjectExcelColumn, Integer> colMap, ProjectExcelColumn col) {
        Cell cell = getCell(row, colMap, col);
        return getRawCellString(cell);
    }

    private BigDecimal getCellBigDecimal(Row row, Map<ProjectExcelColumn, Integer> colMap, ProjectExcelColumn col) {
        Cell cell = getCell(row, colMap, col);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.ERROR) return null;
        if (cell.getCellType() == CellType.ERROR) return null;

        if (cell.getCellType() == CellType.NUMERIC || 
           (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC)) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        String str = getRawCellString(cell);
        if (str == null || str.trim().isEmpty()) return null;
        try {
            return new BigDecimal(str.trim().replaceAll("[^0-9.-]", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate getCellDate(Row row, Map<ProjectExcelColumn, Integer> colMap, ProjectExcelColumn col) {
        Cell cell = getCell(row, colMap, col);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.ERROR) return null;
        if (cell.getCellType() == CellType.ERROR) return null;

        if ((cell.getCellType() == CellType.NUMERIC || (cell.getCellType() == CellType.FORMULA && cell.getCachedFormulaResultType() == CellType.NUMERIC))
                && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            if (date != null) {
                return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        }
        String str = getRawCellString(cell);
        if (str == null || str.trim().isEmpty()) return null;

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(str.trim(), formatter);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String getRawCellString(Cell cell) {
        if (cell == null) return null;
        CellType type = cell.getCellType();

        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        String rawVal = null;
        switch (type) {
            case STRING:
                rawVal = cell.getStringCellValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    return date != null ? formatDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()) : null;
                }
                double val = cell.getNumericCellValue();
                if (val == (long) val) {
                    rawVal = String.valueOf((long) val);
                } else {
                    rawVal = String.valueOf(val);
                }
                break;
            case BOOLEAN:
                rawVal = String.valueOf(cell.getBooleanCellValue());
                break;
            case ERROR:
            case BLANK:
            default:
                return null;
        }

        if (rawVal != null) {
            // Replace newline characters with spaces and sanitize extra spaces
            rawVal = rawVal.replace("\r\n", " ").replace("\n", " ").replace("\r", " ");
            rawVal = rawVal.replaceAll("\\s+", " ").trim();
            if (rawVal.isEmpty()) {
                return null;
            }
        }
        return rawVal;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && cell.getCellType() != CellType.ERROR) {
                String str = getRawCellString(cell);
                if (str != null && !str.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
    }
}
