<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.model.User" %>
<%@ page import="com.tarecruitment.util.I18nUtil" %>
<%
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }
    User profileUser = (User) request.getAttribute("profileUser");
    if (profileUser == null) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }
    String jobId = (String) request.getAttribute("jobId");
    String lang = I18nUtil.getLanguage(request);
    String profileWeekdaysDisplay = "-";
    if (profileUser.getAvailabilityWeekdays() != null && !profileUser.getAvailabilityWeekdays().isEmpty()) {
        String[] codes = profileUser.getAvailabilityWeekdays().split(",");
        java.util.List<String> labels = new java.util.ArrayList<>();
        for (String code : codes) {
            String c = code == null ? "" : code.trim();
            if (!c.isEmpty()) {
                labels.add(I18nUtil.getWeekdayDisplay(c, lang));
            }
        }
        if (!labels.isEmpty()) {
            profileWeekdaysDisplay = String.join(", ", labels);
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18nUtil.get("profile.viewTitle", lang) %> - <%= I18nUtil.get("app.title", lang) %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <nav class="navbar">
            <a href="${pageContext.request.contextPath}/dashboard" class="navbar-brand"><%= I18nUtil.get("app.title", lang) %></a>
            <div class="navbar-menu">
                <a href="${pageContext.request.contextPath}/dashboard"><%= I18nUtil.get("nav.dashboard", lang) %></a>
                <a href="${pageContext.request.contextPath}/jobs/list"><%= I18nUtil.get("nav.jobs", lang) %></a>
                <% if (currentUser.isAdmin()) { %>
                    <a href="${pageContext.request.contextPath}/admin/workload"><%= I18nUtil.get("nav.workload", lang) %></a>
                <% } else { %>
                    <a href="${pageContext.request.contextPath}/jobs/myjobs"><%= I18nUtil.get("nav.myPostedJobs", lang) %></a>
                <% } %>
                <span class="navbar-user"><%= currentUser.getName() %> (<%= currentUser.getRole() %>)</span>
                <a href="${pageContext.request.contextPath}/auth?action=logout" class="btn btn-secondary"><%= I18nUtil.get("nav.logout", lang) %></a>
                <jsp:include page="/jsp/common/language-switcher.jsp" />
            </div>
        </nav>

        <h2><%= I18nUtil.get("profile.viewTitle", lang) %></h2>

        <div class="card">
            <h3><%= profileUser.getName() %></h3>
            <div class="profile-section">
                <div class="profile-info">
                    <p><strong><%= I18nUtil.get("profile.email", lang) %>:</strong> <%= profileUser.getEmail() != null ? profileUser.getEmail() : "-" %></p>
                    <p><strong><%= I18nUtil.get("profile.studentId", lang) %>:</strong> <%= profileUser.getStudentId() != null && !profileUser.getStudentId().isEmpty() ? profileUser.getStudentId() : "-" %></p>
                    <p><strong><%= I18nUtil.get("profile.major", lang) %>:</strong> <%= profileUser.getMajor() != null && !profileUser.getMajor().isEmpty() ? profileUser.getMajor() : "-" %></p>
                    <p><strong><%= I18nUtil.get("profile.year", lang) %>:</strong> <%= profileUser.getYear() > 0 ? profileUser.getYear() : "-" %></p>
                    <p><strong><%= I18nUtil.get("profile.phone", lang) %>:</strong> <%= profileUser.getPhone() != null && !profileUser.getPhone().isEmpty() ? profileUser.getPhone() : "-" %></p>
                    <p><strong><%= I18nUtil.get("profile.availableTime", lang) %>:</strong> <%= (profileUser.getAvailableTime() != null && !profileUser.getAvailableTime().isEmpty()) ? profileUser.getAvailableTime() : "-" %></p>
                    <p><strong><%= I18nUtil.get("profile.availabilityStartDate", lang) %>:</strong> <%= (profileUser.getAvailabilityStartDate() != null && !profileUser.getAvailabilityStartDate().isEmpty()) ? profileUser.getAvailabilityStartDate() : "-" %></p>
                    <p><strong><%= I18nUtil.get("profile.availabilityEndDate", lang) %>:</strong> <%= (profileUser.getAvailabilityEndDate() != null && !profileUser.getAvailabilityEndDate().isEmpty()) ? profileUser.getAvailabilityEndDate() : "-" %></p>
                    <p><strong><%= I18nUtil.get("profile.availabilityWeekdays", lang) %>:</strong> <%= profileWeekdaysDisplay %></p>
                    <p><strong><%= I18nUtil.get("profile.availabilityDailyStartHour", lang) %>:</strong> <%= (profileUser.getAvailabilityDailyStartHour() != null && !profileUser.getAvailabilityDailyStartHour().isEmpty()) ? profileUser.getAvailabilityDailyStartHour() : "-" %></p>
                    <p><strong><%= I18nUtil.get("profile.availabilityDailyEndHour", lang) %>:</strong> <%= (profileUser.getAvailabilityDailyEndHour() != null && !profileUser.getAvailabilityDailyEndHour().isEmpty()) ? profileUser.getAvailabilityDailyEndHour() : "-" %></p>
                    <p><strong><%= I18nUtil.get("profile.skills", lang) %>:</strong>
                        <%= (profileUser.getSkills() != null && !profileUser.getSkills().isEmpty()) ? String.join(", ", profileUser.getSkills()) : "-" %>
                    </p>
                </div>
                <div class="profile-info">
                    <p><strong><%= I18nUtil.get("profile.bio", lang) %>:</strong></p>
                    <p><%= (profileUser.getBio() != null && !profileUser.getBio().isEmpty()) ? profileUser.getBio() : "-" %></p>
                </div>
            </div>
        </div>

        <div class="card">
            <h3><%= I18nUtil.get("profile.resume", lang) %></h3>
            <% if (profileUser.getResumePath() != null && !profileUser.getResumePath().isEmpty()) { %>
                <p><strong><%= I18nUtil.get("profile.currentResume", lang) %>:</strong> <%= profileUser.getResumeFileName() %></p>
                <a href="${pageContext.request.contextPath}/profile/resume/download?userId=<%= profileUser.getUserId() %>&jobId=<%= jobId != null ? jobId : "" %>" class="btn btn-primary"><%= I18nUtil.get("profile.downloadResume", lang) %></a>
            <% } else { %>
                <p style="color: #718096;"><%= I18nUtil.get("profile.noResume", lang) %></p>
            <% } %>
        </div>

        <div style="margin-top: 20px;">
            <% if (jobId != null && !jobId.isEmpty()) { %>
                <a href="${pageContext.request.contextPath}/applications/manage?jobId=<%= jobId %>" class="btn btn-secondary"><%= I18nUtil.get("profile.backToManage", lang) %></a>
            <% } else { %>
                <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-secondary"><%= I18nUtil.get("job.detail.back", lang) %></a>
            <% } %>
        </div>
    </div>
</body>
</html>
