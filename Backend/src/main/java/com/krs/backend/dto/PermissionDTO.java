package com.krs.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDTO {
    private String pageName;
    private String routeUrl;
    private String actionName;
    private Boolean isAllowed;
}
