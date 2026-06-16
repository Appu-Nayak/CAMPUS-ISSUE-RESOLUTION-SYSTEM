package com.campusresolver.controller;

import com.campusresolver.model.Department;
import com.campusresolver.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for department management.
 *
 * Base URL: /api/departments
 *
 * Endpoints:
 *  POST /api/departments       → Create department (admin)
 *  GET  /api/departments       → List all departments
 *  GET  /api/departments/{id}  → Get by ID
 */
@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * Create a new department.
     *
     * Request Body:
     * {
     *   "name": "IT Department",
     *   "description": "Handles all wifi and network issues",
     *   "contactEmail": "it@college.edu",
     *   "headName": "Dr. Arun Kumar",
     *   "categoryHandled": "wifi"
     * }
     */
    @PostMapping
    public ResponseEntity<?> createDepartment(@Valid @RequestBody Department department) {
        try {
            Department saved = departmentService.createDepartment(department);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Failed: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDepartmentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(departmentService.getDepartmentById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
