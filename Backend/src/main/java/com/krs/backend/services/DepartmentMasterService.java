package com.krs.backend.services;

import com.krs.backend.models.DepartmentMaster;
import com.krs.backend.repositories.DepartmentMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        return getOrCreateDepartment(rawName, null, null);
    }

    @Transactional
    public DepartmentMaster getOrCreateDepartment(String rawName, String rawCityVillage, String rawState) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return null;
        }
        String titleCaseName = toTitleCase(rawName);
        Optional<DepartmentMaster> existing = departmentMasterRepository.findByNameIgnoreCase(titleCaseName);
        if (existing.isPresent()) {
            DepartmentMaster record = existing.get();
            boolean updated = false;
            if ((record.getState() == null || record.getState().trim().isEmpty())) {
                record.setState("Gujarat");
                updated = true;
            }
            if (rawCityVillage != null && !rawCityVillage.trim().isEmpty() && (record.getCityVillage() == null || record.getCityVillage().trim().isEmpty())) {
                record.setCityVillage(toTitleCase(rawCityVillage));
                updated = true;
            }
            if (updated) {
                return departmentMasterRepository.save(record);
            }
            return record;
        }

        // Extract city/village from name if comma present and rawCityVillage not provided
        String cityVillage = toTitleCase(rawCityVillage);
        if ((cityVillage == null || cityVillage.isEmpty()) && titleCaseName.contains(",")) {
            String[] parts = titleCaseName.split(",");
            if (parts.length > 1) {
                cityVillage = toTitleCase(parts[parts.length - 1]);
            }
        }

        String state = (rawState != null && !rawState.trim().isEmpty()) ? toTitleCase(rawState) : "Gujarat";

        DepartmentMaster newRecord = DepartmentMaster.builder()
                .name(titleCaseName)
                .cityVillage(cityVillage)
                .state(state)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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
        DepartmentMaster record = departmentMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department Master not found with id: " + id));
        if (rawName != null && !rawName.trim().isEmpty()) {
            record.setName(toTitleCase(rawName));
        }
        if (rawCityVillage != null) {
            record.setCityVillage(toTitleCase(rawCityVillage));
        }
        if (rawState != null && !rawState.trim().isEmpty()) {
            record.setState(toTitleCase(rawState));
        }
        record.setUpdatedAt(LocalDateTime.now());
        return departmentMasterRepository.save(record);
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
