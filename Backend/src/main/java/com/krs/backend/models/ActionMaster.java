package com.krs.backend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "action_master", schema = "krs_schema")
public class ActionMaster extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action_name", nullable = false)
    private String actionName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }
}
