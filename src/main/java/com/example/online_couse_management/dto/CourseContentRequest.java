package com.example.online_couse_management.dto;

import com.example.online_couse_management.entity.ContentType;

public class CourseContentRequest {
    private String title;
    private ContentType contentType; // VIDEO, PDF, QUIZ
    private String url;
    private String description;
    private String quizQuestions;
    private Integer durationMinutes;
    private Integer sequenceNumber;

    public CourseContentRequest() {
    }

    public CourseContentRequest(String title, ContentType contentType, String url, String description, String quizQuestions, Integer durationMinutes, Integer sequenceNumber) {
        this.title = title;
        this.contentType = contentType;
        this.url = url;
        this.description = description;
        this.quizQuestions = quizQuestions;
        this.durationMinutes = durationMinutes;
        this.sequenceNumber = sequenceNumber;
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
