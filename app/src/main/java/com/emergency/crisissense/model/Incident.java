package com.emergency.crisissense.model;

import java.io.Serializable;
import java.util.Date;

public class Incident implements Serializable {
    private String incidentId;
    private String userId;
    private String reportedByName;
    private String title;
    private String type; // Accident, Fire, Flood, etc.
    private String description;
    private String location; // GPS coordinates
    private String locationName; // Address name
    private String image; // Storage URL
    private String status; // pending, approved, rejected, resolved
    private String severity; // low, medium, high, critical
    private Date createdDate;

    // Required empty constructor for Firestore
    public Incident() {
    }

    public Incident(String incidentId, String userId, String reportedByName, String title, String type, 
                    String description, String location, String locationName, String image, 
                    String status, String severity) {
        this.incidentId = incidentId;
        this.userId = userId;
        this.reportedByName = reportedByName;
        this.title = title;
        this.type = type;
        this.description = description;
        this.location = location;
        this.locationName = locationName;
        this.image = image;
        this.status = status;
        this.severity = severity;
        this.createdDate = new Date();
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReportedByName() {
        return reportedByName;
    }

    public void setReportedByName(String reportedByName) {
        this.reportedByName = reportedByName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
