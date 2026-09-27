package com.krs.backend.models;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "constructions", schema = "krs_schema")
@Data
public class Construction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "tender_id")
    @JsonIgnore
    private Tender tender;
    
    private String type;
    private String name;
    private String village;
    private String taluka;
    private String district;
    private BigDecimal physicalProgress;
    private BigDecimal financialProgress;
    private String status;
}
