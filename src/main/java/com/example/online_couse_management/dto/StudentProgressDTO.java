package com.example.online_couse_management.dto;

import java.time.LocalDate;
import java.util.List;

public class StudentProgressDTO {
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private int totalEnrolledCourses;
    private int ongoingCoursesCount;
    private int completedCoursesCount;
    private double overallAverageProgress;
    private List<CourseProgressItem> courses;

    public static class CourseProgressItem {
        private Long enrollmentId;
        private Long courseId;
        private String courseTitle;
        private String category;
        private String instructor;
        private LocalDate enrollmentDate;
        private LocalDate completionDate;
        private String status;
        private int progressPercentage;
        private Double score;
        private String grade;

        public CourseProgressItem() {
        }

        public CourseProgressItem(Long enrollmentId, Long courseId, String courseTitle, String category,
                                  String instructor, LocalDate enrollmentDate, LocalDate completionDate,
                                  String status, int progressPercentage, Double score, String grade) {
            this.enrollmentId = enrollmentId;
            this.courseId = courseId;
            this.courseTitle = courseTitle;
            this.category = category;
            this.instructor = instructor;
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

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getInstructor() {
            return instructor;
        }

        public void setInstructor(String instructor) {
            this.instructor = instructor;
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

    public StudentProgressDTO() {
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

    public int getTotalEnrolledCourses() {
        return totalEnrolledCourses;
    }

    public void setTotalEnrolledCourses(int totalEnrolledCourses) {
        this.totalEnrolledCourses = totalEnrolledCourses;
    }

    public int getOngoingCoursesCount() {
        return ongoingCoursesCount;
    }

    public void setOngoingCoursesCount(int ongoingCoursesCount) {
        this.ongoingCoursesCount = ongoingCoursesCount;
    }

    public int getCompletedCoursesCount() {
        return completedCoursesCount;
    }

    public void setCompletedCoursesCount(int completedCoursesCount) {
        this.completedCoursesCount = completedCoursesCount;
    }

    public double getOverallAverageProgress() {
        return overallAverageProgress;
    }

    public void setOverallAverageProgress(double overallAverageProgress) {
        this.overallAverageProgress = overallAverageProgress;
    }

    public List<CourseProgressItem> getCourses() {
        return courses;
    }

    public void setCourses(List<CourseProgressItem> courses) {
        this.courses = courses;
    }
}
