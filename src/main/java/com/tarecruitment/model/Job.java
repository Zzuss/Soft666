package com.tarecruitment.model;

import java.sql.Timestamp;

public class Job {
    private String jobId;
    private String title;
    private String type;
    private String description;
    private String requirements;
    private String workStartDate;
    private String workEndDate;
    private String workWeekdays;
    private String dailyStartHour;
    private String dailyEndHour;
    private int positions;
    private String postedBy;
    private String deadline;
    private String status;
    private Timestamp createdAt;

    public Job() {
    }

    public Job(String jobId, String title, String type, String description, 
               String requirements, int positions, String postedBy, String deadline) {
        this.jobId = jobId;
        this.title = title;
        this.type = type;
        this.description = description;
        this.requirements = requirements;
        this.positions = positions;
        this.postedBy = postedBy;
        this.deadline = deadline;
        this.status = "OPEN";
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
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

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getWorkStartDate() {
        return workStartDate;
    }

    public void setWorkStartDate(String workStartDate) {
        this.workStartDate = workStartDate;
    }

    public String getWorkEndDate() {
        return workEndDate;
    }

    public void setWorkEndDate(String workEndDate) {
        this.workEndDate = workEndDate;
    }

    public String getWorkWeekdays() {
        return workWeekdays;
    }

    public void setWorkWeekdays(String workWeekdays) {
        this.workWeekdays = workWeekdays;
    }

    public String getDailyStartHour() {
        return dailyStartHour;
    }

    public void setDailyStartHour(String dailyStartHour) {
        this.dailyStartHour = dailyStartHour;
    }

    public String getDailyEndHour() {
        return dailyEndHour;
    }

    public void setDailyEndHour(String dailyEndHour) {
        this.dailyEndHour = dailyEndHour;
    }

    public int getPositions() {
        return positions;
    }

    public void setPositions(int positions) {
        this.positions = positions;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isOpen() {
        return "OPEN".equalsIgnoreCase(status);
    }

    public String getStatusDisplayName(String lang) {
        if ("OPEN".equalsIgnoreCase(status)) {
            return "zh".equals(lang) ? "开放" : "Open";
        } else if ("CLOSED".equalsIgnoreCase(status)) {
            return "zh".equals(lang) ? "已关闭" : "Closed";
        } else {
            return status;
        }
    }

    public String getTypeDisplayName() {
        if ("MODULE".equalsIgnoreCase(type)) {
            return "Module Tutor";
        } else if ("INVIGILATION".equalsIgnoreCase(type)) {
            return "Invigilation";
        } else {
            return "Other";
        }
    }

    public String getTypeDisplayName(String lang) {
        if ("MODULE".equalsIgnoreCase(type)) {
            return "zh".equals(lang) ? "课程导师" : "Module Tutor";
        } else if ("INVIGILATION".equalsIgnoreCase(type)) {
            return "zh".equals(lang) ? "监考" : "Invigilation";
        } else {
            return "zh".equals(lang) ? "其他" : "Other";
        }
    }
}
