package com.example.online_couse_management.config;

import com.example.online_couse_management.dto.CourseContentRequest;
import com.example.online_couse_management.dto.RegisterRequest;
import com.example.online_couse_management.entity.*;
import com.example.online_couse_management.repository.*;
import com.example.online_couse_management.service.AuthService;
import com.example.online_couse_management.service.CourseService;
import com.example.online_couse_management.service.EnrollmentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final CourseRepository courseRepository;
    private final CourseService courseService;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;

    public DataInitializer(UserRepository userRepository,
                           AuthService authService,
                           CourseRepository courseRepository,
                           CourseService courseService,
                           StudentRepository studentRepository,
                           EnrollmentRepository enrollmentRepository,
                           EnrollmentService enrollmentService) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.courseRepository = courseRepository;
        this.courseService = courseService;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentService = enrollmentService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize default instructor if not present
        if (!userRepository.existsByUsername("instructor@learn.com")) {
            RegisterRequest instReq = new RegisterRequest(
                    "Dr. Alan Turing",
                    "instructor@learn.com",
                    "Password@123",
                    "+1-555-0101",
                    "ROLE_INSTRUCTOR",
                    "Computer Science & Software Architecture"
            );
            authService.register(instReq);
        }

        // Initialize default student if not present
        if (!userRepository.existsByUsername("student@learn.com")) {
            RegisterRequest studReq = new RegisterRequest(
                    "Alice Walker",
                    "student@learn.com",
                    "Password@123",
                    "+1-555-0202",
                    "ROLE_STUDENT",
                    null
            );
            authService.register(studReq);
        }

        // Initialize sample courses if none exist
        if (courseRepository.count() == 0) {
            Course javaCourse = new Course(
                    "Full-Stack Java & Spring Boot Masterclass",
                    "Comprehensive course covering Core Java, Spring Boot 3, RESTful APIs, Spring Security, JPA, and Database Design.",
                    "Dr. Alan Turing",
                    "Software Engineering"
            );
            javaCourse = courseService.SaveCourse(javaCourse);

            // Add assigned contents (Video, PDF, Quiz)
            courseService.assignContent(javaCourse.getId(), new CourseContentRequest(
                    "Video: Getting Started with Spring Boot & Architecture",
                    ContentType.VIDEO,
                    "https://www.youtube.com/watch?v=9SGDpanrc8U",
                    "Overview of Spring framework, dependency injection, and project setup.",
                    null,
                    45,
                    1
            ));

            courseService.assignContent(javaCourse.getId(), new CourseContentRequest(
                    "PDF: Spring Security & REST API Architecture Guide",
                    ContentType.PDF,
                    "https://docs.spring.io/spring-security/reference/servlet/architecture.html",
                    "Official architectural overview and best practices document.",
                    null,
                    30,
                    2
            ));

            courseService.assignContent(javaCourse.getId(), new CourseContentRequest(
                    "Quiz: Spring Security & Architecture Assessment",
                    ContentType.QUIZ,
                    null,
                    "Test your knowledge of Spring Security filters and role-based access control.",
                    "Q1: What filter handles JWT Bearer tokens? A) OncePerRequestFilter B) BasicAuthFilter",
                    20,
                    3
            ));

            // Create second course
            Course aiCourse = new Course(
                    "Applied Machine Learning & Python",
                    "Hands-on machine learning algorithms, deep learning fundamentals, and data preprocessing with NumPy and Pandas.",
                    "Dr. Alan Turing",
                    "Artificial Intelligence"
            );
            aiCourse = courseService.SaveCourse(aiCourse);

            courseService.assignContent(aiCourse.getId(), new CourseContentRequest(
                    "Video: Supervised Learning & Gradient Descent",
                    ContentType.VIDEO,
                    "https://www.youtube.com/watch?v=aircAruvnKk",
                    "Visual introduction to neural networks and loss gradient optimization.",
                    null,
                    50,
                    1
            ));

            courseService.assignContent(aiCourse.getId(), new CourseContentRequest(
                    "PDF: ML Mathematical Foundations Cheatsheet",
                    ContentType.PDF,
                    "https://stanford.edu/~shervine/teaching/cs-229/cheatsheet-machine-learning-tips-and-tricks",
                    "Essential matrix calculus, linear algebra, and probability theory.",
                    null,
                    25,
                    2
            ));

            courseService.assignContent(aiCourse.getId(), new CourseContentRequest(
                    "Quiz: Machine Learning Classification Checkpoint",
                    ContentType.QUIZ,
                    null,
                    "Evaluate your understanding of regression vs classification.",
                    "Q1: What metric is best for imbalanced classes? A) Accuracy B) F1-Score",
                    15,
                    3
            ));

            // Enroll student in Java course with 60% progress
            Optional<Student> studentOpt = studentRepository.findByEmail("student@learn.com");
            if (studentOpt.isPresent()) {
                Enrollment enrollment = enrollmentService.enrollStudent(studentOpt.get().getId(), javaCourse.getId());
                enrollmentService.updateProgress(enrollment.getId(), 65);
                enrollmentService.submitQuizScore(enrollment.getId(), 88.0);
            }
        }
    }
}
