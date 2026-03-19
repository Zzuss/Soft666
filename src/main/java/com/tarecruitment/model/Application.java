package com.tarecruitment.model;

import java.sql.Timestamp;

public class Application {
    private String applicationId;
    private String jobId;
    private String userId;
    private String status;
    private Timestamp appliedAt;
    private String reviewedBy;
    private Timestamp reviewedAt;

    public Application() {
    }

    public Application(String applicationId, String jobId, String userId) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.userId = userId;
        this.status = "PENDING";
        this.appliedAt = new Timestamp(System.currentTimeMillis());
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Timestamp appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public Timestamp getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Timestamp reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return "REJECTED".equalsIgnoreCase(status);
    }

    public String getStatusDisplayName(String lang) {
        if ("PENDING".equalsIgnoreCase(status)) {
            return "zh".equals(lang) ? "待审核" : "Pending";
        } else if ("APPROVED".equalsIgnoreCase(status)) {
            return "zh".equals(lang) ? "已批准" : "Approved";
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            return "zh".equals(lang) ? "已拒绝" : "Rejected";
        } else {
            return status;
        }
    }
}