package com.krs.backend.controllers;

import com.krs.backend.models.TodoGoal;
import com.krs.backend.models.User;
import com.krs.backend.repositories.TodoGoalRepository;
import com.krs.backend.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/todos")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TodoGoalController {

    private final TodoGoalRepository todoGoalRepository;
    private final UserRepository userRepository;

    public TodoGoalController(TodoGoalRepository todoGoalRepository, UserRepository userRepository) {
        this.todoGoalRepository = todoGoalRepository;
        this.userRepository = userRepository;
    }
    
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findUserByUsername(username);
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<TodoGoal>> getActiveTodos(@RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        return ResponseEntity.ok(todoGoalRepository.findActiveTodos(date));
    }

    @PostMapping
    public ResponseEntity<TodoGoal> createTodo(@RequestBody TodoGoal todo) {
        User currentUser = getCurrentUser();
        todo.setCreatedBy(currentUser);
        todo.setUpdatedAt(LocalDateTime.now());
        todo.setUpdatedBy(currentUser);
        todo.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(todoGoalRepository.save(todo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoGoal> updateTodo(@PathVariable Long id, @RequestBody TodoGoal todoDetails) {
        return todoGoalRepository.findById(id).map(todo -> {
            // Partial update: only overwrite fields that were explicitly provided
            if (todoDetails.getTaskDescription() != null) {
                todo.setTaskDescription(todoDetails.getTaskDescription());
            }
            if (todoDetails.getPriority() != null) {
                todo.setPriority(todoDetails.getPriority());
            }
            if (todoDetails.getTargetDate() != null) {
                todo.setTargetDate(todoDetails.getTargetDate());
            }
            if (todoDetails.getIsDone() != null) {
                todo.setIsDone(todoDetails.getIsDone());
            }
            todo.setUpdatedAt(LocalDateTime.now());
            todo.setUpdatedBy(getCurrentUser());
            return ResponseEntity.ok(todoGoalRepository.save(todo));
        }).orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/toggle-done")
    public ResponseEntity<TodoGoal> toggleDone(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, String> payload) {
        return todoGoalRepository.findById(id).map(todo -> {
            todo.setIsDone(!todo.getIsDone());
            if (todo.getIsDone()) {
                todo.setDoneDate(LocalDateTime.now());
                todo.setDoneBy(getCurrentUser());
                if (payload != null && payload.containsKey("doneNote")) {
                    todo.setDoneNote(payload.get("doneNote"));
                }
            } else {
                todo.setDoneDate(null);
                todo.setDoneBy(null);
                todo.setDoneNote(null);
            }
            todo.setUpdatedAt(LocalDateTime.now());
            todo.setUpdatedBy(getCurrentUser());
            return ResponseEntity.ok(todoGoalRepository.save(todo));
        }).orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        return todoGoalRepository.findById(id).map(todo -> {
            todo.setIsActive(false);
            todo.setUpdatedAt(LocalDateTime.now());
            todo.setUpdatedBy(getCurrentUser());
            todoGoalRepository.save(todo);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
