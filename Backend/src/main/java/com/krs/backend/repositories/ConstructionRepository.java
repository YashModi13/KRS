package com.krs.backend.repositories;
import com.krs.backend.models.Construction;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ConstructionRepository extends JpaRepository<Construction, Long> {}
