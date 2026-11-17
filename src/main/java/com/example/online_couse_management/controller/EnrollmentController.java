package com.example.online_couse_management.controller;

import com.example.online_couse_management.entity.Enrollment;
import com.example.online_couse_management.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // Requirement: Links students to courses, stores date of enrollment and status
    @PostMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<Enrollment> enrollStudent(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.enrollStudent(studentId, courseId));
    }

    @GetMapping
    public List<Enrollment> getAllEnrollments() {
        return enrollmentService.getAllEnrollments();
    }

    @GetMapping("/{id}")
    public Enrollment getEnrollmentById(@PathVariable Long id) {
        return enrollmentService.getEnrollmentById(id);
    }

    @GetMapping("/student/{studentId}")
    public List<Enrollment> getEnrollmentsByStudentId(@PathVariable Long studentId) {
        return enrollmentService.getEnrollmentsByStudentId(studentId);
    }

    // Requirement: Provides history of courses taken by the student
    @GetMapping("/student/{studentId}/history")
    public ResponseEntity<List<Enrollment>> getStudentCourseHistory(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.getStudentCourseHistory(studentId));
    }

    // Requirement: Update enrollment status (ongoing, completed, dropped)
    @PutMapping("/{id}/status")
    public Enrollment updateEnrollmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return enrollmentService.updateEnrollmentStatus(id, status);
    }

    // Requirement: Track course progress
    @PutMapping("/{id}/progress")
    public ResponseEntity<Enrollment> updateProgress(
            @PathVariable Long id,
            @RequestParam Integer progress) {
        return ResponseEntity.ok(enrollmentService.updateProgress(id, progress));
    }

    // Track performance / quiz scores
    @PostMapping("/{id}/score")
    public ResponseEntity<Enrollment> submitScore(
            @PathVariable Long id,
            @RequestParam Double score) {
        return ResponseEntity.ok(enrollmentService.submitQuizScore(id, score));
    }
}
