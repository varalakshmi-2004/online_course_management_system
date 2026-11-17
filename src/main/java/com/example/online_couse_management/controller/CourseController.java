package com.example.online_couse_management.controller;

import com.example.online_couse_management.dto.CourseContentRequest;
import com.example.online_couse_management.entity.Course;
import com.example.online_couse_management.entity.CourseContent;
import com.example.online_couse_management.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService cs;

    public CourseController(CourseService cs) {
        this.cs = cs;
    }

    // Requirement: Create course
    @PostMapping
    public Course createCourse(@RequestBody Course course) {
        return cs.SaveCourse(course);
    }

    @PostMapping("/add")
    public Course AddCoures(@RequestBody Course course) {
        return cs.SaveCourse(course);
    }

    // Requirement: List all available courses for students (supports filtering by category & search keyword)
    @GetMapping
    public List<Course> getAllCourses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        if (search != null && !search.isBlank()) {
            return cs.searchCoursesByTitle(search);
        }
        if (category != null && !category.isBlank()) {
            return cs.getCoursesByCategory(category);
        }
        return cs.getAllCourses();
    }

    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable Long id) {
        return cs.getCourseById(id);
    }

    // Requirement: Manage courses (update)
    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable Long id, @RequestBody Course course) {
        return cs.updateCourse(id, course);
    }

    // Requirement: Manage courses (delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable Long id) {
        cs.deleteCourse(id);
        return ResponseEntity.ok("Course deleted successfully.");
    }

    // Requirement: Assign content like videos, PDFs, quizzes
    @PostMapping("/{id}/content")
    public ResponseEntity<CourseContent> assignContent(
            @PathVariable Long id,
            @RequestBody CourseContentRequest request) {
        CourseContent created = cs.assignContent(id, request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<List<CourseContent>> getCourseContents(@PathVariable Long id) {
        return ResponseEntity.ok(cs.getCourseContents(id));
    }

    @DeleteMapping("/content/{contentId}")
    public ResponseEntity<String> deleteContent(@PathVariable Long contentId) {
        cs.deleteContent(contentId);
        return ResponseEntity.ok("Course content deleted successfully.");
    }
}
