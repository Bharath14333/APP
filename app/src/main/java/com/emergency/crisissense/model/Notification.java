package com.emergency.crisissense.model;

import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable {
    private String notificationId;
    private String userId; // Specific user ID or "all" for broadcast alerts
    private String title;
    private String message;
    private Date timestamp;

    // Required empty constructor for Firestore
    public Notification() {
    }

    public Notification(String notificationId, String userId, String title, String message) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.timestamp = new Date();
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
