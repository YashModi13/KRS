package com.krs.backend.repositories;

import com.krs.backend.models.TodoGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TodoGoalRepository extends JpaRepository<TodoGoal, Long> {
    
    @Query("SELECT t FROM TodoGoal t WHERE t.isActive = true AND (CAST(:targetDate AS date) IS NULL OR t.targetDate = :targetDate) ORDER BY t.isDone ASC, t.priority DESC, t.targetDate ASC")
    List<TodoGoal> findActiveTodos(@Param("targetDate") LocalDate targetDate);
}
