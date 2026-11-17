package com.example.online_couse_management.service;

import com.example.online_couse_management.dto.CourseContentRequest;
import com.example.online_couse_management.entity.Course;
import com.example.online_couse_management.entity.CourseContent;
import com.example.online_couse_management.entity.Instructor;
import com.example.online_couse_management.repository.CourseContentRepository;
import com.example.online_couse_management.repository.CourseRepository;
import com.example.online_couse_management.repository.InstructorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courserepository;
    private final CourseContentRepository courseContentRepository;
    private final InstructorRepository instructorRepository;
    private final NotificationService notificationService;

    public CourseService(CourseRepository courserepository,
                         CourseContentRepository courseContentRepository,
                         InstructorRepository instructorRepository,
                         NotificationService notificationService) {
        this.courserepository = courserepository;
        this.courseContentRepository = courseContentRepository;
        this.instructorRepository = instructorRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Course SaveCourse(Course course) {
        // Link instructor entity if provided or found by email/name
        if (course.getInstructor() != null) {
            Optional<Instructor> instructorOpt = instructorRepository.findByEmail(course.getInstructor());
            if (instructorOpt.isEmpty()) {
                instructorOpt = instructorRepository.findFirstByName(course.getInstructor());
            }
            instructorOpt.ifPresent(course::setInstructorEntity);
        }

        Course savedCourse = courserepository.save(course);

        // Requirement: Sends alerts for new course additions
        try {
            notificationService.notifyAllStudentsNewCourse(savedCourse);
        } catch (Exception e) {
            // Log and continue, do not fail course creation
            System.err.println("Failed to send new course notifications: " + e.getMessage());
        }

        return savedCourse;
    }

    public List<Course> getAllCourses() {
        return courserepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courserepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found by Id: " + id));
    }

    public List<Course> getCoursesByCategory(String category) {
        return courserepository.findByCategoryIgnoreCase(category);
    }

    public List<Course> searchCoursesByTitle(String keyword) {
        return courserepository.findByTitleContainingIgnoreCase(keyword);
    }

    public List<Course> getCoursesByInstructor(String instructor) {
        return courserepository.findByInstructorIgnoreCase(instructor);
    }

    public List<Course> getCoursesByInstructorId(Long instructorId) {
        return courserepository.findByInstructorEntityId(instructorId);
    }

    @Transactional
    public Course updateCourse(Long id, Course updatedCourse) {
        Course existingCourse = courserepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));

        existingCourse.setTitle(updatedCourse.getTitle());
        existingCourse.setDescription(updatedCourse.getDescription());
        if (updatedCourse.getInstructor() != null) {
            existingCourse.setInstructor(updatedCourse.getInstructor());
        }
        if (updatedCourse.getCategory() != null) {
            existingCourse.setCategory(updatedCourse.getCategory());
        }
        if (updatedCourse.getInstructorEntity() != null) {
            existingCourse.setInstructorEntity(updatedCourse.getInstructorEntity());
        }

        Course saved = courserepository.save(existingCourse);

        // Requirement: Sends alerts for course updates
        try {
            notificationService.notifyStudentsCourseUpdated(saved);
        } catch (Exception e) {
            System.err.println("Failed to send course update notification: " + e.getMessage());
        }

        return saved;
    }

    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        courserepository.delete(course);
    }

    // Requirement: Assign content like videos, PDFs, quizzes
    @Transactional
    public CourseContent assignContent(Long courseId, CourseContentRequest request) {
        Course course = getCourseById(courseId);

        CourseContent content = new CourseContent();
        content.setCourse(course);
        content.setTitle(request.getTitle());
        content.setContentType(request.getContentType());
        content.setUrl(request.getUrl());
        content.setDescription(request.getDescription());
        content.setQuizQuestions(request.getQuizQuestions());
        content.setDurationMinutes(request.getDurationMinutes());

        int currentCount = courseContentRepository.findByCourseId(courseId).size();
        content.setSequenceNumber(request.getSequenceNumber() != null ? request.getSequenceNumber() : currentCount + 1);

        CourseContent saved = courseContentRepository.save(content);

        // Notify enrolled students of new coursework/content update
        try {
            notificationService.notifyStudentsCourseUpdated(course);
        } catch (Exception e) {
            System.err.println("Failed to send content update notification: " + e.getMessage());
        }

        return saved;
    }

    public List<CourseContent> getCourseContents(Long courseId) {
        // Ensure course exists
        getCourseById(courseId);
        return courseContentRepository.findByCourseIdOrderBySequenceNumberAsc(courseId);
    }

    public void deleteContent(Long contentId) {
        CourseContent content = courseContentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Course content not found with ID: " + contentId));
        courseContentRepository.delete(content);
    }
}
