<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.model.User" %>
<%@ page import="com.tarecruitment.util.I18nUtil" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }
    if (!user.isMO() && !user.isAdmin()) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }
    String lang = I18nUtil.getLanguage(request);
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= I18nUtil.get("job.create.title", lang) %> - <%= I18nUtil.get("app.title", lang) %></title>
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

        <div class="card" style="max-width: 600px; margin: 0 auto;">
            <h2><%= I18nUtil.get("job.create.title", lang) %></h2>
            
            <% if (request.getAttribute("error") != null) { %>
                <div class="error"><%= request.getAttribute("error") %></div>
            <% } %>
            
            <form action="${pageContext.request.contextPath}/jobs/create" method="post">
                <div class="form-group">
                    <label for="title"><%= I18nUtil.get("job.create.jobTitle", lang) %></label>
                    <input type="text" id="title" name="title" required>
                </div>
                
                <div class="form-group">
                    <label for="type"><%= I18nUtil.get("job.create.type", lang) %></label>
                    <select id="type" name="type" required>
                        <option value="MODULE"><%= I18nUtil.get("jobs.moduleTutor", lang) %></option>
                        <option value="INVIGILATION"><%= I18nUtil.get("jobs.invigilation", lang) %></option>
                        <option value="OTHER"><%= I18nUtil.get("jobs.other", lang) %></option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="description"><%= I18nUtil.get("job.create.desc", lang) %></label>
                    <textarea id="description" name="description" rows="4" required></textarea>
                </div>
                
                <div class="form-group">
                    <label for="requirements"><%= I18nUtil.get("job.create.req", lang) %></label>
                    <textarea id="requirements" name="requirements" rows="3"></textarea>
                </div>
                
                <div class="form-group">
                    <label for="positions"><%= I18nUtil.get("job.create.numPositions", lang) %></label>
                    <input type="number" id="positions" name="positions" min="1" value="1" required>
                </div>
                
                <div class="form-group">
                    <label for="deadline"><%= I18nUtil.get("job.create.deadline", lang) %></label>
                    <input type="date" id="deadline" name="deadline" required>
                </div>
                
                <div style="display: flex; gap: 15px;">
                    <button type="submit" class="btn btn-primary"><%= I18nUtil.get("job.create.submit", lang) %></button>
                    <a href="${pageContext.request.contextPath}/jobs/myjobs" class="btn btn-secondary"><%= I18nUtil.get("job.create.cancel", lang) %></a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
