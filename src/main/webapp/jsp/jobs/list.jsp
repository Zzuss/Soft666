<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.model.User" %>
<%@ page import="com.tarecruitment.model.Job" %>
<%@ page import="com.tarecruitment.util.I18nUtil" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }
    List<Job> jobs = (List<Job>) request.getAttribute("jobs");
    String keyword = (String) request.getAttribute("keyword");
    String type = (String) request.getAttribute("type");
    List<String> selectedSkills = (List<String>) request.getAttribute("selectedSkills");
    if (selectedSkills == null) {
        selectedSkills = new ArrayList<>();
    }
    List<String> availableSkills = (List<String>) request.getAttribute("availableSkills");
    if (availableSkills == null) {
        availableSkills = new ArrayList<>();
    }
    boolean hasActiveFilter = (keyword != null && !keyword.trim().isEmpty())
            || (type != null && !type.trim().isEmpty() && !"ALL".equalsIgnoreCase(type))
            || !selectedSkills.isEmpty();
    String lang = I18nUtil.getLanguage(request);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18nUtil.get("jobs.available", lang) %> - <%= I18nUtil.get("app.title", lang) %></title>
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
                <% } else if (user.isAdmin()) { %>
                    <a href="${pageContext.request.contextPath}/admin/workload"><%= I18nUtil.get("nav.workload", lang) %></a>
                <% } else { %>
                    <a href="${pageContext.request.contextPath}/jobs/myjobs"><%= I18nUtil.get("nav.myPostedJobs", lang) %></a>
                <% } %>
                <span class="navbar-user"><%= user.getName() %> (<%= user.getRole() %>)</span>
                <a href="${pageContext.request.contextPath}/auth?action=logout" class="btn btn-secondary"><%= I18nUtil.get("nav.logout", lang) %></a>
                <jsp:include page="/jsp/common/language-switcher.jsp" />
            </div>
        </nav>

        <h2><%= I18nUtil.get("jobs.available", lang) %></h2>

        <div class="card">
            <form action="${pageContext.request.contextPath}/jobs/list" method="get" class="filter-bar">
                <input type="text" name="keyword" placeholder="<%= I18nUtil.get("jobs.search", lang) %>" value="<%= keyword != null ? keyword : "" %>">
                <select name="type">
                    <option value="ALL" <%= type == null || type.isEmpty() || "ALL".equalsIgnoreCase(type) ? "selected" : "" %>><%= I18nUtil.get("jobs.allTypes", lang) %></option>
                    <option value="MODULE" <%= "MODULE".equals(type) ? "selected" : "" %>><%= I18nUtil.get("jobs.moduleTutor", lang) %></option>
                    <option value="INVIGILATION" <%= "INVIGILATION".equals(type) ? "selected" : "" %>><%= I18nUtil.get("jobs.invigilation", lang) %></option>
                    <option value="OTHER" <%= "OTHER".equals(type) ? "selected" : "" %>><%= I18nUtil.get("jobs.other", lang) %></option>
                </select>
                <div style="display: flex; flex-wrap: wrap; gap: 8px; width: 100%;">
                    <span style="font-weight: 600;"><%= I18nUtil.get("jobs.skillsFilter", lang) %>:</span>
                    <% for (String skill : availableSkills) { %>
                        <label>
                            <input type="checkbox" name="skills" value="<%= skill %>" <%= selectedSkills.contains(skill) ? "checked" : "" %>>
                            <%= skill %>
                        </label>
                    <% } %>
                </div>
                <button type="submit" class="btn btn-primary"><%= I18nUtil.get("jobs.searchBtn", lang) %></button>
                <a href="${pageContext.request.contextPath}/jobs/list" class="btn btn-secondary"><%= I18nUtil.get("jobs.clearFilters", lang) %></a>
            </form>
            <% if (hasActiveFilter) { %>
                <div class="info" style="margin-top: 8px;">
                    <%= I18nUtil.get("jobs.activeFilters", lang) %>
                    <%= (keyword != null && !keyword.trim().isEmpty()) ? (" | " + I18nUtil.get("jobs.search", lang) + ": " + keyword) : "" %>
                    <%= (type != null && !type.trim().isEmpty() && !"ALL".equalsIgnoreCase(type)) ? (" | " + I18nUtil.get("job.detail.type", lang) + ": " + type) : "" %>
                    <%= !selectedSkills.isEmpty() ? (" | " + I18nUtil.get("jobs.skillsFilter", lang) + ": " + String.join(", ", selectedSkills)) : "" %>
                </div>
            <% } %>
        </div>

        <% if (request.getParameter("error") != null) { %>
            <div class="alert alert-error"><%= request.getParameter("error") %></div>
        <% } %>

        <% if (request.getParameter("success") != null) { %>
            <div class="alert alert-success"><%= request.getParameter("success") %></div>
        <% } %>

        <% if (jobs != null && !jobs.isEmpty()) { %>
            <% for (Job job : jobs) { %>
                <div class="job-card">
                    <div style="display: flex; justify-content: space-between; align-items: start;">
                        <div>
                            <h3><%= job.getTitle() %></h3>
                            <div class="job-meta">
                                <span><%= job.getTypeDisplayName(lang) %></span>
                                <span><%= I18nUtil.get("jobs.positions", lang) %>: <%= job.getPositions() %></span>
                                <span><%= I18nUtil.get("jobs.deadline", lang) %>: <%= job.getDeadline() %></span>
                                <%
                                    String postedAtText = "-";
                                    if (job.getCreatedAt() != null) {
                                        String raw = job.getCreatedAt().toString();
                                        postedAtText = raw.length() >= 16 ? raw.substring(0, 16) : raw;
                                    }
                                %>
                                <span><%= I18nUtil.get("jobs.postedAt", lang) %>: <%= postedAtText %></span>
                                <span class="badge <%= job.isOpen() ? "badge-open" : "badge-closed" %>"><%= job.getStatusDisplayName(lang) %></span>
                            </div>
                        </div>
                        <div>
                            <a href="${pageContext.request.contextPath}/jobs/detail?id=<%= job.getJobId() %>" class="btn btn-primary"><%= I18nUtil.get("jobs.viewDetails", lang) %></a>
                        </div>
                    </div>
                </div>
            <% } %>
        <% } else { %>
            <div class="card text-center">
                <p style="color: #718096;"><%= I18nUtil.get("jobs.noJobs", lang) %></p>
            </div>
        <% } %>
    </div>
</body>
</html>
