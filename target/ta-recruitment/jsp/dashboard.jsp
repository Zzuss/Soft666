<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.model.User" %>
<%
    User user = (User) request.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <nav class="navbar">
            <a href="${pageContext.request.contextPath}/dashboard" class="navbar-brand">TA Recruitment System</a>
            <div class="navbar-menu">
                <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
                <a href="${pageContext.request.contextPath}/jobs/list">Jobs</a>
                <% if (user.isTA()) { %>
                    <a href="${pageContext.request.contextPath}/applications/my">My Applications</a>
                <% } else { %>
                    <a href="${pageContext.request.contextPath}/jobs/myjobs">My Posted Jobs</a>
                <% } %>
                <span class="navbar-user"><%= user.getName() %> (<%= user.getRole() %>)</span>
                <a href="${pageContext.request.contextPath}/auth?action=logout" class="btn btn-secondary">Logout</a>
            </div>
        </nav>

        <h2>Welcome, <%= user.getName() %>!</h2>

        <div class="stats-grid">
            <div class="stat-card">
                <h3><%= request.getAttribute("openJobsCount") %></h3>
                <p>Open Positions</p>
            </div>
            <div class="stat-card">
                <h3><%= request.getAttribute("myJobsCount") %></h3>
                <p>My Posted Jobs</p>
            </div>
            <% if (user.isTA()) { %>
                <div class="stat-card">
                    <h3><%= request.getAttribute("myApplicationsCount") %></h3>
                    <p>My Applications</p>
                </div>
            <% } %>
        </div>

        <div class="card">
            <h3>Quick Actions</h3>
            <div style="display: flex; gap: 15px; flex-wrap: wrap;">
                <% if (user.isTA()) { %>
                    <a href="${pageContext.request.contextPath}/jobs/list" class="btn btn-primary">Browse Jobs</a>
                    <a href="${pageContext.request.contextPath}/applications/my" class="btn btn-primary">My Applications</a>
                <% } else { %>
                    <a href="${pageContext.request.contextPath}/jobs/create" class="btn btn-primary">Post New Job</a>
                    <a href="${pageContext.request.contextPath}/jobs/myjobs" class="btn btn-primary">Manage My Jobs</a>
                <% } %>
            </div>
        </div>

        <div class="card">
            <h3>Role Information</h3>
            <% if (user.isTA()) { %>
                <p>As a Teaching Assistant (TA), you can browse available positions and apply for jobs that match your skills.</p>
            <% } else if (user.isMO()) { %>
                <p>As a Module Organiser (MO), you can post job positions and review applications from TA candidates.</p>
            <% } else if (user.isAdmin()) { %>
                <p>As an Administrator, you have full access to manage the system and view all data.</p>
            <% } %>
        </div>
    </div>
</body>
</html>