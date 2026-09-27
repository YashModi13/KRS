package com.krs.backend.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "tenders", schema = "krs_schema")
@Data
public class Tender {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tenderNo;
    private String name;
    private String description;
    private BigDecimal tenderAmount;
    private BigDecimal agreementAmount;
    private String status;

    @OneToMany(mappedBy = "tender")
    private List<Construction> constructions;
}
