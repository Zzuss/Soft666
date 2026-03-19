<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.model.User" %>
<%@ page import="com.tarecruitment.model.Job" %>
<%@ page import="com.tarecruitment.util.I18nUtil" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }
    Job job = (Job) request.getAttribute("job");
    if (job == null) {
        response.sendRedirect(request.getContextPath() + "/jobs/list");
        return;
    }
    String lang = I18nUtil.getLanguage(request);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= job.getTitle() %> - <%= I18nUtil.get("app.title", lang) %></title>
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
                <% } else { %>
                    <a href="${pageContext.request.contextPath}/jobs/myjobs"><%= I18nUtil.get("nav.myPostedJobs", lang) %></a>
                <% } %>
                <span class="navbar-user"><%= user.getName() %> (<%= user.getRole() %>)</span>
                <a href="${pageContext.request.contextPath}/auth?action=logout" class="btn btn-secondary"><%= I18nUtil.get("nav.logout", lang) %></a>
                <jsp:include page="/jsp/common/language-switcher.jsp" />
            </div>
        </nav>

        <div class="card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                <h2><%= job.getTitle() %></h2>
                <span class="badge <%= job.isOpen() ? "badge-open" : "badge-closed" %>"><%= job.getStatusDisplayName(lang) %></span>
            </div>

            <div class="job-meta" style="margin-bottom: 20px;">
                <span><strong><%= I18nUtil.get("job.detail.type", lang) %>:</strong> <%= job.getTypeDisplayName(lang) %></span>
                <span><strong><%= I18nUtil.get("jobs.positions", lang) %>:</strong> <%= job.getPositions() %></span>
                <span><strong><%= I18nUtil.get("jobs.deadline", lang) %>:</strong> <%= job.getDeadline() %></span>
            </div>

            <div class="profile-section">
                <div class="profile-info">
                    <h3><%= I18nUtil.get("job.detail.description", lang) %></h3>
                    <p><%= job.getDescription() != null ? job.getDescription() : I18nUtil.get("job.detail.noDesc", lang) %></p>
                </div>
                <div class="profile-info">
                    <h3><%= I18nUtil.get("job.detail.requirements", lang) %></h3>
                    <p><%= job.getRequirements() != null ? job.getRequirements() : I18nUtil.get("job.detail.noReq", lang) %></p>
                </div>
            </div>

            <div style="margin-top: 30px; display: flex; gap: 15px;">
                <% if (user.isTA() && job.isOpen()) { %>
                    <form action="${pageContext.request.contextPath}/applications/apply" method="post">
                        <input type="hidden" name="jobId" value="<%= job.getJobId() %>">
                        <button type="submit" class="btn btn-success"><%= I18nUtil.get("job.detail.apply", lang) %></button>
                    </form>
                <% } %>
                <a href="${pageContext.request.contextPath}/jobs/list" class="btn btn-secondary"><%= I18nUtil.get("job.detail.back", lang) %></a>
            </div>
        </div>
    </div>
</body>
</html>