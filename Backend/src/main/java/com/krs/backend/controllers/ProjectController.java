package com.krs.backend.controllers;

import com.krs.backend.models.Project;
import com.krs.backend.models.ProjectLocation;
import com.krs.backend.repositories.ProjectRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.persistence.EntityManager;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.CriteriaQuery;

import com.krs.backend.models.ProjectUploadHistory;
import com.krs.backend.services.ProjectExcelService;
import com.krs.backend.services.SystemErrorLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@SuppressWarnings("java:S4684")
@CrossOrigin(origins = "${app.cors.origins}", maxAge = 3600)
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private static final String PARAM_TENDER_ID = "tenderId";
    private static final String PARAM_PACKAGE_NO = "packageNo";
    private static final String PARAM_NOTICE_NO = "noticeNo";
    private static final String PARAM_DEPARTMENT_NAME = "departmentName";
    private static final String PARAM_NAME_OF_WORK = "nameOfWork";
    private static final String PARAM_VILLAGE_NAME = "villageName";
    private static final String PARAM_RELATED_TO = "relatedTo";
    private static final String PARAM_REF_PERSON = "refPerson";
    private static final String PARAM_WORK_AWARDED_STATUS = "workAwardedStatus";
    private static final String PARAM_EMD_RETURN_STATUS = "emdReturnStatus";
    private static final String PARAM_STATUS = "status";
    private static final String PARAM_TENDER_AMT_MIN = "tenderAmtMin";
    private static final String PARAM_TENDER_AMT_MAX = "tenderAmtMax";
    private static final String PARAM_AGREEMENT_AMT_MIN = "agreementAmtMin";
    private static final String PARAM_AGREEMENT_AMT_MAX = "agreementAmtMax";
    private static final String PARAM_START_DATE_MIN = "startDateMin";
    private static final String PARAM_START_DATE_MAX = "startDateMax";
    private static final String PARAM_WORK_ORDER_DATE = "workOrderDate";

    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;
    private final ProjectExcelService projectExcelService;
    private final SystemErrorLogService systemErrorLogService;
    private final com.krs.backend.services.DepartmentMasterService departmentMasterService;
    private final com.krs.backend.services.RelatedToMasterService relatedToMasterService;
    private final com.krs.backend.services.RefPersonMasterService refPersonMasterService;

    @org.springframework.beans.factory.annotation.Value("${app.dashboard.max-time-limit-days:90}")
    private int maxTimeLimitDays;

    public ProjectController(ProjectRepository projectRepository,
                             EntityManager entityManager,
                             ProjectExcelService projectExcelService,
                             SystemErrorLogService systemErrorLogService,
                             com.krs.backend.services.DepartmentMasterService departmentMasterService,
                             com.krs.backend.services.RelatedToMasterService relatedToMasterService,
                             com.krs.backend.services.RefPersonMasterService refPersonMasterService) {
        this.projectRepository = projectRepository;
        this.entityManager = entityManager;
        this.projectExcelService = projectExcelService;
        this.systemErrorLogService = systemErrorLogService;
        this.departmentMasterService = departmentMasterService;
        this.relatedToMasterService = relatedToMasterService;
        this.refPersonMasterService = refPersonMasterService;
    }

    /**
     * Upload Projects Excel File with streaming batching and error details logging
     */
    @PostMapping("/upload")
    public ResponseEntity<ProjectUploadHistory> uploadProjectsExcel(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        String username = "system";
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                username = SecurityContextHolder.getContext().getAuthentication().getName();
            }
        } catch (Exception ignored) {}

        try {
            ProjectUploadHistory history = projectExcelService.uploadProjectsExcel(file, username);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            systemErrorLogService.logError(e.getClass().getSimpleName(), e.getMessage(), e, request, HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(ProjectUploadHistory.builder()
                    .filename(file.getOriginalFilename())
                    .uploadedBy(username)
                    .totalRows(0)
                    .successCount(0)
                    .failedCount(0)
                    .status("FAILED")
                    .errorDetails("[\"" + e.getMessage() + "\"]")
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            systemErrorLogService.logError(e.getClass().getSimpleName(), e.getMessage(), e, request, HttpStatus.INTERNAL_SERVER_ERROR.value());
            String detailMsg = e.getMessage() != null ? e.getMessage() : e.toString();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                detailMsg += " | Cause: " + e.getCause().getMessage();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ProjectUploadHistory.builder()
                    .filename(file.getOriginalFilename())
                    .uploadedBy(username)
                    .totalRows(0)
                    .successCount(0)
                    .failedCount(0)
                    .status("FAILED")
                    .errorDetails("[\"Failed to process excel file: " + detailMsg.replace("\"", "'") + "\"]")
                    .build());
        }
    }

    /**
     * Get Upload History records
     */
    @GetMapping("/upload-history")
    public ResponseEntity<List<ProjectUploadHistory>> getUploadHistory() {
        return ResponseEntity.ok(projectExcelService.getUploadHistory());
    }

    /**
     * Download Excel Report for a specific Bulk Upload execution (ALL, SUCCESS, or FAILED rows)
     */
    @GetMapping("/upload-history/{id}/export")
    public ResponseEntity<StreamingResponseBody> exportUploadHistoryExcel(
            @PathVariable Long id,
            @RequestParam(defaultValue = "ALL") String type,
            HttpServletRequest request) {

        StreamingResponseBody responseBody = outputStream -> {
            try {
                projectExcelService.exportUploadHistoryDataToExcel(id, type, outputStream);
            } catch (Exception e) {
                systemErrorLogService.logError(e.getClass().getSimpleName(), "Excel Export Failed: " + e.getMessage(), e, request, HttpStatus.INTERNAL_SERVER_ERROR.value());
                throw new RuntimeException("Failed to export upload history data", e);
            }
        };

        String filename = String.format("Upload_History_%d_%s.xlsx", id, type.toUpperCase());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(responseBody);
    }

    /**
     * Download Excel Template
     */
    @GetMapping("/template")
    public ResponseEntity<StreamingResponseBody> downloadTemplate(HttpServletRequest request) {
        StreamingResponseBody responseBody = outputStream -> {
            try {
                projectExcelService.generateTemplate(outputStream);
            } catch (Exception e) {
                systemErrorLogService.logError(e.getClass().getSimpleName(), "Template Download Failed: " + e.getMessage(), e, request, HttpStatus.INTERNAL_SERVER_ERROR.value());
                throw new RuntimeException("Failed to generate template", e);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Tender_Details_Template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(responseBody);
    }

    /**
     * Stream Export Projects Excel
     */
    @GetMapping("/export/excel")
    public ResponseEntity<StreamingResponseBody> exportProjectsExcel() {
        StreamingResponseBody responseBody = outputStream -> {
            try {
                projectExcelService.exportProjectsToExcel(outputStream);
            } catch (Exception e) {
                throw new RuntimeException("Failed to export projects", e);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Projects_Master_Export.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(responseBody);
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getProjectConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("maxTimeLimitDays", maxTimeLimitDays);
        return ResponseEntity.ok(config);
    }

    @GetMapping
    public Map<String, Object> getAllProjects(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam Map<String, String> allParams) {
        
        String sortField = (sortBy != null && SORTABLE_FIELDS.contains(sortBy)) ? sortBy : null;
        boolean ascending = "asc".equalsIgnoreCase(sortDir);

        // Ordering is applied inside the Specification, so the PageRequest stays unsorted
        PageRequest pageRequest = PageRequest.of(offset / limit, limit, Sort.unsorted());
        
        Specification<Project> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            addTextPredicates(allParams, root, cb, predicates);
            addStatusPredicates(allParams, root, cb, predicates);
            addNumericAndDatePredicates(allParams, root, cb, predicates);

            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                if (sortField != null) {
                    applyColumnSort(sortField, ascending, root, query, cb);
                } else {
                    applyDefaultSort(root, query, cb);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        long total = projectRepository.count(spec);
        List<Project> projects = projectRepository.findAll(spec, pageRequest).getContent();
        
        for (Project p : projects) {
            entityManager.detach(p);
            p.setLocations(null);
            p.setRaBills(null);
            p.setApprovals(null);
        }

        resolveMasterDisplayNames(projects);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", projects);
        response.put("total", total);
        return response;
    }

    private void resolveMasterDisplayNames(List<Project> projects) {
        if (projects == null || projects.isEmpty()) return;

        Map<String, String> deptMap = departmentMasterService.getAllDepartmentMasters().stream()
                .filter(d -> d.getId() != null && d.getName() != null)
                .collect(Collectors.toMap(d -> d.getId().toString(), com.krs.backend.models.DepartmentMaster::getName, (a, b) -> a));

        Map<String, String> relatedToMap = relatedToMasterService.getAllRelatedToMasters().stream()
                .filter(r -> r.getId() != null && r.getName() != null)
                .collect(Collectors.toMap(r -> r.getId().toString(), com.krs.backend.models.RelatedToMaster::getName, (a, b) -> a));

        Map<String, String> refPersonMap = refPersonMasterService.getAllRefPersonMasters().stream()
                .filter(r -> r.getId() != null && r.getName() != null)
                .collect(Collectors.toMap(r -> r.getId().toString(), com.krs.backend.models.RefPersonMaster::getName, (a, b) -> a));

        for (Project p : projects) {
            if (p.getDepartmentName() != null && !p.getDepartmentName().trim().isEmpty()) {
                String key = p.getDepartmentName().trim();
                p.setDepartmentName(deptMap.getOrDefault(key, key));
            }
            if (p.getRelatedTo() != null && !p.getRelatedTo().trim().isEmpty()) {
                String key = p.getRelatedTo().trim();
                p.setRelatedTo(relatedToMap.getOrDefault(key, key));
            }
            if (p.getRefPerson() != null && !p.getRefPerson().trim().isEmpty()) {
                String key = p.getRefPerson().trim();
                p.setRefPerson(refPersonMap.getOrDefault(key, key));
            }
        }
    }

    private static final Set<String> TEXT_SORT_FIELDS = Set.of(
        PARAM_TENDER_ID, PARAM_DEPARTMENT_NAME, "relatedTo", PARAM_WORK_AWARDED_STATUS, "emdReturnStatus", PARAM_STATUS
    );
    private static final Set<String> SORTABLE_FIELDS = Set.of(
        PARAM_NOTICE_NO, PARAM_TENDER_ID, PARAM_DEPARTMENT_NAME, "relatedTo", "estimatedTenderCost", "tenderedCost",
        "aboveBelowPercentage", PARAM_WORK_AWARDED_STATUS, PARAM_WORK_ORDER_DATE, "emdReturnStatus", PARAM_STATUS
    );

    private void applyColumnSort(String field, boolean ascending, Root<Project> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Expression<?> expr;
        if (PARAM_NOTICE_NO.equals(field)) {
            // Column displays noticeNo || packageNo, so sort on the same value
            expr = cb.lower(cb.coalesce(root.<String>get(PARAM_NOTICE_NO), root.<String>get(PARAM_PACKAGE_NO)));
        } else if ("aboveBelowPercentage".equals(field)) {
            // Sort on the query-computed variance (same value shown in UI)
            expr = root.get("variancePct");
        } else if (TEXT_SORT_FIELDS.contains(field)) {
            expr = cb.lower(root.<String>get(field));
        } else {
            expr = root.get(field);
        }
        Expression<Integer> nullsLast = cb.<Integer>selectCase().when(cb.isNull(expr), 1).otherwise(0);
        query.orderBy(
            cb.asc(nullsLast),
            ascending ? cb.asc(expr) : cb.desc(expr),
            ascending ? cb.asc(root.get("id")) : cb.desc(root.get("id"))
        );
    }

    private void applyDefaultSort(Root<Project> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Expression<String> statusLower = cb.lower(cb.coalesce(root.get(PARAM_STATUS), ""));
        Expression<String> awardedLower = cb.lower(cb.coalesce(root.get(PARAM_WORK_AWARDED_STATUS), ""));

        Expression<Integer> isCompleted = cb.<Integer>selectCase()
            .when(cb.or(
                cb.equal(statusLower, "completed"),
                cb.equal(statusLower, "work completed"),
                cb.equal(awardedLower, "work completed")
            ), 1)
            .otherwise(0);

        Expression<?> compDate = cb.coalesce(
            root.get("completionDateActual"),
            root.get(PARAM_WORK_ORDER_DATE)
        );

        query.orderBy(
            cb.asc(isCompleted),
            cb.desc(compDate),
            cb.desc(root.get("id"))
        );
    }

    private void addTextPredicates(Map<String, String> allParams, Root<Project> root, CriteriaBuilder cb, List<Predicate> predicates) {
        if (allParams.containsKey("srNo") && !allParams.get("srNo").trim().isEmpty()) {
            String srVal = allParams.get("srNo").trim();
            try {
                long num = Long.parseLong(srVal);
                predicates.add(cb.equal(root.get("id"), num));
            } catch (NumberFormatException e) {
                predicates.add(cb.like(cb.lower(root.get(PARAM_TENDER_ID)), "%" + srVal.toLowerCase() + "%"));
            }
        }
        if (allParams.containsKey(PARAM_PACKAGE_NO) && !allParams.get(PARAM_PACKAGE_NO).trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get(PARAM_PACKAGE_NO)), "%" + allParams.get(PARAM_PACKAGE_NO).trim().toLowerCase() + "%"));
        }
        if (allParams.containsKey(PARAM_NOTICE_NO) && !allParams.get(PARAM_NOTICE_NO).trim().isEmpty()) {
            String val = "%" + allParams.get(PARAM_NOTICE_NO).trim().toLowerCase() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(cb.coalesce(root.get(PARAM_NOTICE_NO), "")), val),
                cb.like(cb.lower(cb.coalesce(root.get(PARAM_PACKAGE_NO), "")), val)
            ));
        }
        if (allParams.containsKey(PARAM_TENDER_ID) && !allParams.get(PARAM_TENDER_ID).trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get(PARAM_TENDER_ID)), "%" + allParams.get(PARAM_TENDER_ID).trim().toLowerCase() + "%"));
        }
        if (allParams.containsKey(PARAM_DEPARTMENT_NAME) && !allParams.get(PARAM_DEPARTMENT_NAME).trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get(PARAM_DEPARTMENT_NAME)), "%" + allParams.get(PARAM_DEPARTMENT_NAME).trim().toLowerCase() + "%"));
        }
        if (allParams.containsKey(PARAM_NAME_OF_WORK) && !allParams.get(PARAM_NAME_OF_WORK).trim().isEmpty()) {
            String val = "%" + allParams.get(PARAM_NAME_OF_WORK).trim().toLowerCase() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(cb.coalesce(root.get(PARAM_NAME_OF_WORK), "")), val),
                cb.like(cb.lower(cb.coalesce(root.get(PARAM_DEPARTMENT_NAME), "")), val)
            ));
        }
    }

    private void addStatusPredicates(Map<String, String> allParams, Root<Project> root, CriteriaBuilder cb, List<Predicate> predicates) {
        if (allParams.containsKey(PARAM_VILLAGE_NAME) && !allParams.get(PARAM_VILLAGE_NAME).trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get(PARAM_VILLAGE_NAME)), "%" + allParams.get(PARAM_VILLAGE_NAME).trim().toLowerCase() + "%"));
        }
        if (allParams.containsKey(PARAM_RELATED_TO) && !allParams.get(PARAM_RELATED_TO).trim().isEmpty()) {
            String val = "%" + allParams.get(PARAM_RELATED_TO).trim().toLowerCase() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(cb.coalesce(root.get(PARAM_RELATED_TO), "")), val),
                cb.like(cb.lower(cb.coalesce(root.get(PARAM_REF_PERSON), "")), val)
            ));
        }
        if (allParams.containsKey(PARAM_REF_PERSON) && !allParams.get(PARAM_REF_PERSON).trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get(PARAM_REF_PERSON)), "%" + allParams.get(PARAM_REF_PERSON).trim().toLowerCase() + "%"));
        }
        if (allParams.containsKey(PARAM_WORK_AWARDED_STATUS) && !allParams.get(PARAM_WORK_AWARDED_STATUS).trim().isEmpty()) {
            String val = "%" + allParams.get(PARAM_WORK_AWARDED_STATUS).trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get(PARAM_WORK_AWARDED_STATUS), "")), val));
        }
        if (allParams.containsKey(PARAM_EMD_RETURN_STATUS) && !allParams.get(PARAM_EMD_RETURN_STATUS).trim().isEmpty()) {
            String val = "%" + allParams.get(PARAM_EMD_RETURN_STATUS).trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get(PARAM_EMD_RETURN_STATUS), "")), val));
        }
        if (allParams.containsKey(PARAM_STATUS) && !allParams.get(PARAM_STATUS).trim().isEmpty()) {
            String val = "%" + allParams.get(PARAM_STATUS).trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(cb.coalesce(root.get(PARAM_STATUS), "")), val));
        }
    }

    private void addNumericAndDatePredicates(Map<String, String> allParams, Root<Project> root, CriteriaBuilder cb, List<Predicate> predicates) {
        if (allParams.containsKey(PARAM_TENDER_AMT_MIN) && !allParams.get(PARAM_TENDER_AMT_MIN).trim().isEmpty()) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("estimatedTenderCost"), Double.parseDouble(allParams.get(PARAM_TENDER_AMT_MIN))));
        }
        if (allParams.containsKey(PARAM_TENDER_AMT_MAX) && !allParams.get(PARAM_TENDER_AMT_MAX).trim().isEmpty()) {
            predicates.add(cb.lessThanOrEqualTo(root.get("estimatedTenderCost"), Double.parseDouble(allParams.get(PARAM_TENDER_AMT_MAX))));
        }
        if (allParams.containsKey(PARAM_AGREEMENT_AMT_MIN) && !allParams.get(PARAM_AGREEMENT_AMT_MIN).trim().isEmpty()) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("tenderedCost"), Double.parseDouble(allParams.get(PARAM_AGREEMENT_AMT_MIN))));
        }
        if (allParams.containsKey(PARAM_AGREEMENT_AMT_MAX) && !allParams.get(PARAM_AGREEMENT_AMT_MAX).trim().isEmpty()) {
            predicates.add(cb.lessThanOrEqualTo(root.get("tenderedCost"), Double.parseDouble(allParams.get(PARAM_AGREEMENT_AMT_MAX))));
        }
        if (allParams.containsKey(PARAM_START_DATE_MIN) && !allParams.get(PARAM_START_DATE_MIN).trim().isEmpty()) {
            predicates.add(cb.greaterThanOrEqualTo(root.get(PARAM_WORK_ORDER_DATE), java.time.LocalDate.parse(allParams.get(PARAM_START_DATE_MIN))));
        }
        if (allParams.containsKey(PARAM_START_DATE_MAX) && !allParams.get(PARAM_START_DATE_MAX).trim().isEmpty()) {
            predicates.add(cb.lessThanOrEqualTo(root.get(PARAM_WORK_ORDER_DATE), java.time.LocalDate.parse(allParams.get(PARAM_START_DATE_MAX))));
        }
    }

    public static void syncProjectDatesFromLocations(Project project) {
        if (project.getLocations() == null || project.getLocations().isEmpty()) {
            return;
        }

        java.time.LocalDate minStart = null;
        java.time.LocalDate maxClosed = null;

        for (ProjectLocation loc : project.getLocations()) {
            loc.setProject(project);
            if (loc.getStartDate() != null && (minStart == null || loc.getStartDate().isBefore(minStart))) {
                minStart = loc.getStartDate();
            }
            if (loc.getClosedDate() != null && (maxClosed == null || loc.getClosedDate().isAfter(maxClosed))) {
                maxClosed = loc.getClosedDate();
            }
        }

        if (minStart != null) {
            project.setWorkOrderDate(minStart);
        }
        if (maxClosed != null) {
            project.setCompletionDateActual(maxClosed);
        }
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        syncProjectDatesFromLocations(project);
        Project savedProject = projectRepository.save(project);
        return ResponseEntity.ok(savedProject);
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id).map(project -> {
            // Touch lazy-loaded collections to initialize them within the transaction
            if (project.getLocations() != null) { project.getLocations().size(); }
            if (project.getRaBills() != null) { project.getRaBills().size(); }
            if (project.getApprovals() != null) { project.getApprovals().size(); }
            resolveMasterDisplayNames(Collections.singletonList(project));
            return ResponseEntity.ok(project);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project projectDetails) {
        return projectRepository.findById(id).map(project -> {
            project.setWorkOrderNumber(projectDetails.getWorkOrderNumber());
            project.setDepartmentName(projectDetails.getDepartmentName());
            project.setDateOfSub(projectDetails.getDateOfSub());
            project.setPackageNo(projectDetails.getPackageNo());
            project.setNoticeNo(projectDetails.getNoticeNo());
            project.setTenderId(projectDetails.getTenderId());
            project.setNameOfWork(projectDetails.getNameOfWork());
            project.setRelatedTo(projectDetails.getRelatedTo());
            project.setTenderFee(projectDetails.getTenderFee());
            project.setTenderFeeNo(projectDetails.getTenderFeeNo());
            project.setDdNo(projectDetails.getDdNo());
            project.setEmdAmt(projectDetails.getEmdAmt());
            project.setEmdNo(projectDetails.getEmdNo());
            project.setEstimatedTenderCost(projectDetails.getEstimatedTenderCost());
            project.setTenderedCost(projectDetails.getTenderedCost());
            project.setAboveBelowPercentage(projectDetails.getAboveBelowPercentage());
            project.setRefPerson(projectDetails.getRefPerson());
            project.setWorkAwardedStatus(projectDetails.getWorkAwardedStatus());
            project.setWorkOrderDate(projectDetails.getWorkOrderDate());
            project.setTimeLimit(projectDetails.getTimeLimit());
            project.setSecurityDepositAmount(projectDetails.getSecurityDepositAmount());
            project.setSdFdrNo(projectDetails.getSdFdrNo());
            project.setRemarks(projectDetails.getRemarks());
            project.setSdRabDeduction(projectDetails.getSdRabDeduction());
            project.setSdRabReturnAmount(projectDetails.getSdRabReturnAmount());
            project.setAdditionalDeduction(projectDetails.getAdditionalDeduction());
            project.setWorkCompletedAmount(projectDetails.getWorkCompletedAmount());
            project.setPendingWorkAmount(projectDetails.getPendingWorkAmount());
            project.setCompletionDateActual(projectDetails.getCompletionDateActual());
            project.setDefectsLiabilityPeriod(projectDetails.getDefectsLiabilityPeriod());
            project.setDlpEndedOn(projectDetails.getDlpEndedOn());
            project.setEmdReturnStatus(projectDetails.getEmdReturnStatus());
            project.setSdReturnStatus(projectDetails.getSdReturnStatus());
            project.setSdRmRabReturnStatus(projectDetails.getSdRmRabReturnStatus());
            project.setStatus(projectDetails.getStatus());
            project.setRetentionMoneyPerBill(projectDetails.getRetentionMoneyPerBill());
            project.setExtraExcessAmount(projectDetails.getExtraExcessAmount());
            project.setTimeLimitExtension(projectDetails.getTimeLimitExtension());
            
            if (projectDetails.getLocations() != null) {
                project.getLocations().clear();
                for (ProjectLocation loc : projectDetails.getLocations()) {
                    loc.setProject(project);
                    project.getLocations().add(loc);
                }
            }

            syncProjectDatesFromLocations(project);
            Project updated = projectRepository.save(project);
            return ResponseEntity.ok(updated);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id) {
        return projectRepository.findById(id).map(project -> {
            projectRepository.delete(project);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @PostMapping("/{id}/locations")
    public ResponseEntity<ProjectLocation> addLocation(@PathVariable Long id, @RequestBody ProjectLocation location) {
        return projectRepository.findById(id).map(project -> {
            location.setProject(project);
            location.setTenderId(project.getTenderId());
            if (project.getLocations() == null) {
                project.setLocations(new ArrayList<>());
            }
            project.getLocations().add(location);
            syncProjectDatesFromLocations(project);
            projectRepository.save(project);
            return ResponseEntity.ok(location);
        }).orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @PutMapping("/{id}/locations/{locationId}")
    public ResponseEntity<ProjectLocation> updateLocation(
            @PathVariable Long id, 
            @PathVariable Long locationId, 
            @RequestBody ProjectLocation locationDetails) {
        return projectRepository.findById(id).map(project -> {
            ProjectLocation target = null;
            if (project.getLocations() != null) {
                for (ProjectLocation loc : project.getLocations()) {
                    if (loc.getId() != null && loc.getId().equals(locationId)) {
                        target = loc;
                        break;
                    }
                }
            }
            if (target != null) {
                target.setTenderId(project.getTenderId());
                target.setVillageName(locationDetails.getVillageName());
                target.setTaluka(locationDetails.getTaluka());
                target.setDistrict(locationDetails.getDistrict());
                target.setBlock(locationDetails.getBlock());
                target.setSchoolId(locationDetails.getSchoolId());
                target.setSchoolName(locationDetails.getSchoolName());
                target.setHead(locationDetails.getHead());
                target.setRepairing(locationDetails.getRepairing());
                target.setNewAcr(locationDetails.getNewAcr());
                target.setNewMdmSqm(locationDetails.getNewMdmSqm());
                target.setNewCwRmt(locationDetails.getNewCwRmt());
                target.setGtb(locationDetails.getGtb());
                target.setBtb(locationDetails.getBtb());
                target.setCwsnToilet(locationDetails.getCwsnToilet());
                target.setShed(locationDetails.getShed());
                target.setStatus(locationDetails.getStatus());
                target.setPhysicalProgress(locationDetails.getPhysicalProgress());
                target.setFinancialProgress(locationDetails.getFinancialProgress());
                target.setTimeLimit(locationDetails.getTimeLimit());
                target.setStartDate(locationDetails.getStartDate());
                target.setClosedDate(locationDetails.getClosedDate());

                syncProjectDatesFromLocations(project);
                projectRepository.save(project);
                return ResponseEntity.ok(target);
            }
            return ResponseEntity.notFound().<ProjectLocation>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @Transactional
    @DeleteMapping("/{id}/locations/{locationId}")
    public ResponseEntity<?> deleteLocation(@PathVariable Long id, @PathVariable Long locationId) {
        return projectRepository.findById(id).map(project -> {
            if (project.getLocations() != null) {
                project.getLocations().removeIf(loc -> loc.getId() != null && loc.getId().equals(locationId));
            }
            syncProjectDatesFromLocations(project);
            projectRepository.save(project);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
