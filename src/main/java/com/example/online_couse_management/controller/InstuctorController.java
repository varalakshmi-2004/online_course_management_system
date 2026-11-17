package com.example.online_couse_management.controller;

import com.example.online_couse_management.dto.StudentPerformanceDTO;
import com.example.online_couse_management.entity.Course;
import com.example.online_couse_management.entity.Instructor;
import com.example.online_couse_management.service.InstructorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/instructor", "/instructors"})
public class InstuctorController {

    private final InstructorService instructorService;

    public InstuctorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    // Requirement: Register as an instructor
    @PostMapping("/register")
    public Instructor saveInstructor(@RequestBody Instructor instructor) {
        return instructorService.SaveInstructor(instructor);
    }

    @GetMapping
    public List<Instructor> getAllInstructor() {
        return instructorService.getAllInstructors();
    }

    @GetMapping("/{id}")
    public Instructor getInstructorById(@PathVariable Long id) {
        return instructorService.getInstructorById(id);
    }

    @PutMapping("/{id}")
    public Instructor updateInstructor(@PathVariable Long id, @RequestBody Instructor instructor) {
        return instructorService.updateInstructor(id, instructor);
    }

    // Requirement: Add and manage courses they're teaching (view courses taught)
    @GetMapping("/{id}/courses")
    public ResponseEntity<List<Course>> getCoursesByInstructor(@PathVariable Long id) {
        return ResponseEntity.ok(instructorService.getCoursesByInstructorId(id));
    }

    // Requirement: View enrolled students and track their performance
    @GetMapping("/{id}/students")
    public ResponseEntity<List<StudentPerformanceDTO>> getEnrolledStudentsAndPerformance(@PathVariable Long id) {
        return ResponseEntity.ok(instructorService.getEnrolledStudentsAndPerformance(id));
    }

    // Requirement: View enrolled students and performance for a specific course
    @GetMapping("/course/{courseId}/students")
    public ResponseEntity<List<StudentPerformanceDTO>> getCourseStudentsPerformance(@PathVariable Long courseId) {
        return ResponseEntity.ok(instructorService.getCoursePerformance(courseId));
    }
}
