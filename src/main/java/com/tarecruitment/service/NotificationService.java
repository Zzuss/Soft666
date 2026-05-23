package com.tarecruitment.service;

import com.tarecruitment.dao.NotificationDAO;
import com.tarecruitment.model.Application;
import com.tarecruitment.model.Notification;
import com.tarecruitment.util.JsonUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NotificationService {
    private static final int REJECTION_NOTE_PREVIEW_LENGTH = 120;
    private final NotificationDAO notificationDAO;

    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }

    public void createApplicationStatusNotification(Application application, String jobTitle) {
        if (application == null || application.getUserId() == null) {
            return;
        }
        String status = application.getStatus() != null ? application.getStatus().toUpperCase() : "";
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            return;
        }

        Notification notification = new Notification();
        notification.setNotificationId(JsonUtil.generateId("n"));
        notification.setUserId(application.getUserId());
        notification.setType("APPLICATION_STATUS");
        notification.setRelatedId(application.getApplicationId());
        notification.setRead(false);
        notification.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        String safeJobTitle = jobTitle != null && !jobTitle.trim().isEmpty() ? jobTitle.trim() : "Untitled Job";
        if ("APPROVED".equals(status)) {
            notification.setTitle("Application Approved");
            notification.setMessage("Your application for \"" + safeJobTitle + "\" has been approved.");
        } else {
            notification.setTitle("Application Rejected");
            String note = normalizeRejectionNote(application.getRejectionNote());
            if (note.isEmpty()) {
                notification.setMessage("Your application for \"" + safeJobTitle + "\" has been rejected.");
            } else {
                notification.setMessage(
                        "Your application for \"" + safeJobTitle + "\" has been rejected. Note: " + note
                );
            }
        }
        notificationDAO.addNotification(notification);
    }

    public List<Notification> getUserNotifications(String userId, boolean unreadFirst) {
        List<Notification> notifications = notificationDAO.getNotificationsByUser(userId);
        List<Notification> sorted = new ArrayList<>(notifications);
        if (unreadFirst) {
            sorted.sort(
                    Comparator.comparing(Notification::isRead)
                            .thenComparing(Notification::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
            );
        } else {
            sorted.sort(Comparator.comparing(Notification::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        }
        return sorted;
    }

    public void markRead(String notificationId, String userId) {
        Notification notification = notificationDAO.getNotificationById(notificationId);
        if (notification == null || userId == null || !userId.equals(notification.getUserId())) {
            return;
        }
        if (notification.isRead()) {
            return;
        }
        notification.setRead(true);
        notificationDAO.updateNotification(notification);
    }

    public void markAllRead(String userId) {
        List<Notification> notifications = notificationDAO.getNotificationsByUser(userId);
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
                notificationDAO.updateNotification(notification);
            }
        }
    }

    public int countUnread(String userId) {
        int count = 0;
        for (Notification notification : notificationDAO.getNotificationsByUser(userId)) {
            if (!notification.isRead()) {
                count++;
            }
        }
        return count;
    }

    private String normalizeRejectionNote(String rejectionNote) {
        if (rejectionNote == null) {
            return "";
        }
        String trimmed = rejectionNote.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        if (trimmed.length() <= REJECTION_NOTE_PREVIEW_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, REJECTION_NOTE_PREVIEW_LENGTH) + "...";
    }
}
