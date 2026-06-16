package com.campusresolver.dto;

import com.campusresolver.model.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for admin to update complaint status.
 * Sent via PUT /api/admin/complaints/{id}/status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ComplaintStatus status;

    private String adminNotes;

    // Optional: manually override department assignment
    private Long departmentId;
}
