package com.campusresolver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Core entity representing a student complaint.
 *
 * AI Classification Flow:
 *  1. Student submits complaint with title + description
 *  2. AIService sends description to OpenRouter
 *  3. AI returns { category, priority, suggestedDepartment }
 *  4. System sets aiCategory, priority, and assignedDepartment
 *  5. Status starts as PENDING
 */
@Entity
@Table(name = "complaints")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, message = "Description must be at least 10 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // ── AI-Classified Fields ───────────────────────────────
    /**
     * Category assigned by AI.
     * Possible values: wifi, hostel, transport, maintenance, other
     */
    @Column(name = "ai_category")
    private String aiCategory;

    /**
     * Priority assigned by AI: URGENT or NORMAL
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority;

    /**
     * Human-readable reason from AI for the classification
     */
    @Column(name = "ai_reasoning", columnDefinition = "TEXT")
    private String aiReasoning;

    // ── Status & Assignment ────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.PENDING;

    /**
     * Admin notes when updating complaint status
     */
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    // ── Timestamps ─────────────────────────────────────────
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // ── Relationships ──────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @ToString.Exclude
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @ToString.Exclude
    private Department assignedDepartment;

    // ── Lifecycle Hooks ────────────────────────────────────
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ComplaintStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.status == ComplaintStatus.RESOLVED && this.resolvedAt == null) {
            this.resolvedAt = LocalDateTime.now();
        }
    }
}
