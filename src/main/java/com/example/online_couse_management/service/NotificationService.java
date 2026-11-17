package com.example.online_couse_management.service;

import com.example.online_couse_management.entity.*;
import com.example.online_couse_management.repository.EnrollmentRepository;
import com.example.online_couse_management.repository.NotificationRepository;
import com.example.online_couse_management.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               StudentRepository studentRepository,
                               EnrollmentRepository enrollmentRepository) {
        this.notificationRepository = notificationRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public Notification createNotification(String recipientEmail, Role role, String title, String message, NotificationType type) {
        Notification notification = new Notification(recipientEmail, role, title, message, type);
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForUser(String email) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    public List<Notification> getUnreadNotificationsForUser(String email) {
        return notificationRepository.findByRecipientEmailAndIsReadFalseOrderByCreatedAtDesc(email);
    }

    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    // Requirement: Sends alerts for new course additions or updates
    public void notifyAllStudentsNewCourse(Course course) {
        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            String title = "New Course Added: " + course.getTitle();
            String message = String.format("A new course '%s' in category '%s' has been published by %s. Check it out and enroll today!",
                    course.getTitle(), course.getCategory() != null ? course.getCategory() : "General", course.getInstructor());
            createNotification(student.getEmail(), Role.ROLE_STUDENT, title, message, NotificationType.NEW_COURSE);
        }
    }

    public void notifyStudentsCourseUpdated(Course course) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(course.getId());
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            String title = "Course Updated: " + course.getTitle();
            String message = String.format("The course '%s' has been updated with new content or details. Please check the course dashboard.",
                    course.getTitle());
            createNotification(student.getEmail(), Role.ROLE_STUDENT, title, message, NotificationType.COURSE_UPDATE);
        }
    }

    // Requirement: Notifies instructors of newly enrolled students
    public void notifyInstructorNewEnrollment(Enrollment enrollment) {
        Course course = enrollment.getCourse();
        Student student = enrollment.getStudent();

        String instructorEmail = null;
        if (course.getInstructorEntity() != null && course.getInstructorEntity().getEmail() != null) {
            instructorEmail = course.getInstructorEntity().getEmail();
        } else if (course.getInstructor() != null && course.getInstructor().contains("@")) {
            instructorEmail = course.getInstructor();
        }

        if (instructorEmail != null) {
            String title = "New Student Enrolled: " + course.getTitle();
            String message = String.format("Student %s (%s) has just enrolled in your course '%s'.",
                    student.getName(), student.getEmail(), course.getTitle());
            createNotification(instructorEmail, Role.ROLE_INSTRUCTOR, title, message, NotificationType.NEW_ENROLLMENT);
        }
    }

    // Requirement: Reminds students of pending or upcoming coursework
    public List<Notification> sendPendingCourseworkReminders() {
        List<Enrollment> ongoingEnrollments = enrollmentRepository.findByStatusIgnoreCase("ONGOING");
        List<Notification> createdReminders = new ArrayList<>();

        for (Enrollment enrollment : ongoingEnrollments) {
            int progress = enrollment.getProgressPercentage();
            if (progress < 100) {
                Student student = enrollment.getStudent();
                Course course = enrollment.getCourse();
                String title = "Coursework Reminder: " + course.getTitle();
                String message = String.format("Friendly reminder: You have pending coursework in '%s'. Your current progress is %d%%. Keep learning to complete your course!",
                        course.getTitle(), progress);
                Notification notification = createNotification(student.getEmail(), Role.ROLE_STUDENT, title, message, NotificationType.COURSEWORK_REMINDER);
                createdReminders.add(notification);
            }
        }
        return createdReminders;
    }
}
