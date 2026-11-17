package com.example.online_couse_management.controller;

import com.example.online_couse_management.dto.StudentProgressDTO;
import com.example.online_couse_management.entity.Enrollment;
import com.example.online_couse_management.entity.Student;
import com.example.online_couse_management.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentservice;

    public StudentController(StudentService studentservice) {
        this.studentservice = studentservice;
    }

    // Register student
    @PostMapping("/register")
    public Student RegisterStudent(@RequestBody Student student) {
        return studentservice.SaveStudent(student);
    }

    // List all students
    @GetMapping
    public List<Student> getAllStudents() {
        return studentservice.getAllStudents();
    }

    // Get student by ID
    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable Long id) {
        return studentservice.getStudentById(id);
    }

    // Requirement: Update personal details (name, email, contact)
    @PutMapping("/{id}")
    public Student updateStudent(@PathVariable Long id, @RequestBody Student student) {
        return studentservice.updateStudent(id, student);
    }

    // Requirement: Track course progress
    @GetMapping("/{id}/progress")
    public ResponseEntity<StudentProgressDTO> getStudentProgress(@PathVariable Long id) {
        return ResponseEntity.ok(studentservice.getStudentProgress(id));
    }

    // Requirement: View completed courses
    @GetMapping("/{id}/completed-courses")
    public ResponseEntity<List<Enrollment>> getCompletedCourses(@PathVariable Long id) {
        return ResponseEntity.ok(studentservice.getCompletedCourses(id));
    }
}
