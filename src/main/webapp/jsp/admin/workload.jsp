<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.model.User" %>
<%@ page import="com.tarecruitment.util.I18nUtil" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }
    List<User> taUsers = (List<User>) request.getAttribute("taUsers");
    Map<String, Integer> workloadMap = (Map<String, Integer>) request.getAttribute("workloadMap");
    Integer workloadThreshold = (Integer) request.getAttribute("workloadThreshold");
    if (workloadThreshold == null) {
        workloadThreshold = 2;
    }
    String lang = I18nUtil.getLanguage(request);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18nUtil.get("admin.workload.title", lang) %> - <%= I18nUtil.get("app.title", lang) %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <nav class="navbar">
            <a href="${pageContext.request.contextPath}/dashboard" class="navbar-brand"><%= I18nUtil.get("app.title", lang) %></a>
            <div class="navbar-menu">
                <a href="${pageContext.request.contextPath}/dashboard"><%= I18nUtil.get("nav.dashboard", lang) %></a>
                <a href="${pageContext.request.contextPath}/jobs/list"><%= I18nUtil.get("nav.jobs", lang) %></a>
                <a href="${pageContext.request.contextPath}/admin/workload"><%= I18nUtil.get("nav.workload", lang) %></a>
                <span class="navbar-user"><%= user.getName() %> (<%= user.getRole() %>)</span>
                <a href="${pageContext.request.contextPath}/auth?action=logout" class="btn btn-secondary"><%= I18nUtil.get("nav.logout", lang) %></a>
                <jsp:include page="/jsp/common/language-switcher.jsp" />
            </div>
        </nav>

        <h2><%= I18nUtil.get("admin.workload.title", lang) %></h2>
        <jsp:include page="/jsp/common/system-warning.jsp" />

        <div class="card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; gap: 10px; flex-wrap: wrap;">
                <p style="color: #718096;">
                    <%= I18nUtil.get("admin.workload.thresholdHint", lang) %> <strong><%= workloadThreshold %></strong>
                </p>
                <a href="${pageContext.request.contextPath}/admin/workload/export" class="btn btn-primary"><%= I18nUtil.get("admin.workload.exportCsv", lang) %></a>
            </div>
            <% if (taUsers != null && !taUsers.isEmpty()) { %>
                <table class="table workload-table">
                    <thead>
                        <tr>
                            <th><%= I18nUtil.get("admin.workload.taName", lang) %></th>
                            <th><%= I18nUtil.get("admin.workload.email", lang) %></th>
                            <th><%= I18nUtil.get("admin.workload.skills", lang) %></th>
                            <th><%= I18nUtil.get("admin.workload.approved", lang) %></th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (User ta : taUsers) {
                            int approvedAssignments = workloadMap != null ? workloadMap.getOrDefault(ta.getUserId(), 0) : 0;
                            boolean overloaded = approvedAssignments > workloadThreshold;
                        %>
                            <tr class="<%= overloaded ? "workload-overload" : "" %>">
                                <td><%= ta.getName() %></td>
                                <td><%= ta.getEmail() %></td>
                                <td><%= (ta.getSkills() != null && !ta.getSkills().isEmpty()) ? String.join(", ", ta.getSkills()) : "-" %></td>
                                <td>
                                    <strong><%= approvedAssignments %></strong>
                                    <% if (overloaded) { %>
                                        <span class="badge badge-rejected"><%= I18nUtil.get("admin.workload.overload", lang) %></span>
                                    <% } %>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } else { %>
                <p style="color: #718096;"><%= I18nUtil.get("admin.workload.noData", lang) %></p>
            <% } %>
        </div>
    </div>
</body>
</html>
