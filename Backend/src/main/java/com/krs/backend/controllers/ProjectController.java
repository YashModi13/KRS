package com.krs.backend.controllers;

import com.krs.backend.models.Project;
import com.krs.backend.repositories.ProjectRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Expression;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "${app.cors.origins}", maxAge = 3600)
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;

    @org.springframework.beans.factory.annotation.Value("${app.dashboard.max-time-limit-days:90}")
    private int maxTimeLimitDays;

    public ProjectController(ProjectRepository projectRepository, EntityManager entityManager) {
        this.projectRepository = projectRepository;
        this.entityManager = entityManager;
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
        
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isEmpty()) {
            sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        }
        
        PageRequest pageRequest = PageRequest.of(offset / limit, limit, sort);
        
        Specification<Project> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (allParams.containsKey("packageNo") && !allParams.get("packageNo").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("packageNo")), "%" + allParams.get("packageNo").toLowerCase() + "%"));
            }
            if (allParams.containsKey("noticeNo") && !allParams.get("noticeNo").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("noticeNo")), "%" + allParams.get("noticeNo").toLowerCase() + "%"));
            }
            if (allParams.containsKey("tenderId") && !allParams.get("tenderId").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("tenderId")), "%" + allParams.get("tenderId").toLowerCase() + "%"));
            }
            if (allParams.containsKey("departmentName") && !allParams.get("departmentName").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("departmentName")), "%" + allParams.get("departmentName").toLowerCase() + "%"));
            }
            if (allParams.containsKey("nameOfWork") && !allParams.get("nameOfWork").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("nameOfWork")), "%" + allParams.get("nameOfWork").toLowerCase() + "%"));
            }
            if (allParams.containsKey("villageName") && !allParams.get("villageName").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("villageName")), "%" + allParams.get("villageName").toLowerCase() + "%"));
            }
            if (allParams.containsKey("relatedTo") && !allParams.get("relatedTo").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("relatedTo")), "%" + allParams.get("relatedTo").toLowerCase() + "%"));
            }
            if (allParams.containsKey("refPerson") && !allParams.get("refPerson").isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("refPerson")), "%" + allParams.get("refPerson").toLowerCase() + "%"));
            }
            if (allParams.containsKey("workAwardedStatus") && !allParams.get("workAwardedStatus").isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("workAwardedStatus")), allParams.get("workAwardedStatus").toLowerCase()));
            }
            if (allParams.containsKey("status") && !allParams.get("status").isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("status")), allParams.get("status").toLowerCase()));
            }
            if (allParams.containsKey("tenderAmtMin") && !allParams.get("tenderAmtMin").isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("estimatedTenderCost"), Double.parseDouble(allParams.get("tenderAmtMin"))));
            }
            if (allParams.containsKey("tenderAmtMax") && !allParams.get("tenderAmtMax").isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("estimatedTenderCost"), Double.parseDouble(allParams.get("tenderAmtMax"))));
            }
            if (allParams.containsKey("agreementAmtMin") && !allParams.get("agreementAmtMin").isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("tenderedCost"), Double.parseDouble(allParams.get("agreementAmtMin"))));
            }
            if (allParams.containsKey("agreementAmtMax") && !allParams.get("agreementAmtMax").isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("tenderedCost"), Double.parseDouble(allParams.get("agreementAmtMax"))));
            }
            if (allParams.containsKey("startDateMin") && !allParams.get("startDateMin").isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("workOrderDate"), java.time.LocalDate.parse(allParams.get("startDateMin"))));
            }
            if (allParams.containsKey("startDateMax") && !allParams.get("startDateMax").isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("workOrderDate"), java.time.LocalDate.parse(allParams.get("startDateMax"))));
            }

            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                if (sortBy == null || sortBy.isEmpty()) {
                    Expression<String> statusLower = cb.lower(cb.coalesce(root.get("status"), ""));
                    Expression<String> awardedLower = cb.lower(cb.coalesce(root.get("workAwardedStatus"), ""));

                    Expression<Integer> isCompleted = cb.<Integer>selectCase()
                        .when(cb.or(
                            cb.equal(statusLower, "completed"),
                            cb.equal(statusLower, "work completed"),
                            cb.equal(awardedLower, "work completed")
                        ), 1)
                        .otherwise(0);

                    Expression<?> compDate = cb.coalesce(
                        root.get("completionDateActual"),
                        root.get("workOrderDate")
                    );

                    query.orderBy(
                        cb.asc(isCompleted),
                        cb.desc(compDate),
                        cb.desc(root.get("id"))
                    );
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
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", projects);
        response.put("total", total);
        return response;
    }

    public static void syncProjectDatesFromLocations(Project project) {
        if (project.getLocations() != null && !project.getLocations().isEmpty()) {
            java.time.LocalDate minStart = null;
            java.time.LocalDate maxClosed = null;

            for (com.krs.backend.models.ProjectLocation loc : project.getLocations()) {
                loc.setProject(project);
                if (loc.getStartDate() != null) {
                    if (minStart == null || loc.getStartDate().isBefore(minStart)) {
                        minStart = loc.getStartDate();
                    }
                }
                if (loc.getClosedDate() != null) {
                    if (maxClosed == null || loc.getClosedDate().isAfter(maxClosed)) {
                        maxClosed = loc.getClosedDate();
                    }
                }
            }

            if (minStart != null) {
                project.setWorkOrderDate(minStart);
            }
            if (maxClosed != null) {
                project.setCompletionDateActual(maxClosed);
            }
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
            if (project.getLocations() != null) project.getLocations().size();
            if (project.getRaBills() != null) project.getRaBills().size();
            if (project.getApprovals() != null) project.getApprovals().size();
            return ResponseEntity.ok(project);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project projectDetails) {
        return projectRepository.findById(id).map(project -> {
            project.setSrNo(projectDetails.getSrNo());
            project.setWorkOrderNumber(projectDetails.getWorkOrderNumber());
            project.setVillageName(projectDetails.getVillageName());
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
                for (com.krs.backend.models.ProjectLocation loc : projectDetails.getLocations()) {
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
    public ResponseEntity<com.krs.backend.models.ProjectLocation> addLocation(@PathVariable Long id, @RequestBody com.krs.backend.models.ProjectLocation location) {
        return projectRepository.findById(id).map(project -> {
            location.setProject(project);
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
    public ResponseEntity<com.krs.backend.models.ProjectLocation> updateLocation(
            @PathVariable Long id, 
            @PathVariable Long locationId, 
            @RequestBody com.krs.backend.models.ProjectLocation locationDetails) {
        return projectRepository.findById(id).map(project -> {
            com.krs.backend.models.ProjectLocation target = null;
            if (project.getLocations() != null) {
                for (com.krs.backend.models.ProjectLocation loc : project.getLocations()) {
                    if (loc.getId() != null && loc.getId().equals(locationId)) {
                        target = loc;
                        break;
                    }
                }
            }
            if (target != null) {
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
            return ResponseEntity.notFound().<com.krs.backend.models.ProjectLocation>build();
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
