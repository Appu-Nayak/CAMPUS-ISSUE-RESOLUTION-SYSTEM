package com.campusresolver.service;

import com.campusresolver.ai.AIService;
import com.campusresolver.dto.*;
import com.campusresolver.model.*;
import com.campusresolver.repository.ComplaintRepository;
import com.campusresolver.repository.DepartmentRepository;
import com.campusresolver.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Core business logic for complaint management.
 *
 * Responsibilities:
 *  1. Submit new complaints (triggers AI classification)
 *  2. Retrieve complaints for students and admins
 *  3. Update complaint status (admin action)
 *  4. Build dashboard statistics
 *  5. Map between entities and DTOs
 */
@Service
public class ComplaintService {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintService.class);

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AIService aiService;

    // ──────────────────────────────────────────────────────
    // STUDENT OPERATIONS
    // ──────────────────────────────────────────────────────

    /**
     * Submits a new complaint.
     * This is the core workflow:
     *  1. Validate student exists
     *  2. Call AI to classify the complaint
     *  3. Auto-assign the correct department
     *  4. Save the complaint to the database
     *
     * @param request Contains title, description, studentId
     * @return ComplaintResponse with full details including AI classification
     */
    @Transactional
    public ComplaintResponse submitComplaint(ComplaintRequest request) {
        logger.info("Submitting complaint for student USN/Roll: {}", request.getRollNumber());

        // Step 1: Find student by roll number or create new
        Student student = studentRepository.findByRollNumber(request.getRollNumber())
                .orElseGet(() -> {
                    Student newStudent = new Student();
                    newStudent.setName(request.getStudentName());
                    newStudent.setRollNumber(request.getRollNumber());
                    newStudent.setEmail(request.getRollNumber().toLowerCase().replaceAll("\\s+", "") + "@campus.edu");
                    newStudent.setStudentDepartment("General");
                    return studentRepository.save(newStudent);
                });

        // Step 2: Call AI to classify the complaint
        logger.info("Calling AI to classify complaint...");
        AIClassificationResult aiResult = aiService.classifyComplaint(
                request.getTitle(),
                request.getDescription()
        );
        logger.info("AI classified: category={}, priority={}", aiResult.getCategory(), aiResult.getPriority());

        // Step 3: Find the matching department based on AI category
        Department assignedDepartment = departmentRepository
                .findByCategoryHandled(aiResult.getCategory())
                .orElseGet(() -> {
                    // Fallback to "other" department if exact category not found
                    logger.warn("No department found for category '{}', using fallback", aiResult.getCategory());
                    return departmentRepository.findByCategoryHandled("other").orElse(null);
                });

        // Step 4: Build and save the Complaint entity
        Priority priority = Priority.valueOf(aiResult.getPriority().toUpperCase());

        Complaint complaint = Complaint.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .student(student)
                .aiCategory(aiResult.getCategory())
                .priority(priority)
                .aiReasoning(aiResult.getReasoning())
                .assignedDepartment(assignedDepartment)
                .status(ComplaintStatus.PENDING)
                .build();

        Complaint savedComplaint = complaintRepository.save(complaint);
        logger.info("Complaint saved with ID: {}", savedComplaint.getId());

        return mapToResponse(savedComplaint);
    }

    /**
     * Returns all complaints for a specific student.
     * Used by the student dashboard to track their submissions.
     */
    public List<ComplaintResponse> getComplaintsByStudentRollNumber(String rollNumber) {
        // Validate student exists
        Student student = studentRepository.findByRollNumber(rollNumber)
                .orElseThrow(() -> new RuntimeException("Student not found with Roll Number: " + rollNumber));
                
        return complaintRepository.findByStudentId(student.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns a single complaint by ID.
     */
    public ComplaintResponse getComplaintById(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + complaintId));
        return mapToResponse(complaint);
    }

    // ──────────────────────────────────────────────────────
    // ADMIN OPERATIONS
    // ──────────────────────────────────────────────────────

    /**
     * Returns ALL complaints (admin only).
     * Sorted by creation date, newest first.
     */
    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAllWithStudentDetails()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns complaints filtered by status.
     */
    public List<ComplaintResponse> getComplaintsByStatus(ComplaintStatus status) {
        return complaintRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns all URGENT + PENDING complaints for priority handling.
     */
    public List<ComplaintResponse> getUrgentPendingComplaints() {
        return complaintRepository.findByPriorityAndStatus(Priority.URGENT, ComplaintStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Admin updates the status of a complaint.
     * Can also manually reassign the department.
     */
    @Transactional
    public ComplaintResponse updateComplaintStatus(Long complaintId, StatusUpdateRequest request) {
        logger.info("Admin updating complaint ID: {} to status: {}", complaintId, request.getStatus());

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found with ID: " + complaintId));

        complaint.setStatus(request.getStatus());

        if (request.getAdminNotes() != null && !request.getAdminNotes().isBlank()) {
            complaint.setAdminNotes(request.getAdminNotes());
        }

        // Optionally reassign department
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with ID: " + request.getDepartmentId()));
            complaint.setAssignedDepartment(dept);
        }

        Complaint updated = complaintRepository.save(complaint);
        return mapToResponse(updated);
    }

    /**
     * Searches complaints by keyword.
     */
    public List<ComplaintResponse> searchComplaints(String keyword) {
        return complaintRepository.searchByKeyword(keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Builds aggregated statistics for the admin dashboard.
     */
    public DashboardStats getDashboardStats() {
        long total      = complaintRepository.count();
        long pending    = complaintRepository.countByStatus(ComplaintStatus.PENDING);
        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long resolved   = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long urgent     = complaintRepository.findByPriority(Priority.URGENT).size();

        // Category breakdown
        Map<String, Long> byCategory = new HashMap<>();
        for (String cat : new String[]{"wifi", "hostel", "transport", "maintenance", "other"}) {
            byCategory.put(cat, complaintRepository.countByAiCategory(cat));
        }

        // Status breakdown
        Map<String, Long> byStatus = new HashMap<>();
        for (ComplaintStatus s : ComplaintStatus.values()) {
            byStatus.put(s.name(), complaintRepository.countByStatus(s));
        }

        return DashboardStats.builder()
                .totalComplaints(total)
                .pendingComplaints(pending)
                .inProgressComplaints(inProgress)
                .resolvedComplaints(resolved)
                .urgentComplaints(urgent)
                .complaintsByCategory(byCategory)
                .complaintsByStatus(byStatus)
                .build();
    }

    // ──────────────────────────────────────────────────────
    // MAPPER: Entity → Response DTO
    // ──────────────────────────────────────────────────────

    /**
     * Converts a Complaint entity to ComplaintResponse DTO.
     * Flattens nested relationships to avoid exposing full entity graphs.
     */
    private ComplaintResponse mapToResponse(Complaint c) {
        ComplaintResponse response = new ComplaintResponse();
        response.setId(c.getId());
        response.setTitle(c.getTitle());
        response.setDescription(c.getDescription());
        response.setAiCategory(c.getAiCategory());
        response.setPriority(c.getPriority());
        response.setAiReasoning(c.getAiReasoning());
        response.setStatus(c.getStatus());
        response.setAdminNotes(c.getAdminNotes());
        response.setCreatedAt(c.getCreatedAt());
        response.setUpdatedAt(c.getUpdatedAt());
        response.setResolvedAt(c.getResolvedAt());

        // Flatten student
        if (c.getStudent() != null) {
            response.setStudentId(c.getStudent().getId());
            response.setStudentName(c.getStudent().getName());
            response.setStudentRollNumber(c.getStudent().getRollNumber());
        }

        // Flatten department
        if (c.getAssignedDepartment() != null) {
            response.setDepartmentId(c.getAssignedDepartment().getId());
            response.setDepartmentName(c.getAssignedDepartment().getName());
            response.setDepartmentContact(c.getAssignedDepartment().getContactEmail());
        }

        return response;
    }
}
