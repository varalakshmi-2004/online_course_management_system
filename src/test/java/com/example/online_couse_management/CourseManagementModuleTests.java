package com.example.online_couse_management;

import com.example.online_couse_management.dto.*;
import com.example.online_couse_management.entity.*;
import com.example.online_couse_management.service.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CourseManagementModuleTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private InstructorService instructorService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private NotificationService notificationService;

    private static final long timestamp = System.currentTimeMillis();
    private static final String instructorEmail = "ada." + timestamp + "@university.edu";
    private static final String studentEmail = "bob." + timestamp + "@university.edu";

    private static Long studentId;
    private static Long instructorId;
    private static Long courseId;
    private static Long enrollmentId;

    @Test
    @Order(1)
    public void testAuthenticationAndRoleModule() {
        // Test Instructor Registration
        RegisterRequest instructorReq = new RegisterRequest(
                "Prof. Ada Lovelace",
                instructorEmail,
                "SecurePass@123",
                "+1-111-222-3333",
                "ROLE_INSTRUCTOR",
                "Algorithms and Computing"
        );
        AuthResponse instAuth = authService.register(instructorReq);
        assertNotNull(instAuth.getToken(), "JWT Token should not be null for instructor");
        assertEquals("ROLE_INSTRUCTOR", instAuth.getRole());
        instructorId = instAuth.getProfileId();
        assertNotNull(instructorId);

        // Test Student Registration
        RegisterRequest studentReq = new RegisterRequest(
                "Bob Smith",
                studentEmail,
                "SecurePass@123",
                "+1-444-555-6666",
                "ROLE_STUDENT",
                null
        );
        AuthResponse studentAuth = authService.register(studentReq);
        assertNotNull(studentAuth.getToken(), "JWT Token should not be null for student");
        assertEquals("ROLE_STUDENT", studentAuth.getRole());
        studentId = studentAuth.getProfileId();
        assertNotNull(studentId);

        // Test Login for both
        AuthResponse loginRes = authService.login(new AuthRequest(studentEmail, "SecurePass@123"));
        assertNotNull(loginRes.getToken());
        assertEquals("ROLE_STUDENT", loginRes.getRole());
    }

    @Test
    @Order(2)
    public void testStudentDetailsUpdate() {
        assertNotNull(studentId);
        Student student = studentService.getStudentById(studentId);
        assertEquals("Bob Smith", student.getName());

        student.setName("Robert Smith");
        student.setContact("+1-999-888-7777");
        Student updated = studentService.updateStudent(studentId, student);

        assertEquals("Robert Smith", updated.getName());
        assertEquals("+1-999-888-7777", updated.getContact());
    }

    @Test
    @Order(3)
    public void testCourseModuleWithContentAssignment() {
        // Create course
        Course course = new Course(
                "Cloud Microservices Architecture " + timestamp,
                "Deep dive into Docker, Kubernetes, API Gateways, and Resilient Microservices.",
                instructorEmail,
                "Cloud Computing"
        );
        course = courseService.SaveCourse(course);
        courseId = course.getId();
        assertNotNull(courseId);

        // Assign Video content
        CourseContent video = courseService.assignContent(courseId, new CourseContentRequest(
                "Video: Microservices Design Patterns",
                ContentType.VIDEO,
                "https://cdn.test.com/videos/microservices.mp4",
                "Circuit breaker, saga, and CQRS patterns.",
                null,
                40,
                1
        ));
        assertNotNull(video.getId());
        assertEquals(ContentType.VIDEO, video.getContentType());

        // Assign PDF content
        CourseContent pdf = courseService.assignContent(courseId, new CourseContentRequest(
                "PDF: Kubernetes Deployment Cheat Sheet",
                ContentType.PDF,
                "https://cdn.test.com/docs/k8s.pdf",
                "Comprehensive cheat sheet for pods, services, and ingresses.",
                null,
                20,
                2
        ));
        assertNotNull(pdf.getId());
        assertEquals(ContentType.PDF, pdf.getContentType());

        // Assign Quiz content
        CourseContent quiz = courseService.assignContent(courseId, new CourseContentRequest(
                "Quiz: Microservices Assessment",
                ContentType.QUIZ,
                null,
                "10 question quiz testing resilient patterns.",
                "Q1: What is the circuit breaker state when errors are frequent? A) OPEN B) CLOSED",
                15,
                3
        ));
        assertNotNull(quiz.getId());
        assertEquals(ContentType.QUIZ, quiz.getContentType());

        // List assigned contents
        List<CourseContent> contents = courseService.getCourseContents(courseId);
        assertEquals(3, contents.size());

        // List available courses for students
        List<Course> cloudCourses = courseService.getCoursesByCategory("Cloud Computing");
        assertFalse(cloudCourses.isEmpty());
    }

    @Test
    @Order(4)
    public void testEnrollmentAndProgressTracking() {
        assertNotNull(studentId);
        assertNotNull(courseId);

        // Enroll student in course
        Enrollment enrollment = enrollmentService.enrollStudent(studentId, courseId);
        enrollmentId = enrollment.getId();
        assertNotNull(enrollmentId);
        assertEquals("ONGOING", enrollment.getStatus());
        assertEquals(0, enrollment.getProgressPercentage());
        assertNotNull(enrollment.getEnrollmentDate());

        // Verify history includes course
        List<Enrollment> history = enrollmentService.getStudentCourseHistory(studentId);
        assertTrue(history.stream().anyMatch(e -> e.getCourse().getId().equals(courseId)));

        // Update progress to 50%
        Enrollment updated = enrollmentService.updateProgress(enrollmentId, 50);
        assertEquals(50, updated.getProgressPercentage());
        assertEquals("ONGOING", updated.getStatus());

        // Track course progress via DTO
        StudentProgressDTO progressDTO = studentService.getStudentProgress(studentId);
        assertEquals("Robert Smith", progressDTO.getStudentName());
        assertTrue(progressDTO.getTotalEnrolledCourses() >= 1);
        assertTrue(progressDTO.getOngoingCoursesCount() >= 1);

        // Complete the course (progress to 100%)
        enrollmentService.submitQuizScore(enrollmentId, 95.0);
        Enrollment completed = enrollmentService.updateProgress(enrollmentId, 100);
        assertEquals("COMPLETED", completed.getStatus());
        assertNotNull(completed.getCompletionDate());
        assertEquals("A (Excellent)", completed.getGrade());

        // View completed courses
        List<Enrollment> completedList = studentService.getCompletedCourses(studentId);
        assertTrue(completedList.stream().anyMatch(e -> e.getId().equals(enrollmentId)));
    }

    @Test
    @Order(5)
    public void testInstructorPerformanceTracking() {
        assertNotNull(instructorId);
        assertNotNull(courseId);

        // View performance for course
        List<StudentPerformanceDTO> coursePerformance = instructorService.getCoursePerformance(courseId);
        assertFalse(coursePerformance.isEmpty());
        StudentPerformanceDTO studentPerf = coursePerformance.stream()
                .filter(p -> p.getStudentId().equals(studentId))
                .findFirst()
                .orElse(null);

        assertNotNull(studentPerf);
        assertEquals("Robert Smith", studentPerf.getStudentName());
        assertEquals("COMPLETED", studentPerf.getStatus());
        assertEquals(100, studentPerf.getProgressPercentage());
        assertEquals(95.0, studentPerf.getScore());
    }

    @Test
    @Order(6)
    public void testNotificationModule() {
        // Verify notification was sent to student for new course or updates
        List<Notification> studentNotifications = notificationService.getNotificationsForUser(studentEmail);
        assertFalse(studentNotifications.isEmpty(), "Student should receive notifications for new courses/updates");

        // Verify instructor received notification for new enrollment
        List<Notification> instructorNotifications = notificationService.getNotificationsForUser(instructorEmail);
        assertFalse(instructorNotifications.isEmpty(), "Instructor should receive notifications for new student enrollment");

        // Test sending coursework reminder
        List<Notification> reminders = notificationService.sendPendingCourseworkReminders();
        assertNotNull(reminders);
    }
}
