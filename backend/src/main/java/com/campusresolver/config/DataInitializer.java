package com.campusresolver.config;

import com.campusresolver.model.Department;
import com.campusresolver.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with default departments on application startup.
 * Runs once when the application starts.
 *
 * This ensures the 5 categories (wifi, hostel, transport, maintenance, other)
 * always have a corresponding department in the database so AI can auto-assign.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public void run(String... args) throws Exception {
        if (departmentRepository.count() == 0) {
            logger.info("Seeding default departments...");
            seedDepartments();
            logger.info("Departments seeded successfully.");
        } else {
            logger.info("Departments already exist, skipping seed.");
        }
    }

    private void seedDepartments() {
        departmentRepository.save(Department.builder()
                .name("IT Department")
                .description("Handles all WiFi, network, and internet connectivity issues")
                .contactEmail("it@college.edu")
                .headName("Dr. Arun Kumar")
                .categoryHandled("wifi")
                .build());

        departmentRepository.save(Department.builder()
                .name("Hostel Office")
                .description("Manages hostel facilities, rooms, and residential complaints")
                .contactEmail("hostel@college.edu")
                .headName("Mr. Suresh Patel")
                .categoryHandled("hostel")
                .build());

        departmentRepository.save(Department.builder()
                .name("Transport Office")
                .description("Handles bus, shuttle, and campus transport issues")
                .contactEmail("transport@college.edu")
                .headName("Ms. Priya Nair")
                .categoryHandled("transport")
                .build());

        departmentRepository.save(Department.builder()
                .name("Maintenance Department")
                .description("Handles infrastructure, electrical, plumbing, and repair issues")
                .contactEmail("maintenance@college.edu")
                .headName("Mr. Rajesh Singh")
                .categoryHandled("maintenance")
                .build());

        departmentRepository.save(Department.builder()
                .name("Admin Office")
                .description("Handles general complaints and issues not covered by other departments")
                .contactEmail("admin@college.edu")
                .headName("Dr. Meera Iyer")
                .categoryHandled("other")
                .build());
    }
}
