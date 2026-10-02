package com.krs.backend.services;

import com.krs.backend.dto.PermissionDTO;
import com.krs.backend.models.*;
import com.krs.backend.repositories.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PermissionService {

    private final UserRepository userRepository;
    private final RolePageActionMappingRepository roleActionRepo;
    private final UserPageActionMappingRepository userActionRepo;

    public PermissionService(
            UserRepository userRepository,
            RolePageActionMappingRepository roleActionRepo,
            UserPageActionMappingRepository userActionRepo
    ) {
        this.userRepository = userRepository;
        this.roleActionRepo = roleActionRepo;
        this.userActionRepo = userActionRepo;
    }

    public List<PermissionDTO> getUserPermissions(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return new ArrayList<>();
        User user = userOpt.get();

        Map<Long, PermissionDTO> finalPermissions = new HashMap<>();

        // 1. Load Role-based permissions (defaults)
        for (Role role : user.getRoles()) {
            List<RolePageActionMapping> roleMappings = roleActionRepo.findByRoleId(role.getId());
            for (RolePageActionMapping rmap : roleMappings) {
                if (rmap.getIsActive() && rmap.getPageAction().getIsActive()) {
                    PageActionMapping pam = rmap.getPageAction();
                    PermissionDTO dto = new PermissionDTO(
                            pam.getPage().getPageName(),
                            pam.getPage().getRouteUrl(),
                            pam.getAction().getActionName(),
                            true // Role maps are true if present
                    );
                    finalPermissions.put(pam.getId(), dto);
                }
            }
        }

        // 2. Load User-based permissions (overrides)
        List<UserPageActionMapping> userMappings = userActionRepo.findByUserId(userId);
        for (UserPageActionMapping umap : userMappings) {
            if (umap.getIsActive() && umap.getPageAction().getIsActive()) {
                PageActionMapping pam = umap.getPageAction();
                PermissionDTO dto = new PermissionDTO(
                        pam.getPage().getPageName(),
                        pam.getPage().getRouteUrl(),
                        pam.getAction().getActionName(),
                        umap.getIsAllowed()
                );
                // This overrides the role permission mapping
                finalPermissions.put(pam.getId(), dto);
            }
        }

        return new ArrayList<>(finalPermissions.values());
    }
}
