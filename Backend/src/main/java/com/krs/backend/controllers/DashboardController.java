package com.krs.backend.controllers;

import com.krs.backend.models.DailyTask;
import com.krs.backend.repositories.DailyTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class DashboardController {

    @Autowired
    private DailyTaskRepository dailyTaskRepository;

    @GetMapping
    public List<DailyTask> getAllTasks() {
        return dailyTaskRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<DailyTask> createTask(@RequestBody DailyTask task) {
        DailyTask savedTask = dailyTaskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DailyTask> updateTaskStatus(@PathVariable Long id, @RequestParam Boolean status) {
        return dailyTaskRepository.findById(id).map(task -> {
            task.setStatus(status);
            return ResponseEntity.ok(dailyTaskRepository.save(task));
        }).orElse(ResponseEntity.notFound().build());
    }
}
