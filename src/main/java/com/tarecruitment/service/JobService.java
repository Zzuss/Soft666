package com.tarecruitment.service;

import com.tarecruitment.dao.JobDAO;
import com.tarecruitment.model.Job;
import com.tarecruitment.util.JsonUtil;

import java.util.List;

public class JobService {
    private JobDAO jobDAO;

    public JobService() {
        this.jobDAO = new JobDAO();
    }

    public Job createJob(String title, String type, String description, 
                        String requirements, int positions, String deadline, String moId) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Job title cannot be empty");
        }
        if (positions <= 0) {
            throw new IllegalArgumentException("Positions must be greater than 0");
        }

        Job job = new Job();
        job.setJobId(JsonUtil.generateId("j"));
        job.setTitle(title.trim());
        job.setType(type != null ? type.toUpperCase() : "OTHER");
        job.setDescription(description);
        job.setRequirements(requirements);
        job.setPositions(positions);
        job.setPostedBy(moId);
        job.setDeadline(deadline);
        job.setStatus("OPEN");

        jobDAO.addJob(job);
        return job;
    }

    public void updateJob(Job job) {
        if (job == null || job.getJobId() == null) {
            throw new IllegalArgumentException("Invalid job");
        }
        Job existingJob = jobDAO.getJobById(job.getJobId());
        if (existingJob == null) {
            throw new IllegalArgumentException("Job not found");
        }
        jobDAO.updateJob(job);
    }

    public void closeJob(String jobId) {
        Job job = jobDAO.getJobById(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Job not found");
        }
        job.setStatus("CLOSED");
        jobDAO.updateJob(job);
    }

    public void deleteJob(String jobId) {
        jobDAO.deleteJob(jobId);
    }

    public Job getJobById(String jobId) {
        return jobDAO.getJobById(jobId);
    }

    public List<Job> getAllJobs() {
        return jobDAO.getAllJobs();
    }

    public List<Job> getOpenJobs() {
        return jobDAO.getOpenJobs();
    }

    public List<Job> getJobsByMo(String moId) {
        return jobDAO.getJobsByMo(moId);
    }

    public List<Job> searchJobs(String keyword, String type) {
        List<Job> jobs;
        
        if (type != null && !type.isEmpty() && !type.equalsIgnoreCase("ALL")) {
            jobs = jobDAO.getJobsByType(type);
        } else {
            jobs = jobDAO.getOpenJobs();
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            jobs.removeIf(job -> 
                !job.getTitle().toLowerCase().contains(lowerKeyword) &&
                !job.getDescription().toLowerCase().contains(lowerKeyword)
            );
        }

        return jobs;
    }
}