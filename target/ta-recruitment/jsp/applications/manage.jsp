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
    String statusFilter = (String) request.getAttribute("statusFilter");
    if (statusFilter == null || statusFilter.isEmpty()) {
        statusFilter = "ALL";
    }
    String keywordFilter = (String) request.getAttribute("keywordFilter");
    if (keywordFilter == null) {
        keywordFilter = "";
    }
    Integer approvedCount = (Integer) request.getAttribute("approvedCount");
    if (approvedCount == null) {
        approvedCount = 0;
    }
    Integer remainingSlots = (Integer) request.getAttribute("remainingSlots");
    if (remainingSlots == null) {
        remainingSlots = 0;
    }
    Integer workloadLimit = (Integer) request.getAttribute("workloadLimit");
    if (workloadLimit == null) {
        workloadLimit = 2;
    }
    String warningApplicationId = (String) request.getAttribute("warningApplicationId");
    String warningUserName = (String) request.getAttribute("warningUserName");
    String warningLoad = (String) request.getAttribute("warningLoad");
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

        <h2><%= I18nUtil.get("app.manage.title", lang) %></h2>
        <jsp:include page="/jsp/common/system-warning.jsp" />

        <% if (job != null) { %>
            <div class="card">
                <h3><%= I18nUtil.get("app.manage.job", lang) %>: <%= job.getTitle() %></h3>
                <div class="job-meta">
                    <span><%= I18nUtil.get("job.detail.type", lang) %>: <%= job.getTypeDisplayName(lang) %></span>
                    <span><%= I18nUtil.get("jobs.positions", lang) %>: <%= job.getPositions() %></span>
                    <span><%= I18nUtil.get("app.manage.approvedCount", lang) %>: <%= approvedCount %></span>
                    <span><%= I18nUtil.get("app.manage.remainingSlots", lang) %>: <%= remainingSlots %></span>
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

        <% if (warningApplicationId != null && job != null) { %>
            <div class="alert alert-error">
                <p><strong><%= I18nUtil.get("app.manage.overloadWarningTitle", lang) %></strong></p>
                <p>
                    <%= I18nUtil.get("app.manage.overloadWarningText", lang) %>
                    <strong><%= warningUserName != null ? warningUserName : "TA" %></strong>
                    (<%= warningLoad != null ? warningLoad : String.valueOf(workloadLimit) %>).
                </p>
                <p><%= I18nUtil.get("app.manage.overloadLimitHint", lang) %> <%= workloadLimit %></p>
                <p><%= I18nUtil.get("app.manage.overloadRiskHint", lang) %></p>
                <form action="${pageContext.request.contextPath}/applications/approve" method="post" style="margin-top: 10px;">
                    <input type="hidden" name="id" value="<%= warningApplicationId %>">
                    <input type="hidden" name="jobId" value="<%= job.getJobId() %>">
                    <input type="hidden" name="forceOverload" value="true">
                    <button type="submit" class="btn btn-danger"><%= I18nUtil.get("app.manage.forceApprove", lang) %><%= warningUserName != null ? (" (" + warningUserName + ")") : "" %></button>
                </form>
            </div>
        <% } %>

        <% if (job != null) { %>
            <div class="card">
                <form action="${pageContext.request.contextPath}/applications/manage" method="get" class="filter-bar">
                    <input type="hidden" name="jobId" value="<%= job.getJobId() %>">
                    <select name="status">
                        <option value="ALL" <%= "ALL".equalsIgnoreCase(statusFilter) ? "selected" : "" %>><%= I18nUtil.get("app.manage.statusAll", lang) %></option>
                        <option value="PENDING" <%= "PENDING".equalsIgnoreCase(statusFilter) ? "selected" : "" %>><%= I18nUtil.get("status.pending", lang) %></option>
                        <option value="APPROVED" <%= "APPROVED".equalsIgnoreCase(statusFilter) ? "selected" : "" %>><%= I18nUtil.get("status.approved", lang) %></option>
                        <option value="REJECTED" <%= "REJECTED".equalsIgnoreCase(statusFilter) ? "selected" : "" %>><%= I18nUtil.get("status.rejected", lang) %></option>
                    </select>
                    <input type="text" name="keyword" value="<%= keywordFilter %>" placeholder="<%= I18nUtil.get("app.manage.keyword", lang) %>">
                    <button type="submit" class="btn btn-primary"><%= I18nUtil.get("app.manage.filter", lang) %></button>
                </form>
            </div>
        <% } %>

        <% if (applications != null && !applications.isEmpty()) { %>
            <div class="card">
                <form id="batchReviewForm" action="${pageContext.request.contextPath}/applications/bulk-review" method="post" class="filter-bar" style="margin-bottom: 12px;">
                    <input type="hidden" name="jobId" value="<%= job != null ? job.getJobId() : "" %>">
                    <label for="batchAction"><%= I18nUtil.get("app.manage.batchAction", lang) %></label>
                    <select id="batchAction" name="batchAction">
                        <option value="APPROVE"><%= I18nUtil.get("app.manage.batchApprove", lang) %></option>
                        <option value="REJECT"><%= I18nUtil.get("app.manage.batchReject", lang) %></option>
                    </select>
                    <input type="text" name="rejectionNote" maxlength="300"
                           placeholder="<%= I18nUtil.get("app.manage.batchRejectionNotePlaceholder", lang) %>"
                           style="min-width: 220px;">
                    <label style="display: inline-flex; align-items: center; gap: 6px;">
                        <input type="checkbox" name="forceOverload" value="true">
                        <%= I18nUtil.get("app.manage.batchForceOverload", lang) %>
                    </label>
                    <button type="submit" class="btn btn-primary"><%= I18nUtil.get("app.manage.batchApply", lang) %></button>
                </form>
                <table class="table">
                    <thead>
                        <tr>
                            <th><%= I18nUtil.get("app.manage.select", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.applicantName", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.email", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.skills", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.availableTime", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.matchScore", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.missingSkills", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.currentLoad", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.appliedDate", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.status", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.rejectionNote", lang) %></th>
                            <th><%= I18nUtil.get("app.manage.actions", lang) %></th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Application app : applications) { 
                            User applicant = (User) request.getAttribute("user_" + app.getUserId());
                            Integer applicantLoad = (Integer) request.getAttribute("load_" + app.getUserId());
                            if (applicantLoad == null) {
                                applicantLoad = 0;
                            }
                        %>
                            <tr>
                                <td>
                                    <% if (app.isPending()) { %>
                                        <input type="checkbox" name="selectedIds" value="<%= app.getApplicationId() %>" form="batchReviewForm">
                                    <% } else { %>
                                        -
                                    <% } %>
                                </td>
                                <td><%= applicant != null ? applicant.getName() : "Unknown" %></td>
                                <td><%= applicant != null ? applicant.getEmail() : "-" %></td>
                                <td><%= (applicant != null && applicant.getSkills() != null && !applicant.getSkills().isEmpty()) ? String.join(", ", applicant.getSkills()) : "-" %></td>
                                <td><%= (applicant != null && applicant.getAvailableTime() != null && !applicant.getAvailableTime().isEmpty()) ? applicant.getAvailableTime() : "-" %></td>
                                <td><%= app.getMatchScore() != null ? (app.getMatchScore() + "%") : "-" %></td>
                                <td><%= (app.getMissingSkills() != null && !app.getMissingSkills().isEmpty()) ? String.join(", ", app.getMissingSkills()) : "-" %></td>
                                <td>
                                    <%= applicantLoad %>
                                    <% if (applicantLoad >= workloadLimit) { %>
                                        <span class="badge badge-rejected"><%= I18nUtil.get("app.manage.overloadBadge", lang) %></span>
                                    <% } %>
                                </td>
                                <td><%= app.getAppliedAt() != null ? app.getAppliedAt().toString().substring(0, 19) : "-" %></td>
                                <td>
                                    <span class="badge <%= app.isPending() ? "badge-pending" : (app.isApproved() ? "badge-approved" : "badge-rejected") %>">
                                        <%= app.getStatusDisplayName(lang) %>
                                    </span>
                                </td>
                                <td><%= (app.getRejectionNote() != null && !app.getRejectionNote().isEmpty()) ? app.getRejectionNote() : "-" %></td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/profile/view?userId=<%= app.getUserId() %>&jobId=<%= job.getJobId() %>" class="btn btn-secondary"><%= I18nUtil.get("app.manage.viewProfile", lang) %></a>
                                    <% if (app.isPending()) { %>
                                        <form action="${pageContext.request.contextPath}/applications/approve" method="post" style="display:inline;">
                                            <input type="hidden" name="id" value="<%= app.getApplicationId() %>">
                                            <input type="hidden" name="jobId" value="<%= job.getJobId() %>">
                                            <button type="submit" class="btn btn-success"><%= I18nUtil.get("app.manage.approve", lang) %></button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/applications/reject" method="post" style="display:inline-flex; gap:6px; align-items:center;">
                                            <input type="hidden" name="id" value="<%= app.getApplicationId() %>">
                                            <input type="hidden" name="jobId" value="<%= job.getJobId() %>">
                                            <input type="text" name="rejectionNote" maxlength="300"
                                                   placeholder="<%= I18nUtil.get("app.manage.rejectionNotePlaceholder", lang) %>"
                                                   style="width: 180px; padding: 6px 8px;">
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
