package com.krs.backend.models;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "roles", schema = "krs_schema")
@Data
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
