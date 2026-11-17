package com.example.online_couse_management.service;

import com.example.online_couse_management.dto.StudentPerformanceDTO;
import com.example.online_couse_management.dto.StudentProgressDTO;
import com.example.online_couse_management.entity.Course;
import com.example.online_couse_management.entity.Enrollment;
import com.example.online_couse_management.entity.Student;
import com.example.online_couse_management.repository.CourseRepository;
import com.example.online_couse_management.repository.EnrollmentRepository;
import com.example.online_couse_management.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final NotificationService notificationService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             StudentRepository studentRepository,
                             CourseRepository courseRepository,
                             NotificationService notificationService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Enrollment enrollStudent(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        Optional<Enrollment> existingOpt = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (existingOpt.isPresent()) {
            Enrollment existing = existingOpt.get();
            if ("DROPPED".equalsIgnoreCase(existing.getStatus())) {
                existing.setStatus("ONGOING");
                existing.setEnrollmentDate(LocalDate.now());
                return enrollmentRepository.save(existing);
            }
            throw new RuntimeException("Student is already enrolled in this course with status: " + existing.getStatus());
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus("ONGOING");
        enrollment.setProgressPercentage(0);

        Enrollment saved = enrollmentRepository.save(enrollment);

        // Requirement: Notifies instructors of newly enrolled students
        try {
            notificationService.notifyInstructorNewEnrollment(saved);
        } catch (Exception e) {
            System.err.println("Failed to send new enrollment notification: " + e.getMessage());
        }

        return saved;
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    public Enrollment getEnrollmentById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Enrollment not found with ID: " + id));
    }

    public List<Enrollment> getEnrollmentsByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    // Requirement: Provides history of courses taken by the student
    public List<Enrollment> getStudentCourseHistory(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));
        return enrollmentRepository.findByStudentId(studentId);
    }

    // Requirement: View completed courses
    public List<Enrollment> getCompletedCourses(Long studentId) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));
        return enrollmentRepository.findByStudentIdAndStatusIgnoreCase(studentId, "COMPLETED");
    }

    // Requirement: Track course progress
    public StudentProgressDTO getStudentProgress(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

        StudentProgressDTO dto = new StudentProgressDTO();
        dto.setStudentId(student.getId());
        dto.setStudentName(student.getName());
        dto.setStudentEmail(student.getEmail());
        dto.setTotalEnrolledCourses(enrollments.size());

        int ongoing = 0;
        int completed = 0;
        int totalProgress = 0;

        List<StudentProgressDTO.CourseProgressItem> items = new ArrayList<>();
        for (Enrollment e : enrollments) {
            if ("COMPLETED".equalsIgnoreCase(e.getStatus())) {
                completed++;
            } else if ("ONGOING".equalsIgnoreCase(e.getStatus())) {
                ongoing++;
            }
            int prog = e.getProgressPercentage();
            totalProgress += prog;

            items.add(new StudentProgressDTO.CourseProgressItem(
                    e.getId(),
                    e.getCourse().getId(),
                    e.getCourse().getTitle(),
                    e.getCourse().getCategory(),
                    e.getCourse().getInstructor(),
                    e.getEnrollmentDate(),
                    e.getCompletionDate(),
                    e.getStatus(),
                    prog,
                    e.getScore(),
                    e.getGrade()
            ));
        }

        dto.setOngoingCoursesCount(ongoing);
        dto.setCompletedCoursesCount(completed);
        dto.setOverallAverageProgress(enrollments.isEmpty() ? 0.0 : (double) totalProgress / enrollments.size());
        dto.setCourses(items);

        return dto;
    }

    @Transactional
    public Enrollment updateEnrollmentStatus(Long id, String status) {
        Enrollment enrollment = getEnrollmentById(id);
        enrollment.setStatus(status.toUpperCase());
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Enrollment updateProgress(Long id, Integer progressPercentage) {
        Enrollment enrollment = getEnrollmentById(id);
        if (progressPercentage < 0 || progressPercentage > 100) {
            throw new IllegalArgumentException("Progress percentage must be between 0 and 100");
        }
        enrollment.setProgressPercentage(progressPercentage);
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public Enrollment submitQuizScore(Long id, Double score) {
        Enrollment enrollment = getEnrollmentById(id);
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Score must be between 0 and 100");
        }
        enrollment.setScore(score);
        return enrollmentRepository.save(enrollment);
    }

    // Requirement: View enrolled students and track their performance (Instructor)
    public List<StudentPerformanceDTO> getStudentPerformanceForCourse(Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        return enrollments.stream().map(this::mapToPerformanceDTO).collect(Collectors.toList());
    }

    public List<StudentPerformanceDTO> getStudentPerformanceForInstructor(Long instructorId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseInstructorEntityId(instructorId);
        return enrollments.stream().map(this::mapToPerformanceDTO).collect(Collectors.toList());
    }

    public List<StudentPerformanceDTO> getStudentPerformanceForInstructorName(String instructorName) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseInstructorIgnoreCase(instructorName);
        return enrollments.stream().map(this::mapToPerformanceDTO).collect(Collectors.toList());
    }

    private StudentPerformanceDTO mapToPerformanceDTO(Enrollment e) {
        return new StudentPerformanceDTO(
                e.getId(),
                e.getCourse().getId(),
                e.getCourse().getTitle(),
                e.getStudent().getId(),
                e.getStudent().getName(),
                e.getStudent().getEmail(),
                e.getStudent().getContact(),
                e.getEnrollmentDate(),
                e.getCompletionDate(),
                e.getStatus(),
                e.getProgressPercentage(),
                e.getScore(),
                e.getGrade()
        );
    }
}
