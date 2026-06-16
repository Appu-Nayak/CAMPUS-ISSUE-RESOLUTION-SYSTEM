package com.campusresolver.controller;

import com.campusresolver.model.Student;
import com.campusresolver.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for student registration and lookup.
 *
 * Base URL: /api/students
 *
 * Endpoints:
 *  POST /api/students          → Register new student
 *  GET  /api/students          → Get all students (admin)
 *  GET  /api/students/{id}     → Get student by ID
 *  GET  /api/students/email/{email} → Get by email
 */
@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentService studentService;

    /**
     * Register a new student.
     *
     * Request Body:
     * {
     *   "name": "Rahul Sharma",
     *   "email": "rahul@college.edu",
     *   "rollNumber": "CS21B001",
     *   "studentDepartment": "Computer Science",
     *   "phoneNumber": "9876543210"
     * }
     */
    @PostMapping
    public ResponseEntity<?> registerStudent(@Valid @RequestBody Student student) {
        try {
            Student saved = studentService.registerStudent(student);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Registration failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(studentService.getStudentById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getStudentByEmail(@PathVariable String email) {
        try {
            return ResponseEntity.ok(studentService.getStudentByEmail(email));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }
}
