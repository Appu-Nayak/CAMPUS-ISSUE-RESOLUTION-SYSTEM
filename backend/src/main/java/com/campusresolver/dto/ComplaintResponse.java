package com.campusresolver.dto;

import com.campusresolver.model.ComplaintStatus;
import com.campusresolver.model.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response object returned to clients after complaint operations.
 * Flattens nested entities for clean API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResponse {

    private Long id;
    private String title;
    private String description;

    // Student info (flattened)
    private Long studentId;
    private String studentName;
    private String studentRollNumber;

    // AI Classification
    private String aiCategory;
    private Priority priority;
    private String aiReasoning;

    // Status
    private ComplaintStatus status;
    private String adminNotes;

    // Department info (flattened)
    private Long departmentId;
    private String departmentName;
    private String departmentContact;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
