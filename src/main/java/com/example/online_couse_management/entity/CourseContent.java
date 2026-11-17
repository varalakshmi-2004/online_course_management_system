package com.example.online_couse_management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "course_contents")
public class CourseContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnore
    private Course course;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType contentType;

    @Column(length = 2000)
    private String url; // Video link, PDF link, or resource link

    @Column(length = 1000)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String quizQuestions; // For quiz type content

    private Integer durationMinutes;

    private Integer sequenceNumber;

    public CourseContent() {
    }

    public CourseContent(Course course, String title, ContentType contentType, String url, String description, Integer durationMinutes, Integer sequenceNumber) {
        this.course = course;
        this.title = title;
        this.contentType = contentType;
        this.url = url;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.sequenceNumber = sequenceNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQuizQuestions() {
        return quizQuestions;
    }

    public void setQuizQuestions(String quizQuestions) {
        this.quizQuestions = quizQuestions;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
