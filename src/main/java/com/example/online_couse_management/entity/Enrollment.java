package com.example.online_couse_management.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private LocalDate enrollmentDate;

    private String status; // ONGOING, COMPLETED, DROPPED

    private Integer progressPercentage = 0; // 0 to 100%

    private LocalDate completionDate;

    private Double score; // Performance score / quiz grade (0-100)

    private String grade; // e.g., "A", "B", "EXCELLENT", "SATISFACTORY"

    public Enrollment() {
        this.enrollmentDate = LocalDate.now();
        this.status = "ONGOING";
        this.progressPercentage = 0;
    }

    public Enrollment(Student student, Course course) {
        this.student = student;
        this.course = course;
        this.enrollmentDate = LocalDate.now();
        this.status = "ONGOING";
        this.progressPercentage = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        if ("COMPLETED".equalsIgnoreCase(status) && this.completionDate == null) {
            this.completionDate = LocalDate.now();
            if (this.progressPercentage == null || this.progressPercentage < 100) {
                this.progressPercentage = 100;
            }
        }
    }

    public Integer getProgressPercentage() {
        return progressPercentage != null ? progressPercentage : 0;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
        if (progressPercentage != null && progressPercentage >= 100) {
            this.status = "COMPLETED";
            if (this.completionDate == null) {
                this.completionDate = LocalDate.now();
            }
        }
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
        if (score != null) {
            if (score >= 90) this.grade = "A (Excellent)";
            else if (score >= 80) this.grade = "B (Good)";
            else if (score >= 70) this.grade = "C (Average)";
            else if (score >= 50) this.grade = "D (Pass)";
            else this.grade = "F (Needs Improvement)";
        }
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
