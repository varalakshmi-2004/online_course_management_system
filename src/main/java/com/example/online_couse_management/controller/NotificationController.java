package com.example.online_couse_management.controller;

import com.example.online_couse_management.entity.Notification;
import com.example.online_couse_management.entity.NotificationType;
import com.example.online_couse_management.entity.Role;
import com.example.online_couse_management.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Get notifications for user by email
    @GetMapping("/user/{email}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable String email) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(email));
    }

    // Get unread notifications
    @GetMapping("/user/{email}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable String email) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationsForUser(email));
    }

    // Mark notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    // Requirement: Reminds students of pending or upcoming coursework
    @PostMapping("/reminders/send-pending-coursework")
    public ResponseEntity<List<Notification>> triggerPendingCourseworkReminders() {
        List<Notification> reminders = notificationService.sendPendingCourseworkReminders();
        return ResponseEntity.ok(reminders);
    }

    // Send custom alert/notification
    @PostMapping("/send")
    public ResponseEntity<Notification> sendNotification(
            @RequestParam String recipientEmail,
            @RequestParam String role,
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam(defaultValue = "GENERAL") NotificationType type) {

        Role userRole = role.toUpperCase().contains("INSTRUCTOR") ? Role.ROLE_INSTRUCTOR : Role.ROLE_STUDENT;
        Notification notification = notificationService.createNotification(recipientEmail, userRole, title, message, type);
        return ResponseEntity.ok(notification);
    }
}
