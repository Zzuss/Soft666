package com.tarecruitment.servlet;

import com.tarecruitment.model.User;
import com.tarecruitment.service.AuthService;
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

    @Override
    public void init() throws ServletException {
        this.userService = new UserService();
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
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !currentUser.isTA()) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || "/".equals(pathInfo)) {
            showProfilePage(request, response);
            return;
        }

        if ("/resume/download".equals(pathInfo)) {
            downloadResume(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/profile");
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
            updateProfile(request, response);
            return;
        }
        if ("/resume/upload".equals(pathInfo)) {
            uploadResume(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void showProfilePage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User sessionUser = (User) request.getSession(false).getAttribute("user");
        User latestUser = userService.getUserById(sessionUser.getUserId());
        if (latestUser == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }
        request.getSession(false).setAttribute("user", latestUser);
        request.setAttribute("profileUser", latestUser);
        request.getRequestDispatcher("/jsp/profile/edit.jsp").forward(request, response);
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User sessionUser = (User) request.getSession(false).getAttribute("user");

        String name = trimOrEmpty(request.getParameter("name"));
        String email = trimOrEmpty(request.getParameter("email"));
        String skillsRaw = request.getParameter("skills");
        String availableTime = trimOrEmpty(request.getParameter("availableTime"));
        String bio = trimOrEmpty(request.getParameter("bio"));

        if (name.isEmpty() || email.isEmpty()) {
            redirectWithMessage(request, response, "error", "Name and email are required");
            return;
        }

        List<String> skills = parseSkills(skillsRaw);
        userService.updateProfile(sessionUser.getUserId(), name, email, skills, availableTime, bio);

        User latestUser = userService.getUserById(sessionUser.getUserId());
        request.getSession(false).setAttribute("user", latestUser);
        request.getSession(false).setAttribute("name", latestUser.getName());

        redirectWithMessage(request, response, "success", "Profile updated successfully");
    }

    private void uploadResume(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        User sessionUser = (User) request.getSession(false).getAttribute("user");
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
                + File.separator + sessionUser.getUserId();
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

        String relativePath = "resumes/" + sessionUser.getUserId() + "/" + storedFileName;
        userService.updateResume(sessionUser.getUserId(), originalFileName, relativePath);

        User latestUser = userService.getUserById(sessionUser.getUserId());
        request.getSession(false).setAttribute("user", latestUser);

        redirectWithMessage(request, response, "success", "Resume uploaded successfully");
    }

    private void downloadResume(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User sessionUser = (User) request.getSession(false).getAttribute("user");
        User latestUser = userService.getUserById(sessionUser.getUserId());
        if (latestUser == null || latestUser.getResumePath() == null || latestUser.getResumePath().isEmpty()) {
            redirectWithMessage(request, response, "error", "No resume found");
            return;
        }

        File resumeFile = new File(JsonUtil.getDataDirectoryPath(), latestUser.getResumePath());
        if (!resumeFile.exists() || !resumeFile.isFile()) {
            redirectWithMessage(request, response, "error", "Resume file does not exist");
            return;
        }

        String fileName = latestUser.getResumeFileName() != null && !latestUser.getResumeFileName().isEmpty()
                ? latestUser.getResumeFileName()
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

    private List<String> parseSkills(String skillsRaw) {
        List<String> skills = new ArrayList<>();
        if (skillsRaw == null || skillsRaw.trim().isEmpty()) {
            return skills;
        }
        String[] parts = skillsRaw.split(",");
        for (String part : parts) {
            String skill = part.trim();
            if (!skill.isEmpty() && !skills.contains(skill)) {
                skills.add(skill);
            }
        }
        return skills;
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
