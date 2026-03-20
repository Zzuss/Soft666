<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.model.User" %>
<%@ page import="com.tarecruitment.util.I18nUtil" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }
    User profileUser = (User) request.getAttribute("profileUser");
    if (profileUser == null) {
        profileUser = user;
    }
    String lang = I18nUtil.getLanguage(request);
    String skillsText = "";
    List<String> skills = profileUser.getSkills();
    if (skills != null && !skills.isEmpty()) {
        skillsText = String.join(", ", skills);
    }
    Set<String> selectedWeekdays = new HashSet<>();
    if (profileUser.getAvailabilityWeekdays() != null && !profileUser.getAvailabilityWeekdays().isEmpty()) {
        String[] values = profileUser.getAvailabilityWeekdays().split(",");
        for (String value : values) {
            String code = value == null ? "" : value.trim().toUpperCase();
            if (!code.isEmpty()) {
                selectedWeekdays.add(code);
            }
        }
    }
    String availabilityStartHourValue = profileUser.getAvailabilityDailyStartHour() != null ? profileUser.getAvailabilityDailyStartHour() : "";
    if (availabilityStartHourValue.length() >= 5) {
        availabilityStartHourValue = availabilityStartHourValue.substring(0, 5);
    }
    String availabilityEndHourValue = profileUser.getAvailabilityDailyEndHour() != null ? profileUser.getAvailabilityDailyEndHour() : "";
    if (availabilityEndHourValue.length() >= 5) {
        availabilityEndHourValue = availabilityEndHourValue.substring(0, 5);
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18nUtil.get("profile.title", lang) %> - <%= I18nUtil.get("app.title", lang) %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <nav class="navbar">
            <a href="${pageContext.request.contextPath}/dashboard" class="navbar-brand"><%= I18nUtil.get("app.title", lang) %></a>
            <div class="navbar-menu">
                <a href="${pageContext.request.contextPath}/dashboard"><%= I18nUtil.get("nav.dashboard", lang) %></a>
                <a href="${pageContext.request.contextPath}/jobs/list"><%= I18nUtil.get("nav.jobs", lang) %></a>
                <% if (user.isTA()) { %>
                    <a href="${pageContext.request.contextPath}/applications/my"><%= I18nUtil.get("nav.myApplications", lang) %></a>
                    <a href="${pageContext.request.contextPath}/profile"><%= I18nUtil.get("nav.profile", lang) %></a>
                <% } else if (user.isMO()) { %>
                    <a href="${pageContext.request.contextPath}/jobs/myjobs"><%= I18nUtil.get("nav.myPostedJobs", lang) %></a>
                <% } else if (user.isAdmin()) { %>
                    <a href="${pageContext.request.contextPath}/admin/workload"><%= I18nUtil.get("nav.workload", lang) %></a>
                <% } %>
                <span class="navbar-user"><%= user.getName() %> (<%= user.getRole() %>)</span>
                <a href="${pageContext.request.contextPath}/auth?action=logout" class="btn btn-secondary"><%= I18nUtil.get("nav.logout", lang) %></a>
                <jsp:include page="/jsp/common/language-switcher.jsp" />
            </div>
        </nav>

        <h2><%= I18nUtil.get("profile.title", lang) %></h2>

        <% if (request.getParameter("error") != null) { %>
            <div class="alert alert-error"><%= request.getParameter("error") %></div>
        <% } %>
        <% if (request.getParameter("success") != null) { %>
            <div class="alert alert-success"><%= request.getParameter("success") %></div>
        <% } %>

        <div class="card">
            <h3><%= I18nUtil.get("profile.basicInfo", lang) %></h3>
            <form action="${pageContext.request.contextPath}/profile/update" method="post">
                <div class="form-group">
                    <label for="name"><%= I18nUtil.get("profile.name", lang) %></label>
                    <input type="text" id="name" name="name" required value="<%= profileUser.getName() != null ? profileUser.getName() : "" %>">
                </div>

                <div class="form-group">
                    <label for="email"><%= I18nUtil.get("profile.email", lang) %></label>
                    <input type="email" id="email" name="email" required value="<%= profileUser.getEmail() != null ? profileUser.getEmail() : "" %>">
                </div>

                <div class="form-group">
                    <label for="skills"><%= I18nUtil.get("profile.skills", lang) %></label>
                    <input type="text" id="skills" name="skills" placeholder="Java, SQL, Python" value="<%= skillsText %>">
                </div>

                <div class="form-group">
                    <label for="availableTime"><%= I18nUtil.get("profile.availableTime", lang) %></label>
                    <input type="text" id="availableTime" name="availableTime" readonly value="<%= profileUser.getAvailableTime() != null ? profileUser.getAvailableTime() : "" %>">
                </div>

                <div class="form-group">
                    <label for="availabilityStartDate"><%= I18nUtil.get("profile.availabilityStartDate", lang) %> *</label>
                    <input type="date" id="availabilityStartDate" name="availabilityStartDate" required value="<%= profileUser.getAvailabilityStartDate() != null ? profileUser.getAvailabilityStartDate() : "" %>">
                </div>

                <div class="form-group">
                    <label for="availabilityEndDate"><%= I18nUtil.get("profile.availabilityEndDate", lang) %> *</label>
                    <input type="date" id="availabilityEndDate" name="availabilityEndDate" required value="<%= profileUser.getAvailabilityEndDate() != null ? profileUser.getAvailabilityEndDate() : "" %>">
                </div>

                <div class="form-group">
                    <label><%= I18nUtil.get("profile.availabilityWeekdays", lang) %> *</label>
                    <div style="display: flex; flex-wrap: wrap; gap: 10px;">
                        <label><input type="checkbox" name="availabilityWeekdays" value="MON" <%= selectedWeekdays.contains("MON") ? "checked" : "" %>> <%= I18nUtil.getWeekdayDisplay("MON", lang) %></label>
                        <label><input type="checkbox" name="availabilityWeekdays" value="TUE" <%= selectedWeekdays.contains("TUE") ? "checked" : "" %>> <%= I18nUtil.getWeekdayDisplay("TUE", lang) %></label>
                        <label><input type="checkbox" name="availabilityWeekdays" value="WED" <%= selectedWeekdays.contains("WED") ? "checked" : "" %>> <%= I18nUtil.getWeekdayDisplay("WED", lang) %></label>
                        <label><input type="checkbox" name="availabilityWeekdays" value="THU" <%= selectedWeekdays.contains("THU") ? "checked" : "" %>> <%= I18nUtil.getWeekdayDisplay("THU", lang) %></label>
                        <label><input type="checkbox" name="availabilityWeekdays" value="FRI" <%= selectedWeekdays.contains("FRI") ? "checked" : "" %>> <%= I18nUtil.getWeekdayDisplay("FRI", lang) %></label>
                        <label><input type="checkbox" name="availabilityWeekdays" value="SAT" <%= selectedWeekdays.contains("SAT") ? "checked" : "" %>> <%= I18nUtil.getWeekdayDisplay("SAT", lang) %></label>
                        <label><input type="checkbox" name="availabilityWeekdays" value="SUN" <%= selectedWeekdays.contains("SUN") ? "checked" : "" %>> <%= I18nUtil.getWeekdayDisplay("SUN", lang) %></label>
                    </div>
                </div>

                <div class="form-group">
                    <label for="availabilityDailyStartHour"><%= I18nUtil.get("profile.availabilityDailyStartHour", lang) %> *</label>
                    <select id="availabilityDailyStartHour" name="availabilityDailyStartHour" required>
                        <option value=""><%= "zh".equals(lang) ? "请选择整点" : "Select hour" %></option>
                        <% for (int h = 0; h < 24; h++) {
                            String hourValue = String.format("%02d:00", h);
                        %>
                            <option value="<%= hourValue %>" <%= hourValue.equals(availabilityStartHourValue) ? "selected" : "" %>><%= hourValue %></option>
                        <% } %>
                    </select>
                </div>

                <div class="form-group">
                    <label for="availabilityDailyEndHour"><%= I18nUtil.get("profile.availabilityDailyEndHour", lang) %> *</label>
                    <select id="availabilityDailyEndHour" name="availabilityDailyEndHour" required>
                        <option value=""><%= "zh".equals(lang) ? "请选择整点" : "Select hour" %></option>
                        <% for (int h = 0; h < 24; h++) {
                            String hourValue = String.format("%02d:00", h);
                        %>
                            <option value="<%= hourValue %>" <%= hourValue.equals(availabilityEndHourValue) ? "selected" : "" %>><%= hourValue %></option>
                        <% } %>
                    </select>
                </div>

                <div class="form-group">
                    <label for="bio"><%= I18nUtil.get("profile.bio", lang) %></label>
                    <textarea id="bio" name="bio" rows="4" placeholder="<%= I18nUtil.get("profile.bioPlaceholder", lang) %>"><%= profileUser.getBio() != null ? profileUser.getBio() : "" %></textarea>
                </div>

                <button type="submit" class="btn btn-primary"><%= I18nUtil.get("profile.update", lang) %></button>
            </form>
        </div>

        <div class="card">
            <h3><%= I18nUtil.get("profile.resume", lang) %></h3>
            <p style="color: #718096; margin-bottom: 12px;"><%= I18nUtil.get("profile.resumeHint", lang) %></p>
            <form action="${pageContext.request.contextPath}/profile/resume/upload" method="post" enctype="multipart/form-data">
                <div class="form-group">
                    <input type="file" name="resume" accept=".pdf,.doc,.docx" required>
                </div>
                <button type="submit" class="btn btn-primary"><%= I18nUtil.get("profile.uploadResume", lang) %></button>
            </form>

            <div style="margin-top: 16px;">
                <% if (profileUser.getResumePath() != null && !profileUser.getResumePath().isEmpty()) { %>
                    <p><strong><%= I18nUtil.get("profile.currentResume", lang) %>:</strong> <%= profileUser.getResumeFileName() %></p>
                    <p style="color: #718096; font-size: 13px;">
                        <%= I18nUtil.get("profile.updatedAt", lang) %>:
                        <%= profileUser.getResumeUpdatedAt() != null ? profileUser.getResumeUpdatedAt().toString().substring(0, 19) : "-" %>
                    </p>
                    <a href="${pageContext.request.contextPath}/profile/resume/download" class="btn btn-secondary"><%= I18nUtil.get("profile.downloadResume", lang) %></a>
                <% } else { %>
                    <p style="color: #718096;"><%= I18nUtil.get("profile.noResume", lang) %></p>
                <% } %>
            </div>
        </div>
    </div>
</body>
</html>
