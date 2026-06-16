package com.campusresolver.repository;

import com.campusresolver.model.Complaint;
import com.campusresolver.model.ComplaintStatus;
import com.campusresolver.model.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access layer for Complaint entity.
 * Spring Data JPA auto-implements all standard CRUD methods.
 */
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Find all complaints submitted by a specific student
    List<Complaint> findByStudentId(Long studentId);

    // Find complaints by status (e.g., all PENDING complaints)
    List<Complaint> findByStatus(ComplaintStatus status);

    // Find complaints by AI-assigned category
    List<Complaint> findByAiCategory(String aiCategory);

    // Find complaints by priority
    List<Complaint> findByPriority(Priority priority);

    // Find complaints assigned to a specific department
    List<Complaint> findByAssignedDepartmentId(Long departmentId);

    // Find urgent + pending complaints (for admin dashboard priority view)
    List<Complaint> findByPriorityAndStatus(Priority priority, ComplaintStatus status);

    // Count complaints by status (for dashboard stats)
    long countByStatus(ComplaintStatus status);

    // Count complaints by category (for analytics)
    long countByAiCategory(String aiCategory);

    // Custom query: Get complaints with student name (for admin view)
    @Query("SELECT c FROM Complaint c JOIN FETCH c.student s ORDER BY c.createdAt DESC")
    List<Complaint> findAllWithStudentDetails();

    // Search complaints by keyword in title or description
    @Query("SELECT c FROM Complaint c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Complaint> searchByKeyword(@Param("keyword") String keyword);
}
