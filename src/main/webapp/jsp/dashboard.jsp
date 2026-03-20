<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.model.User" %>
<%@ page import="com.tarecruitment.util.I18nUtil" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        user = (User) request.getAttribute("user");
    }
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }
    String lang = I18nUtil.getLanguage(request);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18nUtil.get("nav.dashboard", lang) %> - <%= I18nUtil.get("app.title", lang) %></title>
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

        <h2><%= I18nUtil.get("dashboard.welcome", lang) %>, <%= user.getName() %>!</h2>

        <div class="stats-grid">
            <div class="stat-card">
                <h3><%= request.getAttribute("openJobsCount") %></h3>
                <p><%= I18nUtil.get("dashboard.openPositions", lang) %></p>
            </div>
            <% if (!user.isTA()) { %>
                <div class="stat-card">
                    <h3><%= request.getAttribute("myJobsCount") %></h3>
                    <p><%= I18nUtil.get("dashboard.myPostedJobs", lang) %></p>
                </div>
            <% } %>
            <% if (user.isTA()) { %>
                <div class="stat-card">
                    <h3><%= request.getAttribute("myApplicationsCount") %></h3>
                    <p><%= I18nUtil.get("dashboard.myApplications", lang) %></p>
                </div>
            <% } %>
        </div>

        <div class="card">
            <h3><%= I18nUtil.get("dashboard.quickActions", lang) %></h3>
            <div style="display: flex; gap: 15px; flex-wrap: wrap;">
                <% if (user.isTA()) { %>
                    <a href="${pageContext.request.contextPath}/jobs/list" class="btn btn-primary"><%= I18nUtil.get("dashboard.browseJobs", lang) %></a>
                    <a href="${pageContext.request.contextPath}/applications/my" class="btn btn-primary"><%= I18nUtil.get("dashboard.myApplications", lang) %></a>
                    <a href="${pageContext.request.contextPath}/profile" class="btn btn-primary"><%= I18nUtil.get("dashboard.myProfile", lang) %></a>
                <% } else if (user.isAdmin()) { %>
                    <a href="${pageContext.request.contextPath}/admin/workload" class="btn btn-primary"><%= I18nUtil.get("dashboard.taWorkload", lang) %></a>
                    <a href="${pageContext.request.contextPath}/jobs/list" class="btn btn-primary"><%= I18nUtil.get("dashboard.browseJobs", lang) %></a>
                <% } else { %>
                    <a href="${pageContext.request.contextPath}/jobs/create" class="btn btn-primary"><%= I18nUtil.get("dashboard.postNewJob", lang) %></a>
                    <a href="${pageContext.request.contextPath}/jobs/myjobs" class="btn btn-primary"><%= I18nUtil.get("dashboard.manageJobs", lang) %></a>
                <% } %>
            </div>
        </div>

        <div class="card">
            <h3><%= I18nUtil.get("dashboard.roleInfo", lang) %></h3>
            <% if (user.isTA()) { %>
                <p><%= I18nUtil.get("dashboard.taRole", lang) %></p>
            <% } else if (user.isMO()) { %>
                <p><%= I18nUtil.get("dashboard.moRole", lang) %></p>
            <% } else if (user.isAdmin()) { %>
                <p><%= I18nUtil.get("dashboard.adminRole", lang) %></p>
            <% } %>
        </div>
    </div>
</body>
</html>
