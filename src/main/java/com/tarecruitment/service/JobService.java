package com.tarecruitment.service;

import com.tarecruitment.dao.JobDAO;
import com.tarecruitment.model.Job;
import com.tarecruitment.util.JsonUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class JobService {
    private JobDAO jobDAO;

    public JobService() {
        this.jobDAO = new JobDAO();
    }

    public Job createJob(String title, String type, String description, 
                        String requirements, int positions, String deadline, String moId) {
        return createJob(title, type, description, requirements, positions, deadline, moId,
                null, null, null, null, null);
    }

    public Job createJob(String title, String type, String description,
                         String requirements, int positions, String deadline, String moId,
                         String workStartDate, String workEndDate, String workWeekdays,
                         String dailyStartHour, String dailyEndHour) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Job title cannot be empty");
        }
        if (requirements == null || requirements.trim().isEmpty()) {
            throw new IllegalArgumentException("Requirements cannot be empty");
        }
        if (isBlank(workStartDate) || isBlank(workEndDate) || isBlank(workWeekdays)
                || isBlank(dailyStartHour) || isBlank(dailyEndHour)) {
            throw new IllegalArgumentException("Work schedule fields are required");
        }
        if (positions <= 0) {
            throw new IllegalArgumentException("Positions must be greater than 0");
        }
        LocalDate startDate = parseDate(workStartDate, "Invalid work start date");
        LocalDate endDate = parseDate(workEndDate, "Invalid work end date");
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Work start date must be on or before work end date");
        }
        LocalTime startHour = parseHour(dailyStartHour, "Invalid daily start hour");
        LocalTime endHour = parseHour(dailyEndHour, "Invalid daily end hour");
        if (!startHour.isBefore(endHour)) {
            throw new IllegalArgumentException("Daily start hour must be earlier than daily end hour");
        }
        String normalizedWeekdays = normalizeWeekdays(workWeekdays);
        if (normalizedWeekdays.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one weekday");
        }

        Job job = new Job();
        job.setJobId(JsonUtil.generateId("j"));
        job.setTitle(title.trim());
        job.setType(type != null ? type.toUpperCase() : "OTHER");
        job.setDescription(description);
        job.setRequirements(requirements.trim());
        job.setWorkStartDate(startDate.toString());
        job.setWorkEndDate(endDate.toString());
        job.setWorkWeekdays(normalizedWeekdays);
        job.setDailyStartHour(startHour.toString());
        job.setDailyEndHour(endHour.toString());
        job.setPositions(positions);
        job.setPostedBy(moId);
        job.setDeadline(deadline);
        job.setStatus("OPEN");

        jobDAO.addJob(job);
        return job;
    }

    private String normalizeWeekdays(String weekdaysRaw) {
        if (isBlank(weekdaysRaw)) {
            return "";
        }
        Set<String> valid = new LinkedHashSet<>(Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"));
        Set<String> selected = new LinkedHashSet<>();
        String[] values = weekdaysRaw.split("[,\\s]+");
        for (String value : values) {
            String code = value == null ? "" : value.trim().toUpperCase();
            if (valid.contains(code)) {
                selected.add(code);
            }
        }
        return String.join(",", selected);
    }

    private LocalDate parseDate(String raw, String error) {
        try {
            return LocalDate.parse(raw.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(error);
        }
    }

    private LocalTime parseHour(String raw, String error) {
        try {
            LocalTime time = LocalTime.parse(raw.trim());
            if (time.getMinute() != 0 || time.getSecond() != 0 || time.getNano() != 0) {
                throw new IllegalArgumentException(error + " (hour precision required)");
            }
            return time;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(error);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
