package com.krs.backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "page_master", schema = "krs_schema")
public class PageMaster extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_name", nullable = false)
    private String pageName;

    @Column(name = "route_url")
    private String routeUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPageName() { return pageName; }
    public void setPageName(String pageName) { this.pageName = pageName; }

    public String getRouteUrl() { return routeUrl; }
    public void setRouteUrl(String routeUrl) { this.routeUrl = routeUrl; }
}
