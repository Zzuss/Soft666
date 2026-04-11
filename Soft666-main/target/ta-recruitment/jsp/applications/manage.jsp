<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.model.User" %>
<%@ page import="com.tarecruitment.model.Application" %>
<%@ page import="com.tarecruitment.model.Job" %>
<%@ page import="com.tarecruitment.util.I18nUtil" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }
    Job job = (Job) request.getAttribute("job");
    List<Application> applications = (List<Application>) request.getAttribute("applications");
    String lang = I18nUtil.getLanguage(request);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18nUtil.get("app.manage.title", lang) %> - <%= I18nUtil.get("app.title", lang) %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <nav class="navbar">
            <a href="${pageContext.request.contextPath}/dashboard" class="navbar-brand"><%= I18nUtil.get("app.title", lang) %></a>
            <div class="navbar-menu">
                <a href="${pageContext.request.contextPath}/dashboard"><%= I18nUtil.get("nav.dashboard", lang) %></a>
                <a href="${pageContext.request.contextPath}/jobs/list"><%= I18nUtil.get("nav.jobs", lang) %></a>
                <a href="${pageContext.request.contextPath}/jobs/myjobs"><%= I18nUtil.get("nav.myPostedJobs", lang) %></a>
                <span class="navbar-user"><%= user.getName() %> (<%= user.getRole() %>)</span>
                <a href="${pageContext.request.contextPath}/auth?action=logout" class="btn btn-secondary"><%= I18nUtil.get("nav.logout", lang) %></a>
                <jsp:include page="/jsp/common/language-switcher.jsp" />
            </div>
        </nav>

        <h2><%= I18nUtil.get("app.manage.title", lang) %></h2>

        <% if (job != null) { %>
            <div class="card">
                <h3><%= I18nUtil.get("app.manage.job", lang) %>: <%= job.getTitle() %></h3>
                <div class="job-meta">
                    <span><%= I18nUtil.get("job.detail.type", lang) %>: <%= job.getTypeDisplayName(lang) %></span>
                    <span><%= I18nUtil.get("jobs.positions", lang) %>: <%= job.getPositions() %></span>
                    <span><%= I18nUtil.get("jobs.deadline", lang) %>: <%= job.getDeadline() %></span>
                </div>
            </div>
        <% } %>

        <% if (request.getParameter("error") != null) { %>
            <div class="alert alert-error"><%= request.getParameter("error") %></div>
        <% } %>

        <% if (request.getParameter("success") != null) { %>
            <div class="alert alert-success"><%= request.getParameter("success") %></div>
        <% } %>

        <% if (applications != null && !applications.isEmpty()) { %>
            <div class="card">
                <table class="table">
                    <thead>
                        <tr>
                            <th><%= I18nUtil.get("app.manage.applicantName", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.email", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.appliedDate", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.status", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.actions", lang) %></th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Application app : applications) { 
                            User applicant = (User) request.getAttribute("user_" + app.getUserId());
                        %>
                            <tr>
                                <td><%= applicant != null ? applicant.getName() : "Unknown" %></td>
                                <td><%= applicant != null ? applicant.getEmail() : "-" %></td>
                                <td><%= app.getAppliedAt() != null ? app.getAppliedAt().toString().substring(0, 19) : "-" %></td>
                                <td>
                                    <span class="badge <%= app.isPending() ? "badge-pending" : (app.isApproved() ? "badge-approved" : "badge-rejected") %>">
                                        <%= app.getStatusDisplayName(lang) %>
                                    </span>
                                </td>
                                <td>
                                    <% if (app.isPending()) { %>
                                        <form action="${pageContext.request.contextPath}/applications/approve" method="post" style="display:inline;">
                                            <input type="hidden" name="id" value="<%= app.getApplicationId() %>">
                                            <input type="hidden" name="jobId" value="<%= job.getJobId() %>">
                                            <button type="submit" class="btn btn-success"><%= I18nUtil.get("app.manage.approve", lang) %></button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/applications/reject" method="post" style="display:inline;">
                                            <input type="hidden" name="id" value="<%= app.getApplicationId() %>">
                                            <input type="hidden" name="jobId" value="<%= job.getJobId() %>">
                                            <button type="submit" class="btn btn-danger"><%= I18nUtil.get("app.manage.reject", lang) %></button>
                                        </form>
                                    <% } else { %>
                                        <span style="color: #718096;"><%= I18nUtil.get("app.manage.processed", lang) %></span>
                                    <% } %>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } else { %>
            <div class="card text-center">
                <p style="color: #718096;"><%= I18nUtil.get("app.manage.noApps", lang) %></p>
            </div>
        <% } %>

        <div style="margin-top: 20px;">
            <a href="${pageContext.request.contextPath}/jobs/myjobs" class="btn btn-secondary"><%= I18nUtil.get("app.manage.back", lang) %></a>
        </div>
    </div>
</body>
</html>