package com.tarecruitment.dao;

import com.tarecruitment.model.Notification;
import com.tarecruitment.util.JsonUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    private static final String DATA_FILE = "notifications.json";
    private JSONObject data;

    public NotificationDAO() {
        loadData();
    }

    private void loadData() {
        this.data = JsonUtil.readJsonFile(DATA_FILE);
        if (!data.has("notifications")) {
            data.put("notifications", new JSONArray());
        }
    }

    private void saveData() {
        JsonUtil.writeJsonFile(DATA_FILE, data);
    }

    public void addNotification(Notification notification) {
        loadData();
        JSONArray notifications = data.getJSONArray("notifications");
        notifications.put(notificationToJson(notification));
        saveData();
    }

    public void updateNotification(Notification notification) {
        loadData();
        JSONArray notifications = data.getJSONArray("notifications");
        for (int i = 0; i < notifications.length(); i++) {
            JSONObject n = notifications.getJSONObject(i);
            if (notification.getNotificationId().equals(n.optString("notificationId"))) {
                notifications.put(i, notificationToJson(notification));
                saveData();
                return;
            }
        }
    }

    public Notification getNotificationById(String notificationId) {
        loadData();
        JSONArray notifications = data.getJSONArray("notifications");
        for (int i = 0; i < notifications.length(); i++) {
            JSONObject n = notifications.getJSONObject(i);
            if (notificationId.equals(n.optString("notificationId"))) {
                return jsonToNotification(n);
            }
        }
        return null;
    }

    public List<Notification> getNotificationsByUser(String userId) {
        loadData();
        List<Notification> notificationsForUser = new ArrayList<>();
        JSONArray notifications = data.getJSONArray("notifications");
        for (int i = 0; i < notifications.length(); i++) {
            JSONObject n = notifications.getJSONObject(i);
            if (userId.equals(n.optString("userId"))) {
                notificationsForUser.add(jsonToNotification(n));
            }
        }
        return notificationsForUser;
    }

    private JSONObject notificationToJson(Notification notification) {
        JSONObject json = new JSONObject();
        json.put("notificationId", notification.getNotificationId());
        json.put("userId", notification.getUserId());
        json.put("type", notification.getType() != null ? notification.getType() : "");
        json.put("title", notification.getTitle() != null ? notification.getTitle() : "");
        json.put("message", notification.getMessage() != null ? notification.getMessage() : "");
        json.put("relatedId", notification.getRelatedId() != null ? notification.getRelatedId() : "");
        json.put("read", notification.isRead());
        if (notification.getCreatedAt() != null) {
            json.put("createdAt", notification.getCreatedAt().toString());
        }
        return json;
    }

    private Notification jsonToNotification(JSONObject json) {
        Notification notification = new Notification();
        notification.setNotificationId(json.optString("notificationId", ""));
        notification.setUserId(json.optString("userId", ""));
        notification.setType(json.optString("type", ""));
        notification.setTitle(json.optString("title", ""));
        notification.setMessage(json.optString("message", ""));
        notification.setRelatedId(json.optString("relatedId", ""));
        notification.setRead(json.optBoolean("read", false));
        notification.setCreatedAt(JsonUtil.parseTimestamp(json.optString("createdAt", null)));
        return notification;
    }
}
