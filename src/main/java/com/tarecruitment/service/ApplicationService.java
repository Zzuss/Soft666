package com.tarecruitment.service;

import com.tarecruitment.dao.ApplicationDAO;
import com.tarecruitment.dao.JobDAO;
import com.tarecruitment.dao.UserDAO;
import com.tarecruitment.model.Application;
import com.tarecruitment.model.Job;
import com.tarecruitment.model.User;
import com.tarecruitment.util.JsonUtil;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public class ApplicationService {
    public static final int WORKLOAD_LIMIT = 2;
    public static final String OVERLOAD_WARNING_PREFIX = "OVERLOAD_WARNING";
    public static final int REJECTION_NOTE_MAX_LENGTH = 300;

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
        validateApplicantEligibility(applicant);
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
        approveApplication(applicationId, moId, false);
    }

    public void approveApplication(String applicationId, String moId, boolean forceOverload) {
        Application app = applicationDAO.getApplicationById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("Application not found");
        }

        if (!app.isPending()) {
            throw new IllegalArgumentException("Application already processed");
        }
        Job job = jobDAO.getJobById(app.getJobId());
        if (job == null) {
            throw new IllegalArgumentException("Job not found");
        }
        int approvedForJob = getApprovedCountForJob(job.getJobId());
        if (approvedForJob >= job.getPositions()) {
            throw new IllegalArgumentException("No remaining slots for this job");
        }
        int currentWorkload = getApprovedLoadByUser(app.getUserId());
        if (!forceOverload && currentWorkload >= WORKLOAD_LIMIT) {
            throw new IllegalStateException(
                    OVERLOAD_WARNING_PREFIX + "|" + app.getUserId() + "|" + currentWorkload
            );
        }

        app.setStatus("APPROVED");
        app.setRejectionNote("");
        app.setReviewedBy(moId);
        app.setReviewedAt(new Timestamp(System.currentTimeMillis()));
        applicationDAO.updateApplication(app);
    }

    public void rejectApplication(String applicationId, String moId) {
        rejectApplication(applicationId, moId, null);
    }

    public void rejectApplication(String applicationId, String moId, String rejectionNote) {
        Application app = applicationDAO.getApplicationById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("Application not found");
        }

        if (!app.isPending()) {
            throw new IllegalArgumentException("Application already processed");
        }
        String normalizedNote = rejectionNote == null ? "" : rejectionNote.trim();
        if (normalizedNote.length() > REJECTION_NOTE_MAX_LENGTH) {
            throw new IllegalArgumentException("Rejection note cannot exceed " + REJECTION_NOTE_MAX_LENGTH + " characters");
        }

        app.setStatus("REJECTED");
        app.setRejectionNote(normalizedNote);
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
            int count = getApprovedLoadByUser(ta.getUserId());
            workload.put(ta.getUserId(), count);
        }

        return workload;
    }

    public int getApprovedCountForJob(String jobId) {
        List<Application> apps = applicationDAO.getApplicationsByJob(jobId);
        int count = 0;
        for (Application app : apps) {
            if (app != null && app.isApproved()) {
                count++;
            }
        }
        return count;
    }

    public int getRemainingSlots(String jobId) {
        Job job = jobDAO.getJobById(jobId);
        if (job == null) {
            return 0;
        }
        return Math.max(job.getPositions() - getApprovedCountForJob(jobId), 0);
    }

    public int getApprovedLoadByUser(String userId) {
        return applicationDAO.getApprovedApplicationsByUser(userId).size();
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

    private void validateApplicantEligibility(User applicant) {
        List<String> missingFields = new ArrayList<>();
        if (applicant.getStudentId() == null || applicant.getStudentId().trim().isEmpty()) {
            missingFields.add("Student ID");
        }
        if (applicant.getMajor() == null || applicant.getMajor().trim().isEmpty()) {
            missingFields.add("Major");
        }
        if (applicant.getYear() < 1000 || applicant.getYear() > 9999) {
            missingFields.add("Year");
        }
        if (applicant.getPhone() == null || applicant.getPhone().trim().isEmpty()) {
            missingFields.add("Phone");
        }
        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please complete profile fields before applying: " + String.join(", ", missingFields)
            );
        }

        if (applicant.getResumePath() == null || applicant.getResumePath().trim().isEmpty()) {
            throw new IllegalArgumentException("Please upload your resume before applying");
        }
        File resumeFile = new File(JsonUtil.getDataDirectoryPath(), applicant.getResumePath());
        if (!resumeFile.exists() || !resumeFile.isFile()) {
            throw new IllegalArgumentException("Please upload a valid resume before applying");
        }
    }
}
