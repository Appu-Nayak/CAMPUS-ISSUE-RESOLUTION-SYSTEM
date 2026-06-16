package com.campusresolver.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

/**
 * Represents a campus department that handles specific complaint categories.
 *
 * Example departments:
 *  - IT Department     → handles wifi complaints
 *  - Hostel Office     → handles hostel complaints
 *  - Transport Office  → handles transport complaints
 *  - Maintenance Dept  → handles maintenance complaints
 *  - Admin Office      → handles other complaints
 */
@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Department name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "head_name")
    private String headName;

    /**
     * Maps to the AI-classified category.
     * Values: wifi, hostel, transport, maintenance, other
     */
    @Column(name = "category_handled", nullable = false, unique = true)
    private String categoryHandled;

    // A department handles many complaints
    @OneToMany(mappedBy = "assignedDepartment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Complaint> complaints;
}
