package com.example.online_couse_management.dto;

import java.time.LocalDate;

public class StudentPerformanceDTO {
    private Long enrollmentId;
    private Long courseId;
    private String courseTitle;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentContact;
    private LocalDate enrollmentDate;
    private LocalDate completionDate;
    private String status;
    private int progressPercentage;
    private Double score;
    private String grade;

    public StudentPerformanceDTO() {
    }

    public StudentPerformanceDTO(Long enrollmentId, Long courseId, String courseTitle, Long studentId,
                                 String studentName, String studentEmail, String studentContact,
                                 LocalDate enrollmentDate, LocalDate completionDate, String status,
                                 int progressPercentage, Double score, String grade) {
        this.enrollmentId = enrollmentId;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.studentContact = studentContact;
        this.enrollmentDate = enrollmentDate;
        this.completionDate = completionDate;
        this.status = status;
        this.progressPercentage = progressPercentage;
        this.score = score;
        this.grade = grade;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentContact() {
        return studentContact;
    }

    public void setStudentContact(String studentContact) {
        this.studentContact = studentContact;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
