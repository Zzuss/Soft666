package com.tarecruitment.service;

import com.tarecruitment.dao.JobDAO;
import com.tarecruitment.model.Job;
import com.tarecruitment.util.JsonUtil;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class JobService {
    private static final List<String> SUPPORTED_SKILLS = Arrays.asList(
            "Java", "Python", "SQL", "JavaScript", "React", "Grading",
            "Teaching", "Communication", "Teamwork", "Invigilation"
    );

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
        JobValidatedData validated = validateJobFields(
                title, type, description, requirements, positions, deadline,
                workStartDate, workEndDate, workWeekdays, dailyStartHour, dailyEndHour
        );

        Job job = new Job();
        job.setJobId(JsonUtil.generateId("j"));
        applyValidatedFields(job, validated);
        job.setPostedBy(moId);
        job.setStatus("OPEN");
        job.setCreatedAt(new Timestamp(System.currentTimeMillis()));

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
        JobValidatedData validated = validateJobFields(
                job.getTitle(), job.getType(), job.getDescription(), job.getRequirements(),
                job.getPositions(), job.getDeadline(), job.getWorkStartDate(), job.getWorkEndDate(),
                job.getWorkWeekdays(), job.getDailyStartHour(), job.getDailyEndHour()
        );
        applyValidatedFields(job, validated);
        if (isBlank(job.getPostedBy())) {
            job.setPostedBy(existingJob.getPostedBy());
        }
        if (isBlank(job.getStatus())) {
            job.setStatus(existingJob.getStatus());
        }
        if (job.getCreatedAt() == null) {
            job.setCreatedAt(existingJob.getCreatedAt());
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
        return searchJobs(keyword, type, null);
    }

    public List<Job> searchJobs(String keyword, String type, List<String> skills) {
        List<Job> jobs;
        
        if (type != null && !type.isEmpty() && !type.equalsIgnoreCase("ALL")) {
            jobs = jobDAO.getJobsByType(type);
        } else {
            jobs = jobDAO.getOpenJobs();
        }

        if (skills != null && !skills.isEmpty()) {
            jobs = new ArrayList<>(jobs);
            jobs.removeIf(job -> !matchesAnySkill(job, skills));
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String trimmed = keyword.trim().toLowerCase(Locale.ROOT);
            jobs = new ArrayList<>(jobs);
            jobs.removeIf(job -> !matchesKeyword(job, trimmed));
        }

        jobs.sort(Comparator.comparing(this::toSortableDeadline));
        return jobs;
    }

    public List<String> getSupportedSkills() {
        return new ArrayList<>(SUPPORTED_SKILLS);
    }

    private JobValidatedData validateJobFields(String title, String type, String description,
                                               String requirements, int positions, String deadline,
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

        if (!isBlank(deadline)) {
            parseDate(deadline, "Invalid application deadline");
        }

        JobValidatedData validated = new JobValidatedData();
        validated.title = title.trim();
        validated.type = type != null ? type.toUpperCase(Locale.ROOT) : "OTHER";
        validated.description = description != null ? description : "";
        validated.requirements = requirements.trim();
        validated.positions = positions;
        validated.deadline = deadline;
        validated.startDate = startDate.toString();
        validated.endDate = endDate.toString();
        validated.weekdays = normalizedWeekdays;
        validated.startHour = startHour.toString();
        validated.endHour = endHour.toString();
        return validated;
    }

    private void applyValidatedFields(Job job, JobValidatedData validated) {
        job.setTitle(validated.title);
        job.setType(validated.type);
        job.setDescription(validated.description);
        job.setRequirements(validated.requirements);
        job.setPositions(validated.positions);
        job.setDeadline(validated.deadline);
        job.setWorkStartDate(validated.startDate);
        job.setWorkEndDate(validated.endDate);
        job.setWorkWeekdays(validated.weekdays);
        job.setDailyStartHour(validated.startHour);
        job.setDailyEndHour(validated.endHour);
    }

    private boolean matchesAnySkill(Job job, List<String> skills) {
        String text = combinedJobText(job);
        for (String skill : skills) {
            if (skill == null || skill.trim().isEmpty()) {
                continue;
            }
            if (text.contains(skill.trim().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesKeyword(Job job, String keyword) {
        String text = combinedJobText(job);
        if (text.contains(keyword)) {
            return true;
        }
        String[] words = text.split("[^a-z0-9]+");
        String[] queryTokens = keyword.split("\\s+");
        for (String token : queryTokens) {
            String q = token.trim();
            if (q.isEmpty()) {
                continue;
            }
            boolean matchedToken = false;
            for (String word : words) {
                if (word.isEmpty()) {
                    continue;
                }
                if (word.contains(q) || (q.length() >= 3 && isWithinOneEdit(word, q))) {
                    matchedToken = true;
                    break;
                }
            }
            if (!matchedToken) {
                return false;
            }
        }
        return true;
    }

    private String combinedJobText(Job job) {
        StringBuilder sb = new StringBuilder();
        appendLower(sb, job.getTitle());
        appendLower(sb, job.getDescription());
        appendLower(sb, job.getRequirements());
        appendLower(sb, job.getType());
        return sb.toString();
    }

    private void appendLower(StringBuilder sb, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(' ');
        }
        sb.append(value.toLowerCase(Locale.ROOT));
    }

    private LocalDate toSortableDeadline(Job job) {
        if (job == null || isBlank(job.getDeadline())) {
            return LocalDate.MAX;
        }
        try {
            return LocalDate.parse(job.getDeadline().trim());
        } catch (Exception e) {
            return LocalDate.MAX;
        }
    }

    private boolean isWithinOneEdit(String source, String target) {
        int m = source.length();
        int n = target.length();
        if (Math.abs(m - n) > 1) {
            return false;
        }

        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= m; i++) {
            int rowMin = Integer.MAX_VALUE;
            for (int j = 1; j <= n; j++) {
                int cost = source.charAt(i - 1) == target.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
                rowMin = Math.min(rowMin, dp[i][j]);
            }
            if (rowMin > 1) {
                return false;
            }
        }
        return dp[m][n] <= 1;
    }

    private static class JobValidatedData {
        private String title;
        private String type;
        private String description;
        private String requirements;
        private int positions;
        private String deadline;
        private String startDate;
        private String endDate;
        private String weekdays;
        private String startHour;
        private String endHour;
    }
}
