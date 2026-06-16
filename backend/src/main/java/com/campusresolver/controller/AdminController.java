package com.campusresolver.controller;

import com.campusresolver.dto.ComplaintResponse;
import com.campusresolver.dto.DashboardStats;
import com.campusresolver.dto.StatusUpdateRequest;
import com.campusresolver.model.ComplaintStatus;
import com.campusresolver.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for admin operations.
 *
 * Base URL: /api/admin
 *
 * Endpoints:
 *  GET  /api/admin/dashboard              → Dashboard statistics
 *  GET  /api/admin/complaints             → All complaints
 *  GET  /api/admin/complaints/status/{s}  → Filter by status
 *  GET  /api/admin/complaints/urgent      → Urgent + pending
 *  GET  /api/admin/complaints/search      → Search by keyword
 *  PUT  /api/admin/complaints/{id}/status → Update complaint status
 *
 * NOTE: In production, secure all /api/admin/** routes with Spring Security.
 *       A simple Basic Auth is configured in SecurityConfig.java.
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private ComplaintService complaintService;

    /**
     * Admin dashboard: returns aggregated statistics.
     *
     * Example Response:
     * {
     *   "totalComplaints": 42,
     *   "pendingComplaints": 15,
     *   "urgentComplaints": 7,
     *   "complaintsByCategory": { "wifi": 12, "hostel": 8, ... }
     * }
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStats> getDashboard() {
        return ResponseEntity.ok(complaintService.getDashboardStats());
    }

    /**
     * Get all complaints (newest first).
     */
    @GetMapping("/complaints")
    public ResponseEntity<List<ComplaintResponse>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    /**
     * Filter complaints by status.
     * Example: GET /api/admin/complaints/status/PENDING
     */
    @GetMapping("/complaints/status/{status}")
    public ResponseEntity<?> getComplaintsByStatus(@PathVariable String status) {
        try {
            ComplaintStatus complaintStatus = ComplaintStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(complaintService.getComplaintsByStatus(complaintStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status +
                    ". Valid values: PENDING, IN_PROGRESS, RESOLVED, CLOSED, REJECTED");
        }
    }

    /**
     * Get all URGENT + PENDING complaints.
     * Admins should check this first thing every day.
     */
    @GetMapping("/complaints/urgent")
    public ResponseEntity<List<ComplaintResponse>> getUrgentComplaints() {
        return ResponseEntity.ok(complaintService.getUrgentPendingComplaints());
    }

    /**
     * Search complaints by keyword in title or description.
     * Example: GET /api/admin/complaints/search?keyword=wifi
     */
    @GetMapping("/complaints/search")
    public ResponseEntity<List<ComplaintResponse>> searchComplaints(@RequestParam String keyword) {
        return ResponseEntity.ok(complaintService.searchComplaints(keyword));
    }

    /**
     * Update the status of a complaint and optionally reassign department.
     *
     * Request Body:
     * {
     *   "status": "IN_PROGRESS",
     *   "adminNotes": "Team has been notified, fix expected by Friday",
     *   "departmentId": 2   (optional: reassign to different department)
     * }
     */
    @PutMapping("/complaints/{id}/status")
    public ResponseEntity<?> updateComplaintStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        try {
            ComplaintResponse updated = complaintService.updateComplaintStatus(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Update failed: " + e.getMessage());
        }
    }
}
