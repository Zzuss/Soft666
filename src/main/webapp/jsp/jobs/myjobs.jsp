<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.model.User" %>
<%@ page import="com.tarecruitment.model.Job" %>
<%@ page import="com.tarecruitment.util.I18nUtil" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }
    List<Job> jobs = (List<Job>) request.getAttribute("jobs");
    String lang = I18nUtil.getLanguage(request);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18nUtil.get("job.my.title", lang) %> - <%= I18nUtil.get("app.title", lang) %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <nav class="navbar">
            <a href="${pageContext.request.contextPath}/dashboard" class="navbar-brand"><%= I18nUtil.get("app.title", lang) %></a>
            <div class="navbar-menu">
                <a href="${pageContext.request.contextPath}/dashboard"><%= I18nUtil.get("nav.dashboard", lang) %></a>
                <a href="${pageContext.request.contextPath}/jobs/list"><%= I18nUtil.get("nav.jobs", lang) %></a>
                <% if (user.isAdmin()) { %>
                    <a href="${pageContext.request.contextPath}/admin/workload"><%= I18nUtil.get("nav.workload", lang) %></a>
                <% } else { %>
                    <a href="${pageContext.request.contextPath}/jobs/myjobs"><%= I18nUtil.get("nav.myPostedJobs", lang) %></a>
                <% } %>
                <span class="navbar-user"><%= user.getName() %> (<%= user.getRole() %>)</span>
                <a href="${pageContext.request.contextPath}/auth?action=logout" class="btn btn-secondary"><%= I18nUtil.get("nav.logout", lang) %></a>
                <jsp:include page="/jsp/common/language-switcher.jsp" />
            </div>
        </nav>

        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h2><%= I18nUtil.get("job.my.title", lang) %></h2>
            <a href="${pageContext.request.contextPath}/jobs/create" class="btn btn-primary"><%= I18nUtil.get("job.my.postNew", lang) %></a>
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
                                <span class="badge <%= job.isOpen() ? "badge-open" : "badge-closed" %>"><%= job.getStatusDisplayName(lang) %></span>
                            </div>
                        </div>
                        <div class="table-actions">
                            <a href="${pageContext.request.contextPath}/applications/manage?jobId=<%= job.getJobId() %>" class="btn btn-primary"><%= I18nUtil.get("job.my.viewApps", lang) %></a>
                            <a href="${pageContext.request.contextPath}/jobs/edit?id=<%= job.getJobId() %>" class="btn btn-secondary"><%= I18nUtil.get("job.my.edit", lang) %></a>
                            <% if (job.isOpen()) { %>
                                <form action="${pageContext.request.contextPath}/jobs/close" method="post" style="display:inline;">
                                    <input type="hidden" name="id" value="<%= job.getJobId() %>">
                                    <button type="submit" class="btn btn-secondary" onclick="return confirm('<%= I18nUtil.get("job.my.confirmClose", lang) %>')"><%= I18nUtil.get("job.my.close", lang) %></button>
                                </form>
                            <% } %>
                        </div>
                    </div>
                </div>
            <% } %>
        <% } else { %>
            <div class="card text-center">
                <p style="color: #718096;"><%= I18nUtil.get("job.my.noJobs", lang) %></p>
                <a href="${pageContext.request.contextPath}/jobs/create" class="btn btn-primary" style="margin-top: 15px;"><%= I18nUtil.get("job.my.firstJob", lang) %></a>
            </div>
        <% } %>
    </div>
</body>
</html>
