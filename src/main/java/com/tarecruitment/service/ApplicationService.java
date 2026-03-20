package com.tarecruitment.service;

import com.tarecruitment.dao.ApplicationDAO;
import com.tarecruitment.dao.JobDAO;
import com.tarecruitment.dao.UserDAO;
import com.tarecruitment.model.Application;
import com.tarecruitment.model.Job;
import com.tarecruitment.model.User;
import com.tarecruitment.util.JsonUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public class ApplicationService {
    private ApplicationDAO applicationDAO;
    private JobDAO jobDAO;
    private UserDAO userDAO;
    private MatchingService matchingService;

    public ApplicationService() {
        this.applicationDAO = new ApplicationDAO();
        this.jobDAO = new JobDAO();
        this.userDAO = new UserDAO();
        this.matchingService = new MatchingService();
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

        User applicant = userDAO.getUserById(userId);
        if (applicant == null) {
            throw new IllegalArgumentException("User not found");
        }
        MatchingService.MatchResult matchResult = matchingService.evaluate(job, applicant);

        Application app = new Application();
        app.setApplicationId(JsonUtil.generateId("a"));
        app.setJobId(jobId);
        app.setUserId(userId);
        app.setStatus("PENDING");
        app.setAppliedAt(new Timestamp(System.currentTimeMillis()));
        app.setMatchScore(matchResult.getScore());
        app.setMatchedSkills(matchResult.getMatchedSkills());
        app.setMissingSkills(matchResult.getMissingSkills());

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
        List<Application> apps = applicationDAO.getApplicationsByUser(userId);
        return refreshAndReturn(apps);
    }

    public List<Application> getJobApplications(String jobId) {
        List<Application> apps = applicationDAO.getApplicationsByJob(jobId);
        return refreshAndReturn(apps);
    }

    public Application getApplication(String jobId, String userId) {
        Application app = applicationDAO.getApplication(jobId, userId);
        if (app != null) {
            refreshMatchingResult(app);
        }
        return app;
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

    private List<Application> refreshAndReturn(List<Application> apps) {
        List<Application> result = new ArrayList<>();
        for (Application app : apps) {
            refreshMatchingResult(app);
            result.add(app);
        }
        return result;
    }

    private void refreshMatchingResult(Application app) {
        Job job = jobDAO.getJobById(app.getJobId());
        User applicant = userDAO.getUserById(app.getUserId());
        if (job == null || applicant == null) {
            return;
        }
        MatchingService.MatchResult matchResult = matchingService.evaluate(job, applicant);

        boolean changed = !Objects.equals(app.getMatchScore(), matchResult.getScore())
                || !Objects.equals(app.getMatchedSkills(), matchResult.getMatchedSkills())
                || !Objects.equals(app.getMissingSkills(), matchResult.getMissingSkills());
        if (!changed) {
            return;
        }

        app.setMatchScore(matchResult.getScore());
        app.setMatchedSkills(matchResult.getMatchedSkills());
        app.setMissingSkills(matchResult.getMissingSkills());
        applicationDAO.updateApplication(app);
    }
}
