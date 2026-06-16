package com.campusresolver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Holds the structured response returned by the AI classification service.
 *
 * Example AI JSON response parsed into this object:
 * {
 *   "category": "wifi",
 *   "priority": "urgent",
 *   "reasoning": "The complaint mentions network outage affecting exam preparation",
 *   "suggestedDepartment": "IT Department"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIClassificationResult {

    /**
     * One of: wifi, hostel, transport, maintenance, other
     */
    private String category;

    /**
     * One of: urgent, normal
     */
    private String priority;

    /**
     * AI's explanation for why it chose this category/priority
     */
    private String reasoning;

    /**
     * Suggested department name (maps to Department.categoryHandled)
     */
    private String suggestedDepartment;
}
