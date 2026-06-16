package com.campusresolver.controller;

import com.campusresolver.dto.ComplaintRequest;
import com.campusresolver.dto.ComplaintResponse;
import com.campusresolver.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for student-facing complaint operations.
 *
 * Base URL: /api/complaints
 *
 * Endpoints:
 *  POST   /api/complaints              → Submit new complaint (triggers AI)
 *  GET    /api/complaints/{id}         → Get single complaint by ID
 *  GET    /api/complaints/student/{id} → Get all complaints by a student
 */
@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*")   // Allow all origins for development
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    /**
     * Submit a new complaint.
     * The AI classification happens automatically inside this endpoint.
     *
     * Request Body:
     * {
     *   "title": "WiFi not working in Block C",
     *   "description": "The internet has been down for 2 days in hostel block C...",
     *   "studentId": 1
     * }
     *
     * Response: Full complaint with AI category and assigned department
     */
    @PostMapping
    public ResponseEntity<?> submitComplaint(@Valid @RequestBody ComplaintRequest request) {
        try {
            ComplaintResponse response = complaintService.submitComplaint(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse("Submission failed: " + e.getMessage())
            );
        }
    }

    /**
     * Get a single complaint by ID.
     * Students use this to check the status of their complaint.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getComplaintById(@PathVariable Long id) {
        try {
            ComplaintResponse response = complaintService.getComplaintById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all complaints submitted by a specific student.
     * Used for the student's "My Complaints" view.
     */
    @GetMapping("/student/{rollNumber}")
    public ResponseEntity<?> getComplaintsByStudent(@PathVariable String rollNumber) {
        try {
            List<ComplaintResponse> complaints = complaintService.getComplaintsByStudentRollNumber(rollNumber);
            return ResponseEntity.ok(complaints);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                new ErrorResponse(e.getMessage())
            );
        }
    }

    // Simple error response wrapper
    record ErrorResponse(String message) {}
}
