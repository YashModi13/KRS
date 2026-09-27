package com.krs.backend.repositories;
import com.krs.backend.models.Tender;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TenderRepository extends JpaRepository<Tender, Long> {}
