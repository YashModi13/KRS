package com.krs.backend.services;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProjectExcelService {

    private static final int BATCH_SIZE = 100;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_PARTIAL_SUCCESS = "PARTIAL_SUCCESS";

    private static final List<String> HEADERS = Arrays.stream(ProjectExcelColumn.values())
            .map(col -> col.getHeaderName())
            .toList();

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
    public void generateTemplate(OutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tender Details Template");
            writeHeaderRow(sheet, workbook, HEADERS);

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
    public void exportProjectsToExcel(OutputStream outputStream) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Projects Export");
            writeHeaderRow(sheet, workbook, HEADERS);

            List<Project> projects = projectRepository.findAll();
            int rowIndex = 1;

            for (Project p : projects) {
                Row row = sheet.createRow(rowIndex++);
                populateProjectRow(row, p);
                entityManager.detach(p);
            }

            workbook.write(outputStream);
            workbook.dispose();
        }
    }

    /**
     * Batch & Stream Bulk Excel Upload storing row records in bulk_upload_projects_data and projects tables.
     */
    @Transactional(rollbackFor = Exception.class)
    public ProjectUploadHistory uploadProjectsExcel(MultipartFile file, String uploadedBy) throws IOException {
        validateFileFormat(file);

        ZoneId zone = ZoneId.systemDefault();
        ProjectUploadHistory history = createInitialHistoryRecord(file.getOriginalFilename(), uploadedBy, zone);
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

            Sheet sheet = getValidSheet(workbook);
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
                BulkUploadProjectsData detailRow = parseRowToDetail(row, colMap, masterId, zone);

                StringBuilder rowErr = new StringBuilder();
                boolean isDuplicate = validateRow(displayRow, detailRow.getTenderId(), detailRow.getDepartmentName(), existingTenderIds, fileTenderIds, rowErr);

                if (isDuplicate) {
                    failedCount++;
                    handleFailedRow(detailRow, rowErr.toString().trim(), errorLogs, detailSaveBatch);
                } else if (trySaveProjectRow(detailRow, displayRow, projectSaveBatch, detailSaveBatch, fileTenderIds, errorLogs)) {
                    successCount++;
                } else {
                    failedCount++;
                }

                if (projectSaveBatch.size() >= BATCH_SIZE || detailSaveBatch.size() >= BATCH_SIZE) {
                    flushBatches(projectSaveBatch, detailSaveBatch);
                }
            }

            flushBatches(projectSaveBatch, detailSaveBatch);

            String finalStatus = determineFinalStatus(successCount, failedCount);
            return finalizeHistoryRecord(history, totalRows, successCount, failedCount, finalStatus, errorLogs, zone);

        } catch (Exception e) {
            return handleUploadError(history, e, zone);
        }
    }

    private void validateFileFormat(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") && !filename.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("Invalid file format. Please upload a valid Excel file (.xlsx or .xls).");
        }
    }

    private Sheet getValidSheet(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        if (sheet == null || sheet.getLastRowNum() < 1) {
            throw new IllegalArgumentException("Excel file is empty or missing data rows.");
        }
        return sheet;
    }

    private ProjectUploadHistory createInitialHistoryRecord(String filename, String uploadedBy, ZoneId zone) {
        ProjectUploadHistory history = ProjectUploadHistory.builder()
                .filename(filename)
                .uploadedBy(uploadedBy != null ? uploadedBy : "system")
                .uploadTime(LocalDateTime.now(zone))
                .totalRows(0)
                .successCount(0)
                .failedCount(0)
                .status(STATUS_PROCESSING)
                .createdAt(LocalDateTime.now(zone))
                .updatedAt(LocalDateTime.now(zone))
                .build();
        return uploadHistoryRepository.saveAndFlush(history);
    }

    private BulkUploadProjectsData parseRowToDetail(Row row, Map<ProjectExcelColumn, Integer> colMap, Long masterId, ZoneId zone) {
        LocalDate dateOfSub = getCellDate(row, colMap, ProjectExcelColumn.TENDER_SUB_DATE);
        String rawDepartment = getCellString(row, colMap, ProjectExcelColumn.DEPARTMENT_NAME);
        DepartmentMaster masterDept = departmentMasterService.getOrCreateDepartment(rawDepartment);
        String departmentName = resolveDepartmentName(masterDept, rawDepartment);

        String tenderId = getCellString(row, colMap, ProjectExcelColumn.TENDER_ID);
        String noticeNo = getCellString(row, colMap, ProjectExcelColumn.NOTICE_NO);
        String nameOfWork = getCellString(row, colMap, ProjectExcelColumn.NAME_OF_WORK);
        String rawRelatedTo = getCellString(row, colMap, ProjectExcelColumn.RELATED_TO);
        RelatedToMaster masterRelatedTo = relatedToMasterService.getOrCreateRelatedTo(rawRelatedTo);
        String relatedTo = resolveRelatedToName(masterRelatedTo, rawRelatedTo);

        BigDecimal tenderFee = getCellBigDecimal(row, colMap, ProjectExcelColumn.TENDER_FEE);
        String tenderFeeNo = getCellString(row, colMap, ProjectExcelColumn.TENDER_FEE_NO);
        BigDecimal emdAmt = getCellBigDecimal(row, colMap, ProjectExcelColumn.EMD);
        String emdNo = getCellString(row, colMap, ProjectExcelColumn.EMD_NO);
        BigDecimal estimatedTenderCost = getCellBigDecimal(row, colMap, ProjectExcelColumn.ESTIMATED_COST);
        BigDecimal tenderedCost = getCellBigDecimal(row, colMap, ProjectExcelColumn.TENDERED_COST);
        BigDecimal aboveBelowPercentage = getCellBigDecimal(row, colMap, ProjectExcelColumn.ABOVE_BELOW);

        String rawRefPerson = getCellString(row, colMap, ProjectExcelColumn.REF);
        RefPersonMaster masterRef = refPersonMasterService.getOrCreateRefPerson(rawRefPerson);
        String refPerson = resolveRefPersonName(masterRef, rawRefPerson);

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

        return BulkUploadProjectsData.builder()
                .masterId(masterId)
                .dateOfSub(dateOfSub)
                .departmentName(departmentName)
                .tenderId(tenderId)
                .noticeNo(noticeNo)
                .packageNo(noticeNo)
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
                .createdAt(LocalDateTime.now(zone))
                .build();
    }

    private String resolveDepartmentName(DepartmentMaster master, String raw) {
        String idStr = (master != null && master.getId() != null) ? master.getId().toString() : null;
        return resolveMasterIdOrTitleCase(idStr, raw, DepartmentMasterService.toTitleCase(raw));
    }

    private String resolveRelatedToName(RelatedToMaster master, String raw) {
        String idStr = (master != null && master.getId() != null) ? master.getId().toString() : null;
        return resolveMasterIdOrTitleCase(idStr, raw, RelatedToMasterService.toTitleCase(raw));
    }

    private String resolveRefPersonName(RefPersonMaster master, String raw) {
        String idStr = (master != null && master.getId() != null) ? master.getId().toString() : null;
        return resolveMasterIdOrTitleCase(idStr, raw, RefPersonMasterService.toTitleCase(raw));
    }

    private void handleFailedRow(BulkUploadProjectsData detailRow, String errMessage, List<String> errorLogs, List<BulkUploadProjectsData> detailSaveBatch) {
        errorLogs.add(errMessage);
        detailRow.setIsFailed(true);
        detailRow.setFailedReason(errMessage);
        detailSaveBatch.add(detailRow);
    }

    private void flushBatches(List<Project> projectSaveBatch, List<BulkUploadProjectsData> detailSaveBatch) {
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
    }

    private String determineFinalStatus(int successCount, int failedCount) {
        if (failedCount == 0 && successCount > 0) {
            return STATUS_SUCCESS;
        }
        if (successCount > 0) {
            return STATUS_PARTIAL_SUCCESS;
        }
        return STATUS_FAILED;
    }

    private ProjectUploadHistory finalizeHistoryRecord(ProjectUploadHistory history, int totalRows, int successCount, int failedCount, String finalStatus, List<String> errorLogs, ZoneId zone) throws JsonProcessingException {
        history.setTotalRows(totalRows);
        history.setSuccessCount(successCount);
        history.setFailedCount(failedCount);
        history.setStatus(finalStatus);
        history.setErrorDetails(OBJECT_MAPPER.writeValueAsString(errorLogs));
        history.setUpdatedAt(LocalDateTime.now(zone));
        return uploadHistoryRepository.saveAndFlush(history);
    }

    private ProjectUploadHistory handleUploadError(ProjectUploadHistory history, Exception e, ZoneId zone) throws IOException {
        history.setStatus(STATUS_FAILED);
        history.setErrorDetails("[\"Failed to process excel file: " + (e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Unknown error") + "\"]");
        history.setUpdatedAt(LocalDateTime.now(zone));
        uploadHistoryRepository.saveAndFlush(history);
        if (e instanceof IOException ioException) {
            throw ioException;
        }
        throw new IOException("Failed to process Excel file", e);
    }

    private boolean trySaveProjectRow(BulkUploadProjectsData detailRow, int displayRow, List<Project> projectSaveBatch,
                                      List<BulkUploadProjectsData> detailSaveBatch, Set<String> fileTenderIds,
                                      List<String> errorLogs) {
        try {
            Project p = buildProjectFromDetail(detailRow);
            p.setLocations(buildLocations(p));
            projectSaveBatch.add(p);

            detailRow.setIsFailed(false);
            detailRow.setFailedReason(null);
            detailSaveBatch.add(detailRow);

            if (detailRow.getTenderId() != null && !detailRow.getTenderId().trim().isEmpty()) {
                fileTenderIds.add(detailRow.getTenderId().trim());
            }
            return true;
        } catch (Exception ex) {
            String errMessage = String.format("Row %d: Parse error - %s", displayRow, ex.getMessage());
            errorLogs.add(errMessage);

            detailRow.setIsFailed(true);
            detailRow.setFailedReason(errMessage);
            detailSaveBatch.add(detailRow);
            return false;
        }
    }

    private Project buildProjectFromDetail(BulkUploadProjectsData d) {
        return Project.builder()
                .dateOfSub(d.getDateOfSub())
                .departmentName(d.getDepartmentName())
                .tenderId(d.getTenderId())
                .noticeNo(d.getNoticeNo())
                .packageNo(d.getPackageNo())
                .nameOfWork(d.getNameOfWork())
                .relatedTo(d.getRelatedTo())
                .tenderFee(d.getTenderFee())
                .tenderFeeNo(d.getTenderFeeNo())
                .emdAmt(d.getEmdAmt())
                .emdNo(d.getEmdNo())
                .estimatedTenderCost(d.getEstimatedTenderCost())
                .tenderedCost(d.getTenderedCost())
                .aboveBelowPercentage(d.getAboveBelowPercentage())
                .refPerson(d.getRefPerson())
                .workAwardedStatus(d.getWorkAwardedStatus())
                .workOrderNumber(d.getWorkOrderNumber())
                .workOrderDate(d.getWorkOrderDate())
                .timeLimit(d.getTimeLimit())
                .securityDepositAmount(d.getSecurityDepositAmount())
                .sdFdrNo(d.getSdFdrNo())
                .remarks(d.getRemarks())
                .sdRabDeduction(d.getSdRabDeduction())
                .sdRabReturnAmount(d.getSdRabReturnAmount())
                .additionalDeduction(d.getAdditionalDeduction())
                .workCompletedAmount(d.getWorkCompletedAmount())
                .pendingWorkAmount(d.getPendingWorkAmount())
                .completionDateActual(d.getCompletionDateActual())
                .defectsLiabilityPeriod(d.getDefectsLiabilityPeriod())
                .dlpEndedOn(d.getDlpEndedOn())
                .emdReturnStatus(d.getEmdReturnStatus())
                .sdReturnStatus(d.getSdReturnStatus())
                .sdRmRabReturnStatus(d.getSdRmRabReturnStatus())
                .status(d.getStatus())
                .build();
    }

    private boolean validateRow(int displayRow, String tenderId, String departmentName,
                                Set<String> existingTenderIds, Set<String> fileTenderIds,
                                StringBuilder rowErr) {
        boolean isDuplicate = false;
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
        return isDuplicate;
    }

    private List<com.krs.backend.models.ProjectLocation> buildLocations(Project p) {
        ZoneId zone = ZoneId.systemDefault();
        List<com.krs.backend.models.ProjectLocation> locList = new ArrayList<>();
        String noticeNo = p.getNoticeNo();
        String nameOfWork = p.getNameOfWork();
        String rawLocSource = (noticeNo != null && noticeNo.contains(";")) ? noticeNo : nameOfWork;
        if (rawLocSource != null && rawLocSource.contains(";")) {
            String[] parts = rawLocSource.split(";");
            for (String part : parts) {
                String cleanLoc = part.trim();
                if (!cleanLoc.isEmpty()) {
                    locList.add(createProjectLocation(p, cleanLoc, zone));
                }
            }
        } else {
            String cleanLoc = rawLocSource != null ? rawLocSource.trim() : null;
            locList.add(createProjectLocation(p, cleanLoc, zone));
        }
        return locList;
    }

    private com.krs.backend.models.ProjectLocation createProjectLocation(Project p, String villageName, ZoneId zone) {
        return com.krs.backend.models.ProjectLocation.builder()
                .project(p)
                .villageName(villageName)
                .tenderId(p.getTenderId())
                .startDate(p.getWorkOrderDate() != null ? p.getWorkOrderDate() : p.getDateOfSub())
                .closedDate(p.getCompletionDateActual())
                .status(p.getStatus() != null ? p.getStatus() : "Running")
                .createdAt(LocalDateTime.now(zone))
                .updatedAt(LocalDateTime.now(zone))
                .build();
    }

    /**
     * Download Excel Report for a specific Bulk Upload execution (ALL, SUCCESS, or FAILED rows)
     */
    @Transactional(readOnly = true)
    public void exportUploadHistoryDataToExcel(Long masterId, String filterType, OutputStream outputStream) throws IOException {
        if (!uploadHistoryRepository.existsById(masterId)) {
            throw new IllegalArgumentException("Upload history record not found with ID: " + masterId);
        }

        List<BulkUploadProjectsData> list;
        if (STATUS_SUCCESS.equalsIgnoreCase(filterType)) {
            list = bulkUploadDataRepository.findByMasterIdAndIsFailedOrderByIdAsc(masterId, false);
        } else if (STATUS_FAILED.equalsIgnoreCase(filterType)) {
            list = bulkUploadDataRepository.findByMasterIdAndIsFailedOrderByIdAsc(masterId, true);
        } else {
            list = bulkUploadDataRepository.findByMasterIdOrderByIdAsc(masterId);
        }

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Upload Report - " + filterType.toUpperCase());

            List<String> reportHeaders = new ArrayList<>(HEADERS);
            reportHeaders.add("UPLOAD STATUS");
            reportHeaders.add("FAILURE REASON");
            writeHeaderRow(sheet, workbook, reportHeaders);

            int rowIndex = 1;
            for (BulkUploadProjectsData p : list) {
                Row row = sheet.createRow(rowIndex++);
                populateUploadDataRow(row, p);

                row.createCell(33).setCellValue(Boolean.TRUE.equals(p.getIsFailed()) ? STATUS_FAILED : STATUS_SUCCESS);
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

    private static void setCellString(Row row, int colIndex, String val) {
        row.createCell(colIndex).setCellValue(val != null ? val : "");
    }

    private static void setCellDouble(Row row, int colIndex, BigDecimal val) {
        row.createCell(colIndex).setCellValue(val != null ? val.doubleValue() : 0.0);
    }

    private void writeHeaderRow(Sheet sheet, Workbook workbook, List<String> headers) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }
    }

    private void populateProjectRow(Row row, Project p) {
        setCellString(row, 0, formatDate(p.getDateOfSub()));
        setCellString(row, 1, resolveDepartmentDisplay(p.getDepartmentName()));
        setCellString(row, 2, p.getTenderId());
        setCellString(row, 3, resolveNoticeOrPackage(p.getNoticeNo(), p.getPackageNo()));
        setCellString(row, 4, p.getNameOfWork());
        setCellString(row, 5, resolveRelatedToDisplay(p.getRelatedTo()));
        setCellDouble(row, 6, p.getTenderFee());
        setCellString(row, 7, p.getTenderFeeNo());
        setCellDouble(row, 8, p.getEmdAmt());
        setCellString(row, 9, p.getEmdNo());
        setCellDouble(row, 10, p.getEstimatedTenderCost());
        setCellDouble(row, 11, p.getTenderedCost());
        setCellString(row, 12, p.getAboveBelowPercentage() != null ? p.getAboveBelowPercentage().toString() : null);
        setCellString(row, 13, resolveRefPersonDisplay(p.getRefPerson()));
        setCellString(row, 14, p.getWorkAwardedStatus());
        setCellString(row, 15, p.getWorkOrderNumber());
        setCellString(row, 16, formatDate(p.getWorkOrderDate()));
        setCellString(row, 17, p.getTimeLimit());
        setCellDouble(row, 18, p.getSecurityDepositAmount());
        setCellString(row, 19, p.getSdFdrNo());
        setCellString(row, 20, p.getRemarks());
        setCellDouble(row, 21, p.getSdRabDeduction());
        setCellDouble(row, 22, p.getSdRabReturnAmount());
        setCellString(row, 23, p.getAdditionalDeduction());
        setCellDouble(row, 24, p.getWorkCompletedAmount());
        setCellDouble(row, 25, p.getPendingWorkAmount());
        setCellString(row, 26, formatDate(p.getCompletionDateActual()));
        setCellString(row, 27, p.getDefectsLiabilityPeriod());
        setCellString(row, 28, formatDate(p.getDlpEndedOn()));
        setCellString(row, 29, p.getEmdReturnStatus());
        setCellString(row, 30, p.getSdReturnStatus());
        setCellString(row, 31, p.getSdRmRabReturnStatus());
        setCellString(row, 32, p.getStatus());
    }

    private void populateUploadDataRow(Row row, BulkUploadProjectsData p) {
        setCellString(row, 0, formatDate(p.getDateOfSub()));
        setCellString(row, 1, resolveDepartmentDisplay(p.getDepartmentName()));
        setCellString(row, 2, p.getTenderId());
        setCellString(row, 3, resolveNoticeOrPackage(p.getNoticeNo(), p.getPackageNo()));
        setCellString(row, 4, p.getNameOfWork());
        setCellString(row, 5, resolveRelatedToDisplay(p.getRelatedTo()));
        setCellDouble(row, 6, p.getTenderFee());
        setCellString(row, 7, p.getTenderFeeNo());
        setCellDouble(row, 8, p.getEmdAmt());
        setCellString(row, 9, p.getEmdNo());
        setCellDouble(row, 10, p.getEstimatedTenderCost());
        setCellDouble(row, 11, p.getTenderedCost());
        setCellString(row, 12, p.getAboveBelowPercentage() != null ? p.getAboveBelowPercentage().toString() : null);
        setCellString(row, 13, resolveRefPersonDisplay(p.getRefPerson()));
        setCellString(row, 14, p.getWorkAwardedStatus());
        setCellString(row, 15, p.getWorkOrderNumber());
        setCellString(row, 16, formatDate(p.getWorkOrderDate()));
        setCellString(row, 17, p.getTimeLimit());
        setCellDouble(row, 18, p.getSecurityDepositAmount());
        setCellString(row, 19, p.getSdFdrNo());
        setCellString(row, 20, p.getRemarks());
        setCellDouble(row, 21, p.getSdRabDeduction());
        setCellDouble(row, 22, p.getSdRabReturnAmount());
        setCellString(row, 23, p.getAdditionalDeduction());
        setCellDouble(row, 24, p.getWorkCompletedAmount());
        setCellDouble(row, 25, p.getPendingWorkAmount());
        setCellString(row, 26, formatDate(p.getCompletionDateActual()));
        setCellString(row, 27, p.getDefectsLiabilityPeriod());
        setCellString(row, 28, formatDate(p.getDlpEndedOn()));
        setCellString(row, 29, p.getEmdReturnStatus());
        setCellString(row, 30, p.getSdReturnStatus());
        setCellString(row, 31, p.getSdRmRabReturnStatus());
        setCellString(row, 32, p.getStatus());
    }

    private String resolveNoticeOrPackage(String noticeNo, String packageNo) {
        if (noticeNo != null && !noticeNo.isEmpty()) {
            return noticeNo;
        }
        return packageNo != null ? packageNo : "";
    }

    private String resolveMasterIdOrTitleCase(String masterIdStr, String rawVal, String titleCaseVal) {
        if (masterIdStr != null) {
            return masterIdStr;
        }
        return rawVal != null ? titleCaseVal : null;
    }

    private String resolveDepartmentDisplay(String val) {
        if (val == null || val.trim().isEmpty()) return "";
        String trimmed = val.trim();
        try {
            Long id = Long.parseLong(trimmed);
            return departmentMasterService.findById(id)
                    .map(dept -> dept.getName())
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
                    .map(ref -> ref.getName())
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
                    .map(rel -> rel.getName())
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
            } catch (Exception ignored) {
                // Ignore date parsing exception for non-matching date format
            }
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
            case ERROR, BLANK:
            default:
                return null;
        }

        if (rawVal != null) {
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
