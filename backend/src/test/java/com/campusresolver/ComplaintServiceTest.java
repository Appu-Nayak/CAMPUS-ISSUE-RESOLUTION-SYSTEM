package com.campusresolver;

import com.campusresolver.ai.AIService;
import com.campusresolver.dto.AIClassificationResult;
import com.campusresolver.dto.ComplaintRequest;
import com.campusresolver.dto.ComplaintResponse;
import com.campusresolver.model.*;
import com.campusresolver.repository.ComplaintRepository;
import com.campusresolver.repository.DepartmentRepository;
import com.campusresolver.repository.StudentRepository;
import com.campusresolver.service.ComplaintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ComplaintService.
 * Uses Mockito to mock dependencies (no real DB or AI calls).
 */
class ComplaintServiceTest {

    @Mock private ComplaintRepository complaintRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private AIService aiService;

    @InjectMocks
    private ComplaintService complaintService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void submitComplaint_ShouldClassifyAndAssignDepartment() {
        // Arrange: set up mock student
        Student mockStudent = Student.builder()
                .id(1L)
                .name("Rahul Sharma")
                .email("rahul@college.edu")
                .rollNumber("CS21B001")
                .build();

        // Set up mock department
        Department mockDept = Department.builder()
                .id(1L)
                .name("IT Department")
                .categoryHandled("wifi")
                .contactEmail("it@college.edu")
                .build();

        // Set up mock AI result
        AIClassificationResult mockAiResult = AIClassificationResult.builder()
                .category("wifi")
                .priority("urgent")
                .reasoning("Network outage affecting exams")
                .suggestedDepartment("IT Department")
                .build();

        // Set up mock saved complaint
        Complaint mockComplaint = Complaint.builder()
                .id(1L)
                .title("WiFi down")
                .description("Internet is not working")
                .student(mockStudent)
                .assignedDepartment(mockDept)
                .aiCategory("wifi")
                .priority(Priority.URGENT)
                .status(ComplaintStatus.PENDING)
                .build();

        // Mock all dependencies
        when(studentRepository.findByRollNumber("CS21B001")).thenReturn(Optional.of(mockStudent));
        when(aiService.classifyComplaint(anyString(), anyString())).thenReturn(mockAiResult);
        when(departmentRepository.findByCategoryHandled("wifi")).thenReturn(Optional.of(mockDept));
        when(complaintRepository.save(any(Complaint.class))).thenReturn(mockComplaint);

        // Act
        ComplaintRequest request = new ComplaintRequest("WiFi down", "Internet is not working", "Rahul Sharma", "CS21B001");
        ComplaintResponse response = complaintService.submitComplaint(request);

        // Assert
        assertNotNull(response);
        assertEquals("WiFi down", response.getTitle());
        assertEquals("wifi", response.getAiCategory());
        assertEquals(Priority.URGENT, response.getPriority());
        assertEquals("IT Department", response.getDepartmentName());
        assertEquals(ComplaintStatus.PENDING, response.getStatus());

        // Verify AI was called
        verify(aiService, times(1)).classifyComplaint("WiFi down", "Internet is not working");
        verify(complaintRepository, times(1)).save(any(Complaint.class));
    }

    @Test
    void submitComplaint_ShouldCreateStudentIfNotFound() {
        // Arrange
        when(studentRepository.findByRollNumber("CS21B002")).thenReturn(Optional.empty());
        
        Student mockNewStudent = Student.builder()
                .id(2L)
                .name("New Student")
                .email("cs21b002@campus.edu")
                .rollNumber("CS21B002")
                .build();
        when(studentRepository.save(any(Student.class))).thenReturn(mockNewStudent);

        Department mockDept = Department.builder()
                .id(1L)
                .name("IT Department")
                .categoryHandled("wifi")
                .contactEmail("it@college.edu")
                .build();
        when(departmentRepository.findByCategoryHandled("wifi")).thenReturn(Optional.of(mockDept));

        AIClassificationResult mockAiResult = AIClassificationResult.builder()
                .category("wifi")
                .priority("urgent")
                .reasoning("Network outage")
                .suggestedDepartment("IT Department")
                .build();
        when(aiService.classifyComplaint(anyString(), anyString())).thenReturn(mockAiResult);

        Complaint mockComplaint = Complaint.builder()
                .id(2L)
                .title("WiFi down")
                .description("Internet is not working")
                .student(mockNewStudent)
                .assignedDepartment(mockDept)
                .aiCategory("wifi")
                .priority(Priority.URGENT)
                .status(ComplaintStatus.PENDING)
                .build();
        when(complaintRepository.save(any(Complaint.class))).thenReturn(mockComplaint);

        // Act
        ComplaintRequest request = new ComplaintRequest("WiFi down", "Internet is not working", "New Student", "CS21B002");
        ComplaintResponse response = complaintService.submitComplaint(request);

        // Assert
        assertNotNull(response);
        verify(studentRepository, times(1)).save(any(Student.class));
    }
}
