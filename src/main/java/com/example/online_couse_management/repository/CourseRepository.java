package com.example.online_couse_management.repository;

import com.example.online_couse_management.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByCategoryIgnoreCase(String category);
    List<Course> findByInstructorIgnoreCase(String instructor);
    List<Course> findByInstructorEntityId(Long instructorId);
    List<Course> findByTitleContainingIgnoreCase(String keyword);
}
