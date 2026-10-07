package com.krs.backend.services;

import com.krs.backend.models.DepartmentMaster;
import com.krs.backend.repositories.DepartmentMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentMasterService {

    private final DepartmentMasterRepository departmentMasterRepository;

    /**
     * Case-insensitive lookup and auto-creation of Department master entry in Title Case.
     */
    @Transactional
    public DepartmentMaster getOrCreateDepartment(String rawName) {
        return getOrCreateDepartmentInternal(rawName, null, null);
    }

    @Transactional
    public DepartmentMaster getOrCreateDepartment(String rawName, String rawCityVillage, String rawState) {
        return getOrCreateDepartmentInternal(rawName, rawCityVillage, rawState);
    }

    private DepartmentMaster getOrCreateDepartmentInternal(String rawName, String rawCityVillage, String rawState) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return null;
        }
        String titleCaseName = toTitleCase(rawName);
        Optional<DepartmentMaster> existing = departmentMasterRepository.findByNameIgnoreCase(titleCaseName);
        if (existing.isPresent()) {
            return updateExistingDepartmentIfNeeded(existing.get(), rawCityVillage);
        }

        return createNewDepartment(titleCaseName, rawCityVillage, rawState);
    }

    private DepartmentMaster updateExistingDepartmentIfNeeded(DepartmentMaster deptRecord, String rawCityVillage) {
        boolean updated = false;
        if (deptRecord.getState() == null || deptRecord.getState().trim().isEmpty()) {
            deptRecord.setState("Gujarat");
            updated = true;
        }
        if (rawCityVillage != null && !rawCityVillage.trim().isEmpty() && (deptRecord.getCityVillage() == null || deptRecord.getCityVillage().trim().isEmpty())) {
            deptRecord.setCityVillage(toTitleCase(rawCityVillage));
            updated = true;
        }
        if (updated) {
            return departmentMasterRepository.save(deptRecord);
        }
        return deptRecord;
    }

    private DepartmentMaster createNewDepartment(String titleCaseName, String rawCityVillage, String rawState) {
        String cityVillage = toTitleCase(rawCityVillage);
        if ((cityVillage == null || cityVillage.isEmpty()) && titleCaseName.contains(",")) {
            String[] parts = titleCaseName.split(",");
            if (parts.length > 1) {
                cityVillage = toTitleCase(parts[parts.length - 1]);
            }
        }

        String state = (rawState != null && !rawState.trim().isEmpty()) ? toTitleCase(rawState) : "Gujarat";
        ZoneId zone = ZoneId.systemDefault();

        DepartmentMaster newRecord = DepartmentMaster.builder()
                .name(titleCaseName)
                .cityVillage(cityVillage)
                .state(state)
                .createdAt(LocalDateTime.now(zone))
                .updatedAt(LocalDateTime.now(zone))
                .build();
        return departmentMasterRepository.save(newRecord);
    }

    public Optional<DepartmentMaster> findById(Long id) {
        if (id == null) return Optional.empty();
        return departmentMasterRepository.findById(id);
    }

    public List<DepartmentMaster> getAllDepartmentMasters() {
        return departmentMasterRepository.findAllByOrderByNameAsc();
    }

    public org.springframework.data.domain.Page<DepartmentMaster> getPaginated(String search, org.springframework.data.domain.Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return departmentMasterRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        }
        return departmentMasterRepository.findAll(pageable);
    }

    @Transactional
    public DepartmentMaster updateDepartment(Long id, String rawName, String rawCityVillage, String rawState) {
        DepartmentMaster deptRecord = departmentMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department Master not found with id: " + id));
        if (rawName != null && !rawName.trim().isEmpty()) {
            deptRecord.setName(toTitleCase(rawName));
        }
        if (rawCityVillage != null) {
            deptRecord.setCityVillage(toTitleCase(rawCityVillage));
        }
        if (rawState != null && !rawState.trim().isEmpty()) {
            deptRecord.setState(toTitleCase(rawState));
        }
        deptRecord.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        return departmentMasterRepository.save(deptRecord);
    }

    public static String toTitleCase(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text != null ? text.trim() : null;
        }
        String[] words = text.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    sb.append(word.substring(1).toLowerCase());
                }
            }
            if (i < words.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
