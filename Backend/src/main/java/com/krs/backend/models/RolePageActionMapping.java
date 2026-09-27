package com.krs.backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "role_page_action_mapping", schema = "krs_schema")
public class RolePageActionMapping extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_action_id", nullable = false)
    private PageActionMapping pageAction;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public PageActionMapping getPageAction() { return pageAction; }
    public void setPageAction(PageActionMapping pageAction) { this.pageAction = pageAction; }
}
