package com.campusresolver.model;

/**
 * AI-detected priority level of a complaint.
 * URGENT complaints should be addressed within 24 hours.
 * NORMAL complaints within the standard SLA of 3-5 days.
 */
public enum Priority {
    URGENT,
    NORMAL
}
