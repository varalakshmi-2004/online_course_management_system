package com.example.online_couse_management.service;

import com.example.online_couse_management.dto.StudentPerformanceDTO;
import com.example.online_couse_management.entity.Course;
import com.example.online_couse_management.entity.Instructor;
import com.example.online_couse_management.repository.CourseRepository;
import com.example.online_couse_management.repository.InstructorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;

    public InstructorService(InstructorRepository instructorRepository,
                             CourseRepository courseRepository,
                             EnrollmentService enrollmentService) {
        this.instructorRepository = instructorRepository;
        this.courseRepository = courseRepository;
        this.enrollmentService = enrollmentService;
    }

    public Instructor SaveInstructor(Instructor instructor) {
        return instructorRepository.save(instructor);
    }

    public List<Instructor> getAllInstructors() {
        return instructorRepository.findAll();
    }

    public Instructor getInstructorById(Long id) {
        return instructorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Instructor not found with ID: " + id));
    }

    public Optional<Instructor> getInstructorByEmail(String email) {
        return instructorRepository.findByEmail(email);
    }

    @Transactional
    public Instructor updateInstructor(Long id, Instructor updatedInstructor) {
        Instructor existing = getInstructorById(id);
        if (updatedInstructor.getName() != null) {
            existing.setName(updatedInstructor.getName());
        }
        if (updatedInstructor.getEmail() != null) {
            existing.setEmail(updatedInstructor.getEmail());
        }
        if (updatedInstructor.getContact() != null) {
            existing.setContact(updatedInstructor.getContact());
        }
        if (updatedInstructor.getSpecialization() != null) {
            existing.setSpecialization(updatedInstructor.getSpecialization());
        }
        return instructorRepository.save(existing);
    }

    // Requirement: Add and manage courses they're teaching
    public List<Course> getCoursesByInstructorId(Long instructorId) {
        Instructor instructor = getInstructorById(instructorId);
        List<Course> courses = courseRepository.findByInstructorEntityId(instructorId);
        if (courses.isEmpty() && instructor.getName() != null) {
            courses = courseRepository.findByInstructorIgnoreCase(instructor.getName());
        }
        return courses;
    }

    // Requirement: View enrolled students and track their performance
    public List<StudentPerformanceDTO> getEnrolledStudentsAndPerformance(Long instructorId) {
        Instructor instructor = getInstructorById(instructorId);
        List<StudentPerformanceDTO> list = enrollmentService.getStudentPerformanceForInstructor(instructorId);
        if (list.isEmpty() && instructor.getName() != null) {
            list = enrollmentService.getStudentPerformanceForInstructorName(instructor.getName());
        }
        return list;
    }

    public List<StudentPerformanceDTO> getCoursePerformance(Long courseId) {
        return enrollmentService.getStudentPerformanceForCourse(courseId);
    }
}
