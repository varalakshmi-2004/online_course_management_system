package com.example.online_couse_management.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String instructor; // Name or email of instructor

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Instructor instructorEntity;

    private String category;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseContent> contents = new ArrayList<>();

    public Course() {
    }

    public Course(String title, String description, String instructor, String category) {
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public Instructor getInstructorEntity() {
        return instructorEntity;
    }

    public void setInstructorEntity(Instructor instructorEntity) {
        this.instructorEntity = instructorEntity;
        if (instructorEntity != null && (this.instructor == null || this.instructor.isBlank())) {
            this.instructor = instructorEntity.getName();
        }
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<CourseContent> getContents() {
        return contents;
    }

    public void setContents(List<CourseContent> contents) {
        this.contents = contents;
    }
}
