<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.model.User" %>
<%@ page import="com.tarecruitment.model.Application" %>
<%@ page import="com.tarecruitment.model.Job" %>
<%@ page import="com.tarecruitment.model.Notification" %>
<%@ page import="com.tarecruitment.util.I18nUtil" %>
<%@ page import="java.util.List" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }
    List<Application> applications = (List<Application>) request.getAttribute("applications");
    List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
    String lang = I18nUtil.getLanguage(request);
    Integer unreadNotificationCountObj = (Integer) request.getAttribute("unreadNotificationCount");
    int unreadNotificationCount = unreadNotificationCountObj != null ? unreadNotificationCountObj : 0;
    String statusFilter = (String) request.getAttribute("statusFilter");
    if (statusFilter == null || statusFilter.isEmpty()) {
        statusFilter = "ALL";
    }
    String notificationRedirectTo = "/applications/my?status=" + statusFilter;
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18nUtil.get("app.my.title", lang) %> - <%= I18nUtil.get("app.title", lang) %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <nav class="navbar">
            <a href="${pageContext.request.contextPath}/dashboard" class="navbar-brand"><%= I18nUtil.get("app.title", lang) %></a>
            <div class="navbar-menu">
                <a href="${pageContext.request.contextPath}/dashboard"><%= I18nUtil.get("nav.dashboard", lang) %></a>
                <a href="${pageContext.request.contextPath}/jobs/list"><%= I18nUtil.get("nav.jobs", lang) %></a>
                <a href="${pageContext.request.contextPath}/applications/my"><%= I18nUtil.get("nav.myApplications", lang) %></a>
                <a href="${pageContext.request.contextPath}/profile"><%= I18nUtil.get("nav.profile", lang) %></a>
                <span class="navbar-user"><%= user.getName() %> (<%= user.getRole() %>)</span>
                <a href="${pageContext.request.contextPath}/auth?action=logout" class="btn btn-secondary"><%= I18nUtil.get("nav.logout", lang) %></a>
                <jsp:include page="/jsp/common/language-switcher.jsp" />
            </div>
        </nav>

        <h2><%= I18nUtil.get("app.my.title", lang) %></h2>
        <jsp:include page="/jsp/common/system-warning.jsp" />

        <% if (request.getParameter("error") != null) { %>
            <div class="alert alert-error"><%= request.getParameter("error") %></div>
        <% } %>

        <% if (request.getParameter("success") != null) { %>
            <div class="alert alert-success"><%= request.getParameter("success") %></div>
        <% } %>

        <div class="card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; flex-wrap: wrap; gap: 8px;">
                <h3 style="margin-bottom: 0;"><%= I18nUtil.get("notif.title", lang) %></h3>
                <div>
                    <span class="badge badge-pending"><%= unreadNotificationCount %> <%= I18nUtil.get("notif.unread", lang) %></span>
                    <% if (unreadNotificationCount > 0) { %>
                        <form action="${pageContext.request.contextPath}/notifications/read-all" method="post" style="display: inline;">
                            <input type="hidden" name="redirectTo" value="<%= notificationRedirectTo %>">
                            <button type="submit" class="btn btn-secondary"><%= I18nUtil.get("notif.markAllRead", lang) %></button>
                        </form>
                    <% } %>
                </div>
            </div>
            <% if (notifications != null && !notifications.isEmpty()) { %>
                <table class="table">
                    <thead>
                    <tr>
                        <th><%= I18nUtil.get("notif.status", lang) %></th>
                        <th><%= I18nUtil.get("notif.message", lang) %></th>
                        <th><%= I18nUtil.get("notif.time", lang) %></th>
                        <th><%= I18nUtil.get("app.my.action", lang) %></th>
                    </tr>
                    </thead>
                    <tbody>
                    <% for (Notification notification : notifications) { %>
                        <tr>
                            <td>
                                <span class="badge <%= notification.isRead() ? "badge-open" : "badge-pending" %>">
                                    <%= notification.isRead() ? I18nUtil.get("notif.read", lang) : I18nUtil.get("notif.unread", lang) %>
                                </span>
                            </td>
                            <td>
                                <strong><%= notification.getTitle() %></strong><br>
                                <%= notification.getMessage() %>
                            </td>
                            <td><%= notification.getCreatedAt() != null ? notification.getCreatedAt().toString().substring(0, 19) : "-" %></td>
                            <td>
                                <% if (!notification.isRead()) { %>
                                    <form action="${pageContext.request.contextPath}/notifications/read" method="post">
                                        <input type="hidden" name="notificationId" value="<%= notification.getNotificationId() %>">
                                        <input type="hidden" name="redirectTo" value="<%= notificationRedirectTo %>">
                                        <button type="submit" class="btn btn-primary"><%= I18nUtil.get("notif.markRead", lang) %></button>
                                    </form>
                                <% } else { %>
                                    -
                                <% } %>
                            </td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            <% } else { %>
                <p style="color: #718096;"><%= I18nUtil.get("notif.empty", lang) %></p>
            <% } %>
        </div>

        <div class="card">
            <form action="${pageContext.request.contextPath}/applications/my" method="get" class="filter-bar">
                <select name="status">
                    <option value="ALL" <%= "ALL".equalsIgnoreCase(statusFilter) ? "selected" : "" %>><%= I18nUtil.get("app.manage.statusAll", lang) %></option>
                    <option value="PENDING" <%= "PENDING".equalsIgnoreCase(statusFilter) ? "selected" : "" %>><%= I18nUtil.get("status.pending", lang) %></option>
                    <option value="APPROVED" <%= "APPROVED".equalsIgnoreCase(statusFilter) ? "selected" : "" %>><%= I18nUtil.get("status.approved", lang) %></option>
                    <option value="REJECTED" <%= "REJECTED".equalsIgnoreCase(statusFilter) ? "selected" : "" %>><%= I18nUtil.get("status.rejected", lang) %></option>
                </select>
                <button type="submit" class="btn btn-primary"><%= I18nUtil.get("app.manage.filter", lang) %></button>
            </form>
        </div>

        <% if (applications != null && !applications.isEmpty()) { %>
            <div class="card">
                <table class="table">
                    <thead>
                        <tr>
                            <th><%= I18nUtil.get("app.my.jobTitle", lang) %></th>
                            <th><%= I18nUtil.get("app.my.type", lang) %></th>
                            <th><%= I18nUtil.get("app.my.appliedDate", lang) %></th>
                            <th><%= I18nUtil.get("app.my.status", lang) %></th>
                            <th><%= I18nUtil.get("app.my.matchScore", lang) %></th>
                            <th><%= I18nUtil.get("app.my.missingSkills", lang) %></th>
                            <th><%= I18nUtil.get("app.my.rejectionNote", lang) %></th>
                            <th><%= I18nUtil.get("app.my.action", lang) %></th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Application app : applications) { 
                            Job job = (Job) request.getAttribute("job_" + app.getJobId());
                        %>
                            <tr>
                                <td><%= job != null ? job.getTitle() : "Unknown Job" %></td>
                                <td><%= job != null ? job.getTypeDisplayName(lang) : "-" %></td>
                                <td><%= app.getAppliedAt() != null ? app.getAppliedAt().toString().substring(0, 19) : "-" %></td>
                                <td>
                                    <span class="badge <%= app.isPending() ? "badge-pending" : (app.isApproved() ? "badge-approved" : "badge-rejected") %>">
                                        <%= app.getStatusDisplayName(lang) %>
                                    </span>
                                </td>
                                <td><%= app.getMatchScore() != null ? (app.getMatchScore() + "%") : "-" %></td>
                                <td><%= (app.getMissingSkills() != null && !app.getMissingSkills().isEmpty()) ? String.join(", ", app.getMissingSkills()) : "-" %></td>
                                <td>
                                    <%= app.isRejected() && app.getRejectionNote() != null && !app.getRejectionNote().isEmpty()
                                            ? app.getRejectionNote()
                                            : "-" %>
                                </td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/jobs/detail?id=<%= app.getJobId() %>" class="btn btn-secondary"><%= I18nUtil.get("app.my.viewJob", lang) %></a>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } else { %>
            <div class="card text-center">
                <p style="color: #718096;"><%= I18nUtil.get("app.my.noApps", lang) %></p>
                <a href="${pageContext.request.contextPath}/jobs/list" class="btn btn-primary" style="margin-top: 15px;"><%= I18nUtil.get("app.my.browse", lang) %></a>
            </div>
        <% } %>
    </div>
</body>
</html>
