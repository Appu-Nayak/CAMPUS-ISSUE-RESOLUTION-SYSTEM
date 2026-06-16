package com.campusresolver.model;

/**
 * Lifecycle states of a complaint.
 *
 * Flow:
 *  PENDING → IN_PROGRESS → RESOLVED
 *                        → CLOSED
 *         → REJECTED
 */
public enum ComplaintStatus {
    PENDING,        // Newly submitted, awaiting review
    IN_PROGRESS,    // Assigned and being worked on
    RESOLVED,       // Issue has been fixed
    CLOSED,         // Closed without full resolution
    REJECTED        // Invalid or duplicate complaint
}
