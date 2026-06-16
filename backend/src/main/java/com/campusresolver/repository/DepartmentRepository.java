package com.campusresolver.repository;

import com.campusresolver.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access layer for Department entity.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // Find department by the category it handles (used by AI assignment logic)
    Optional<Department> findByCategoryHandled(String categoryHandled);

    Optional<Department> findByName(String name);

    boolean existsByCategoryHandled(String categoryHandled);
}
