package com.tarecruitment.servlet;

import com.tarecruitment.model.Job;
import com.tarecruitment.model.User;
import com.tarecruitment.service.AuthService;
import com.tarecruitment.service.JobService;
import com.tarecruitment.service.MatchingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/jobs/*")
public class JobServlet extends HttpServlet {
    private JobService jobService;
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        this.jobService = new JobService();
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!authService.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
            listJobs(request, response);
        } else if (pathInfo.equals("/create")) {
            showCreateForm(request, response);
        } else if (pathInfo.equals("/edit")) {
            showEditForm(request, response);
        } else if (pathInfo.equals("/detail")) {
            showJobDetail(request, response);
        } else if (pathInfo.equals("/myjobs")) {
            listMyJobs(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/jobs/list");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!authService.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        String pathInfo = request.getPathInfo();

        if ("/create".equals(pathInfo)) {
            createJob(request, response);
        } else if ("/update".equals(pathInfo)) {
            updateJob(request, response);
        } else if ("/close".equals(pathInfo)) {
            closeJob(request, response);
        } else if ("/delete".equals(pathInfo)) {
            deleteJob(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/jobs/list");
        }
    }

    private void listJobs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession(false).getAttribute("user");
        String keyword = request.getParameter("keyword");
        String type = request.getParameter("type");
        String[] skillParams = request.getParameterValues("skills");
        List<String> selectedSkills = normalizeSelectedSkills(skillParams);
        boolean hasActiveFilter = hasText(keyword)
                || (hasText(type) && !"ALL".equalsIgnoreCase(type.trim()))
                || !selectedSkills.isEmpty();

        List<Job> jobs = jobService.searchJobs(keyword, type, selectedSkills);
        if (!hasActiveFilter && user != null && user.isTA()) {
            List<JobService.JobRecommendation> recommendations = jobService.getRecommendedJobsForUser(user, 5);
            request.setAttribute("recommendedJobs", recommendations);
        }
        request.setAttribute("jobs", jobs);
        request.setAttribute("keyword", keyword != null ? keyword.trim() : "");
        request.setAttribute("type", type != null ? type.trim() : "");
        request.setAttribute("selectedSkills", selectedSkills);
        request.setAttribute("hasActiveFilter", hasActiveFilter);
        request.setAttribute("availableSkills", jobService.getSupportedSkills());
        request.getRequestDispatcher("/jsp/jobs/list.jsp").forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession(false).getAttribute("user");
        if (!user.isMO() && !user.isAdmin()) {
            request.setAttribute("error", "Only MO or Admin can create jobs");
            request.getRequestDispatcher("/jsp/jobs/list.jsp").forward(request, response);
            return;
        }
        request.setAttribute("editMode", false);
        request.getRequestDispatcher("/jsp/jobs/create.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession(false).getAttribute("user");
        String jobId = request.getParameter("id");
        Job job = jobService.getJobById(jobId);
        if (job == null) {
            response.sendRedirect(request.getContextPath() + "/jobs/myjobs?error=Job not found");
            return;
        }
        if (!canManageJob(user, job)) {
            response.sendRedirect(request.getContextPath() + "/jobs/myjobs?error=No permission to edit this job");
            return;
        }
        request.setAttribute("editMode", true);
        request.setAttribute("job", job);
        request.getRequestDispatcher("/jsp/jobs/create.jsp").forward(request, response);
    }

    private void showJobDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession(false).getAttribute("user");
        String jobId = request.getParameter("id");
        Job job = jobService.getJobById(jobId);
        
        if (job == null) {
            response.sendRedirect(request.getContextPath() + "/jobs/list");
            return;
        }

        if (user != null && user.isTA()) {
            MatchingService.MatchResult matchResult = jobService.evaluateEnhancedMatchForUser(job, user);
            request.setAttribute("currentUserMatchScore", matchResult.getScore());
            request.setAttribute("currentUserMissingSkills", matchResult.getMissingSkills());
            request.setAttribute("currentUserMatchSource", matchResult.getSource());
            request.setAttribute("currentUserMatchExplanation", matchResult.getExplanation());
        }

        request.setAttribute("job", job);
        request.getRequestDispatcher("/jsp/jobs/detail.jsp").forward(request, response);
    }

    private void listMyJobs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession(false).getAttribute("user");
        List<Job> jobs = jobService.getJobsByMo(user.getUserId());
        request.setAttribute("jobs", jobs);
        request.getRequestDispatcher("/jsp/jobs/myjobs.jsp").forward(request, response);
    }

    private void createJob(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = (User) request.getSession(false).getAttribute("user");
            
            String title = request.getParameter("title");
            String type = request.getParameter("type");
            String description = request.getParameter("description");
            String requirements = request.getParameter("requirements");
            String courseCode = request.getParameter("courseCode");
            String requiredSkills = request.getParameter("requiredSkills");
            String workStartDate = request.getParameter("workStartDate");
            String workEndDate = request.getParameter("workEndDate");
            String[] workWeekdays = request.getParameterValues("workWeekdays");
            String dailyStartHour = request.getParameter("dailyStartHour");
            String dailyEndHour = request.getParameter("dailyEndHour");
            int positions = parsePositions(request.getParameter("positions"));
            String deadline = request.getParameter("deadline");

            jobService.createJob(
                    title, type, description, requirements, positions, deadline, user.getUserId(),
                    courseCode, requiredSkills,
                    workStartDate, workEndDate, joinValues(workWeekdays), dailyStartHour, dailyEndHour
            );
            
            response.sendRedirect(request.getContextPath() + "/jobs/myjobs?success=Job created successfully");
        } catch (Exception e) {
            forwardJobFormWithErrors(request, response, false, null, e.getMessage());
        }
    }

    private void updateJob(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String jobId = request.getParameter("id");
        try {
            User user = (User) request.getSession(false).getAttribute("user");
            Job existing = jobService.getJobById(jobId);
            if (existing == null) {
                response.sendRedirect(request.getContextPath() + "/jobs/myjobs?error=Job not found");
                return;
            }
            if (!canManageJob(user, existing)) {
                response.sendRedirect(request.getContextPath() + "/jobs/myjobs?error=No permission to update this job");
                return;
            }

            String title = request.getParameter("title");
            String type = request.getParameter("type");
            String description = request.getParameter("description");
            String requirements = request.getParameter("requirements");
            String courseCode = request.getParameter("courseCode");
            String requiredSkills = request.getParameter("requiredSkills");
            String workStartDate = request.getParameter("workStartDate");
            String workEndDate = request.getParameter("workEndDate");
            String[] workWeekdays = request.getParameterValues("workWeekdays");
            String dailyStartHour = request.getParameter("dailyStartHour");
            String dailyEndHour = request.getParameter("dailyEndHour");
            int positions = parsePositions(request.getParameter("positions"));
            String deadline = request.getParameter("deadline");

            Job updated = new Job();
            updated.setJobId(jobId);
            updated.setTitle(title);
            updated.setCourseCode(courseCode);
            updated.setType(type);
            updated.setDescription(description);
            updated.setRequirements(requirements);
            updated.setRequiredSkills(requiredSkills);
            updated.setWorkStartDate(workStartDate);
            updated.setWorkEndDate(workEndDate);
            updated.setWorkWeekdays(joinValues(workWeekdays));
            updated.setDailyStartHour(dailyStartHour);
            updated.setDailyEndHour(dailyEndHour);
            updated.setPositions(positions);
            updated.setDeadline(deadline);
            updated.setPostedBy(existing.getPostedBy());
            updated.setStatus(existing.getStatus());
            updated.setCreatedAt(existing.getCreatedAt());

            jobService.updateJob(updated);
            response.sendRedirect(request.getContextPath() + "/jobs/myjobs?success=Job updated successfully");
        } catch (Exception e) {
            Job job = jobService.getJobById(jobId);
            forwardJobFormWithErrors(request, response, true, job, e.getMessage());
        }
    }

    private void closeJob(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession(false).getAttribute("user");
        try {
            String jobId = request.getParameter("id");
            Job job = jobService.getJobById(jobId);
            if (job == null) {
                response.sendRedirect(request.getContextPath() + "/jobs/myjobs?error=Job not found");
                return;
            }
            if (!canManageJob(user, job)) {
                response.sendRedirect(request.getContextPath() + "/jobs/myjobs?error=No permission to close this job");
                return;
            }
            jobService.closeJob(jobId);
            response.sendRedirect(request.getContextPath() + "/jobs/myjobs?success=Job closed successfully");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/jobs/myjobs?error=" + e.getMessage());
        }
    }

    private void deleteJob(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession(false).getAttribute("user");
        try {
            String jobId = request.getParameter("id");
            Job job = jobService.getJobById(jobId);
            if (job == null) {
                response.sendRedirect(request.getContextPath() + "/jobs/myjobs?error=Job not found");
                return;
            }
            if (!canManageJob(user, job)) {
                response.sendRedirect(request.getContextPath() + "/jobs/myjobs?error=No permission to delete this job");
                return;
            }
            jobService.deleteJob(jobId);
            response.sendRedirect(request.getContextPath() + "/jobs/myjobs?success=Job deleted successfully");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/jobs/myjobs?error=" + e.getMessage());
        }
    }

    private String joinValues(String[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        return String.join(",", values);
    }

    private int parsePositions(String rawPositions) {
        if (!hasText(rawPositions)) {
            throw new IllegalArgumentException("Positions must be greater than 0");
        }
        try {
            return Integer.parseInt(rawPositions.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Positions must be greater than 0");
        }
    }

    private void forwardJobFormWithErrors(HttpServletRequest request, HttpServletResponse response,
                                          boolean editMode, Job existingJob, String errorMessage)
            throws ServletException, IOException {
        request.setAttribute("error", errorMessage);
        request.setAttribute("editMode", editMode);
        if (existingJob != null) {
            request.setAttribute("job", existingJob);
        }
        preserveSubmittedFormValues(request);
        clearInvalidSubmittedFields(request, errorMessage);
        request.getRequestDispatcher("/jsp/jobs/create.jsp").forward(request, response);
    }

    private void preserveSubmittedFormValues(HttpServletRequest request) {
        request.setAttribute("formTitle", valueOrEmpty(request.getParameter("title")));
        request.setAttribute("formCourseCode", valueOrEmpty(request.getParameter("courseCode")));
        request.setAttribute("formType", valueOrEmpty(request.getParameter("type")));
        request.setAttribute("formDescription", valueOrEmpty(request.getParameter("description")));
        request.setAttribute("formRequirements", valueOrEmpty(request.getParameter("requirements")));
        request.setAttribute("formRequiredSkills", valueOrEmpty(request.getParameter("requiredSkills")));
        request.setAttribute("formWorkStartDate", valueOrEmpty(request.getParameter("workStartDate")));
        request.setAttribute("formWorkEndDate", valueOrEmpty(request.getParameter("workEndDate")));
        request.setAttribute("formDailyStartHour", valueOrEmpty(request.getParameter("dailyStartHour")));
        request.setAttribute("formDailyEndHour", valueOrEmpty(request.getParameter("dailyEndHour")));
        request.setAttribute("formPositions", valueOrEmpty(request.getParameter("positions")));
        request.setAttribute("formDeadline", valueOrEmpty(request.getParameter("deadline")));
        request.setAttribute("formWeekdays", normalizeSelectedSkills(request.getParameterValues("workWeekdays")));
    }

    private void clearInvalidSubmittedFields(HttpServletRequest request, String errorMessage) {
        if (!hasText(errorMessage)) {
            return;
        }
        if (errorMessage.contains("Job title")) {
            request.setAttribute("formTitle", "");
            return;
        }
        if (errorMessage.contains("Course code")) {
            request.setAttribute("formCourseCode", "");
            return;
        }
        if (errorMessage.contains("Requirements")) {
            request.setAttribute("formRequirements", "");
            return;
        }
        if (errorMessage.contains("Positions")) {
            request.setAttribute("formPositions", "");
            return;
        }
        if (errorMessage.contains("Invalid application deadline")) {
            request.setAttribute("formDeadline", "");
            return;
        }
        if (errorMessage.contains("Work schedule fields are required")) {
            request.setAttribute("formWorkStartDate", "");
            request.setAttribute("formWorkEndDate", "");
            request.setAttribute("formDailyStartHour", "");
            request.setAttribute("formDailyEndHour", "");
            request.setAttribute("formWeekdays", new ArrayList<String>());
            return;
        }
        if (errorMessage.contains("Invalid work start date")) {
            request.setAttribute("formWorkStartDate", "");
            return;
        }
        if (errorMessage.contains("Invalid work end date")) {
            request.setAttribute("formWorkEndDate", "");
            return;
        }
        if (errorMessage.contains("Work start date must be on or before work end date")) {
            request.setAttribute("formWorkStartDate", "");
            request.setAttribute("formWorkEndDate", "");
            return;
        }
        if (errorMessage.contains("Invalid daily start hour")) {
            request.setAttribute("formDailyStartHour", "");
            return;
        }
        if (errorMessage.contains("Invalid daily end hour")) {
            request.setAttribute("formDailyEndHour", "");
            return;
        }
        if (errorMessage.contains("Daily start hour must be earlier than daily end hour")) {
            request.setAttribute("formDailyStartHour", "");
            request.setAttribute("formDailyEndHour", "");
            return;
        }
        if (errorMessage.contains("Please select at least one weekday")) {
            request.setAttribute("formWeekdays", new ArrayList<String>());
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private List<String> normalizeSelectedSkills(String[] rawSkills) {
        List<String> selected = new ArrayList<>();
        if (rawSkills == null || rawSkills.length == 0) {
            return selected;
        }
        for (String raw : rawSkills) {
            if (raw == null) {
                continue;
            }
            String trimmed = raw.trim();
            if (!trimmed.isEmpty() && !selected.contains(trimmed)) {
                selected.add(trimmed);
            }
        }
        return selected;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean canManageJob(User user, Job job) {
        if (user == null || job == null) {
            return false;
        }
        if (user.isAdmin()) {
            return true;
        }
        return user.isMO() && user.getUserId().equals(job.getPostedBy());
    }
}
