package com.tarecruitment.service;

import com.tarecruitment.dao.ApplicationDAO;
import com.tarecruitment.dao.JobDAO;
import com.tarecruitment.dao.UserDAO;
import com.tarecruitment.model.Application;
import com.tarecruitment.model.Job;
import com.tarecruitment.model.User;
import com.tarecruitment.util.JsonUtil;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ApplicationService {
    private ApplicationDAO applicationDAO;
    private JobDAO jobDAO;
    private UserDAO userDAO;

    public ApplicationService() {
        this.applicationDAO = new ApplicationDAO();
        this.jobDAO = new JobDAO();
        this.userDAO = new UserDAO();
    }

    public Application applyForJob(String jobId, String userId) {
        if (jobId == null || userId == null) {
            throw new IllegalArgumentException("Invalid job or user");
        }

        Job job = jobDAO.getJobById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Job not found");
        }

        if (!job.isOpen()) {
            throw new IllegalArgumentException("Job is closed");
        }

        if (applicationDAO.hasApplied(jobId, userId)) {
            throw new IllegalArgumentException("You have already applied for this job");
        }

        Application app = new Application();
        app.setApplicationId(JsonUtil.generateId("a"));
        app.setJobId(jobId);
        app.setUserId(userId);
        app.setStatus("PENDING");

        applicationDAO.addApplication(app);
        return app;
    }

    public void approveApplication(String applicationId, String moId) {
        Application app = applicationDAO.getApplicationById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("Application not found");
        }

        if (!app.isPending()) {
            throw new IllegalArgumentException("Application already processed");
        }

        app.setStatus("APPROVED");
        app.setReviewedBy(moId);
        app.setReviewedAt(new Timestamp(System.currentTimeMillis()));
        applicationDAO.updateApplication(app);
    }

    public void rejectApplication(String applicationId, String moId) {
        Application app = applicationDAO.getApplicationById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("Application not found");
        }

        if (!app.isPending()) {
            throw new IllegalArgumentException("Application already processed");
        }

        app.setStatus("REJECTED");
        app.setReviewedBy(moId);
        app.setReviewedAt(new Timestamp(System.currentTimeMillis()));
        applicationDAO.updateApplication(app);
    }

    public List<Application> getUserApplications(String userId) {
        return applicationDAO.getApplicationsByUser(userId);
    }

    public List<Application> getJobApplications(String jobId) {
        return applicationDAO.getApplicationsByJob(jobId);
    }

    public Application getApplication(String jobId, String userId) {
        return applicationDAO.getApplication(jobId, userId);
    }

    public Map<String, Integer> getTAWorkload() {
        List<User> tas = userDAO.getTAs();
        Map<String, Integer> workload = new HashMap<>();

        for (User ta : tas) {
            int count = applicationDAO.getApprovedApplicationsByUser(ta.getUserId()).size();
            workload.put(ta.getUserId(), count);
        }

        return workload;
    }
}