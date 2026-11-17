package com.example.online_couse_management.service;

import com.example.online_couse_management.dto.StudentProgressDTO;
import com.example.online_couse_management.entity.Enrollment;
import com.example.online_couse_management.entity.Student;
import com.example.online_couse_management.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentService enrollmentService;

    public StudentService(StudentRepository studentRepository, EnrollmentService enrollmentService) {
        this.studentRepository = studentRepository;
        this.enrollmentService = enrollmentService;
    }

    public Student SaveStudent(Student student) {
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + id));
    }

    public Optional<Student> getStudentByEmail(String email) {
        return studentRepository.findByEmail(email);
    }

    @Transactional
    public Student updateStudent(Long id, Student updatedStudent) {
        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + id));

        if (updatedStudent.getName() != null) {
            existingStudent.setName(updatedStudent.getName());
        }
        if (updatedStudent.getEmail() != null) {
            existingStudent.setEmail(updatedStudent.getEmail());
        }
        if (updatedStudent.getContact() != null) {
            existingStudent.setContact(updatedStudent.getContact());
        }

        return studentRepository.save(existingStudent);
    }

    // Requirement: Track course progress
    public StudentProgressDTO getStudentProgress(Long studentId) {
        return enrollmentService.getStudentProgress(studentId);
    }

    // Requirement: View completed courses
    public List<Enrollment> getCompletedCourses(Long studentId) {
        return enrollmentService.getCompletedCourses(studentId);
    }
}
