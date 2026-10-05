package com.krs.backend.services;

import com.krs.backend.models.RefPersonMaster;
import com.krs.backend.repositories.RefPersonMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefPersonMasterService {

    private final RefPersonMasterRepository refPersonMasterRepository;

    /**
     * Case-insensitive lookup and auto-creation of RefPerson master entry in Title Case.
     */
    @Transactional
    public RefPersonMaster getOrCreateRefPerson(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return null;
        }
        String titleCaseName = toTitleCase(rawName);
        Optional<RefPersonMaster> existing = refPersonMasterRepository.findByNameIgnoreCase(titleCaseName);
        if (existing.isPresent()) {
            return existing.get();
        }
        RefPersonMaster newRecord = RefPersonMaster.builder()
                .name(titleCaseName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return refPersonMasterRepository.save(newRecord);
    }

    public Optional<RefPersonMaster> findById(Long id) {
        if (id == null) return Optional.empty();
        return refPersonMasterRepository.findById(id);
    }

    public List<RefPersonMaster> getAllRefPersonMasters() {
        return refPersonMasterRepository.findAllByOrderByNameAsc();
    }

    public org.springframework.data.domain.Page<RefPersonMaster> getPaginated(String search, org.springframework.data.domain.Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return refPersonMasterRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        }
        return refPersonMasterRepository.findAll(pageable);
    }

    @Transactional
    public RefPersonMaster updateRefPerson(Long id, String rawName) {
        RefPersonMaster record = refPersonMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ref Person Master not found with id: " + id));
        if (rawName != null && !rawName.trim().isEmpty()) {
            record.setName(toTitleCase(rawName));
        }
        record.setUpdatedAt(LocalDateTime.now());
        return refPersonMasterRepository.save(record);
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
