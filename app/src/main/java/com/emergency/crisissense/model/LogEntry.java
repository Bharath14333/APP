package com.emergency.crisissense.model;

import java.io.Serializable;
import java.util.Date;

public class LogEntry implements Serializable {
    private String logId;
    private String action;
    private String details;
    private String performedBy;
    private Date timestamp;

    public LogEntry() {
    }

    public LogEntry(String logId, String action, String details, String performedBy) {
        this.logId = logId;
        this.action = action;
        this.details = details;
        this.performedBy = performedBy;
        this.timestamp = new Date();
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
