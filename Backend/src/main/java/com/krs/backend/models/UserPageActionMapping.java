package com.krs.backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "user_page_action_mapping", schema = "krs_schema")
public class UserPageActionMapping extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_action_id", nullable = false)
    private PageActionMapping pageAction;

    @Column(name = "is_allowed", nullable = false)
    private Boolean isAllowed;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public PageActionMapping getPageAction() { return pageAction; }
    public void setPageAction(PageActionMapping pageAction) { this.pageAction = pageAction; }

    public Boolean getIsAllowed() { return isAllowed; }
    public void setIsAllowed(Boolean allowed) { isAllowed = allowed; }
}
