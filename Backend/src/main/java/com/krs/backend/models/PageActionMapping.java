package com.krs.backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "page_action_mapping", schema = "krs_schema")
public class PageActionMapping extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", nullable = false)
    private PageMaster page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    private ActionMaster action;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PageMaster getPage() { return page; }
    public void setPage(PageMaster page) { this.page = page; }

    public ActionMaster getAction() { return action; }
    public void setAction(ActionMaster action) { this.action = action; }
}
