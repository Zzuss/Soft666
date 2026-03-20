package com.tarecruitment.servlet;

import com.tarecruitment.model.Application;
import com.tarecruitment.model.Job;
import com.tarecruitment.model.User;
import com.tarecruitment.service.ApplicationService;
import com.tarecruitment.service.AuthService;
import com.tarecruitment.service.JobService;
import com.tarecruitment.service.UserService;
import com.tarecruitment.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/profile/*")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 6 * 1024 * 1024
)
public class ProfileServlet extends HttpServlet {
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".pdf", ".doc", ".docx");

    private UserService userService;
    private AuthService authService;
    private JobService jobService;
    private ApplicationService applicationService;

    @Override
    public void init() throws ServletException {
        this.userService = new UserService();
        this.authService = new AuthService();
        this.jobService = new JobService();
        this.applicationService = new ApplicationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!authService.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || "/".equals(pathInfo)) {
            if (!currentUser.isTA()) {
                response.sendRedirect(request.getContextPath() + "/dashboard");
                return;
            }
            showOwnProfilePage(request, response, currentUser);
            return;
        }

        if ("/view".equals(pathInfo)) {
            showApplicantProfilePage(request, response, currentUser);
            return;
        }

        if ("/resume/download".equals(pathInfo)) {
            downloadResume(request, response, currentUser);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!authService.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !currentUser.isTA()) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String pathInfo = request.getPathInfo();
        if ("/update".equals(pathInfo)) {
            updateProfile(request, response, currentUser);
            return;
        }
        if ("/resume/upload".equals(pathInfo)) {
            uploadResume(request, response, currentUser);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void showOwnProfilePage(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        User latestUser = userService.getUserById(currentUser.getUserId());
        if (latestUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }
        request.getSession(false).setAttribute("user", latestUser);
        request.setAttribute("profileUser", latestUser);
        request.getRequestDispatcher("/jsp/profile/edit.jsp").forward(request, response);
    }

    private void showApplicantProfilePage(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws ServletException, IOException {
        String targetUserId = request.getParameter("userId");
        String jobId = request.getParameter("jobId");
        User targetUser = resolveViewableUser(currentUser, targetUserId, jobId);
        if (targetUser == null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        request.setAttribute("profileUser", targetUser);
        request.setAttribute("jobId", jobId);
        request.getRequestDispatcher("/jsp/profile/view.jsp").forward(request, response);
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws IOException {
        String name = trimOrEmpty(request.getParameter("name"));
        String email = trimOrEmpty(request.getParameter("email"));
        String skillsRaw = request.getParameter("skills");
        String availableTime = trimOrEmpty(request.getParameter("availableTime"));
        String availabilityStartDate = trimOrEmpty(request.getParameter("availabilityStartDate"));
        String availabilityEndDate = trimOrEmpty(request.getParameter("availabilityEndDate"));
        String[] availabilityWeekdays = request.getParameterValues("availabilityWeekdays");
        String availabilityDailyStartHour = trimOrEmpty(request.getParameter("availabilityDailyStartHour"));
        String availabilityDailyEndHour = trimOrEmpty(request.getParameter("availabilityDailyEndHour"));
        String bio = trimOrEmpty(request.getParameter("bio"));

        if (name.isEmpty() || email.isEmpty()) {
            redirectWithMessage(request, response, "error", "Name and email are required");
            return;
        }
        if (availabilityStartDate.isEmpty() || availabilityEndDate.isEmpty()
                || availabilityWeekdays == null || availabilityWeekdays.length == 0
                || availabilityDailyStartHour.isEmpty() || availabilityDailyEndHour.isEmpty()) {
            redirectWithMessage(request, response, "error", "Availability schedule fields are required");
            return;
        }

        List<String> skills = parseSkills(skillsRaw);
        userService.updateProfile(
                currentUser.getUserId(),
                name,
                email,
                skills,
                availableTime,
                bio,
                availabilityStartDate,
                availabilityEndDate,
                joinValues(availabilityWeekdays),
                availabilityDailyStartHour,
                availabilityDailyEndHour
        );

        User latestUser = userService.getUserById(currentUser.getUserId());
        request.getSession(false).setAttribute("user", latestUser);
        request.getSession(false).setAttribute("name", latestUser.getName());

        redirectWithMessage(request, response, "success", "Profile updated successfully");
    }

    private void uploadResume(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws IOException, ServletException {
        Part filePart = request.getPart("resume");
        if (filePart == null || filePart.getSize() <= 0) {
            redirectWithMessage(request, response, "error", "Please choose a resume file");
            return;
        }

        String originalFileName = filePart.getSubmittedFileName();
        String extension = getExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            redirectWithMessage(request, response, "error", "Only PDF/DOC/DOCX files are allowed");
            return;
        }

        String resumeDirPath = JsonUtil.getDataDirectoryPath() + File.separator + "resumes"
                + File.separator + currentUser.getUserId();
        File resumeDir = new File(resumeDirPath);
        if (!resumeDir.exists() && !resumeDir.mkdirs()) {
            redirectWithMessage(request, response, "error", "Failed to create resume directory");
            return;
        }

        String storedFileName = "resume_" + System.currentTimeMillis() + extension.toLowerCase();
        File storedFile = new File(resumeDir, storedFileName);
        try (InputStream inputStream = filePart.getInputStream()) {
            Files.copy(inputStream, storedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        String relativePath = "resumes/" + currentUser.getUserId() + "/" + storedFileName;
        userService.updateResume(currentUser.getUserId(), originalFileName, relativePath);

        User latestUser = userService.getUserById(currentUser.getUserId());
        request.getSession(false).setAttribute("user", latestUser);

        redirectWithMessage(request, response, "success", "Resume uploaded successfully");
    }

    private void downloadResume(HttpServletRequest request, HttpServletResponse response, User currentUser)
            throws IOException {
        String targetUserId = request.getParameter("userId");
        String jobId = request.getParameter("jobId");
        if (targetUserId == null || targetUserId.trim().isEmpty()) {
            targetUserId = currentUser.getUserId();
        }

        User targetUser = resolveViewableUser(currentUser, targetUserId, jobId);
        if (targetUser == null || targetUser.getResumePath() == null || targetUser.getResumePath().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        File resumeFile = new File(JsonUtil.getDataDirectoryPath(), targetUser.getResumePath());
        if (!resumeFile.exists() || !resumeFile.isFile()) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String fileName = targetUser.getResumeFileName() != null && !targetUser.getResumeFileName().isEmpty()
                ? targetUser.getResumeFileName()
                : resumeFile.getName();
        String contentType = getContentType(fileName);

        response.setContentType(contentType);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8)
        );
        response.setContentLengthLong(resumeFile.length());
        Files.copy(resumeFile.toPath(), response.getOutputStream());
    }

    private User resolveViewableUser(User currentUser, String targetUserId, String jobId) {
        if (targetUserId == null || targetUserId.trim().isEmpty()) {
            return null;
        }
        User targetUser = userService.getUserById(targetUserId);
        if (targetUser == null) {
            return null;
        }

        if (currentUser.isAdmin()) {
            return targetUser;
        }
        if (currentUser.isTA()) {
            return currentUser.getUserId().equals(targetUserId) ? targetUser : null;
        }
        if (currentUser.isMO()) {
            if (jobId == null || jobId.trim().isEmpty()) {
                return null;
            }
            Job job = jobService.getJobById(jobId);
            if (job == null || !currentUser.getUserId().equals(job.getPostedBy())) {
                return null;
            }
            Application app = applicationService.getApplication(jobId, targetUserId);
            return app != null ? targetUser : null;
        }
        return null;
    }

    private List<String> parseSkills(String skillsRaw) {
        List<String> skills = new ArrayList<>();
        if (skillsRaw == null || skillsRaw.trim().isEmpty()) {
            return skills;
        }
        String[] parts = skillsRaw.split("[,，;；/／\\\\|\\n\\r]+");
        for (String part : parts) {
            String skill = part.trim();
            if (!skill.isEmpty() && !skills.contains(skill)) {
                skills.add(skill);
            }
        }
        return skills;
    }

    private String joinValues(String[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        return String.join(",", values);
    }

    private String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String getExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx >= fileName.length() - 1) {
            return "";
        }
        return fileName.substring(idx);
    }

    private String getContentType(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }
        if (lower.endsWith(".doc")) {
            return "application/msword";
        }
        if (lower.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return "application/octet-stream";
    }

    private void redirectWithMessage(HttpServletRequest request, HttpServletResponse response,
                                     String key, String message) throws IOException {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/profile?" + key + "=" + encoded);
    }
}
