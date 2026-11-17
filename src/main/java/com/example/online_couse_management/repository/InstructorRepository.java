package com.example.online_couse_management.repository;

import com.example.online_couse_management.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Instructor> findByName(String name);
    Optional<Instructor> findFirstByName(String name);
    Optional<Instructor> findByUserId(Long userId);
}
