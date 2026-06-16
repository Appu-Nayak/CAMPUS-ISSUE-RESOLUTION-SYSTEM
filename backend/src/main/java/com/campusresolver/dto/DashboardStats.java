package com.campusresolver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Aggregated statistics for the admin dashboard.
 * Returned by GET /api/admin/dashboard
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {

    // Totals
    private long totalComplaints;
    private long pendingComplaints;
    private long inProgressComplaints;
    private long resolvedComplaints;
    private long urgentComplaints;

    // Breakdown by category (e.g. { "wifi": 12, "hostel": 8 })
    private Map<String, Long> complaintsByCategory;

    // Breakdown by status
    private Map<String, Long> complaintsByStatus;

    // Breakdown by department
    private Map<String, Long> complaintsByDepartment;
}
