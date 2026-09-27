package com.krs.backend.dto;

public class PermissionDTO {
    private String pageName;
    private String routeUrl;
    private String actionName;
    private Boolean isAllowed;

    public PermissionDTO(String pageName, String routeUrl, String actionName, Boolean isAllowed) {
        this.pageName = pageName;
        this.routeUrl = routeUrl;
        this.actionName = actionName;
        this.isAllowed = isAllowed;
    }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getRouteUrl() { return routeUrl; }
    public void setRouteUrl(String routeUrl) { this.routeUrl = routeUrl; }

    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }

    public Boolean getIsAllowed() { return isAllowed; }
    public void setIsAllowed(Boolean isAllowed) { this.isAllowed = isAllowed; }
}
