package com.example.online_couse_management.repository;

import com.example.online_couse_management.entity.CourseContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseContentRepository extends JpaRepository<CourseContent, Long> {
    List<CourseContent> findByCourseIdOrderBySequenceNumberAsc(Long courseId);
    List<CourseContent> findByCourseId(Long courseId);
}
