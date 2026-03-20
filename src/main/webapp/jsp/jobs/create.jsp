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
                    <textarea id="requirements" name="requirements" rows="3" required></textarea>
                </div>

                <div class="form-group">
                    <label for="workStartDate"><%= I18nUtil.get("job.create.workStartDate", lang) %></label>
                    <input type="date" id="workStartDate" name="workStartDate" required>
                </div>

                <div class="form-group">
                    <label for="workEndDate"><%= I18nUtil.get("job.create.workEndDate", lang) %></label>
                    <input type="date" id="workEndDate" name="workEndDate" required>
                </div>

                <div class="form-group">
                    <label><%= I18nUtil.get("job.create.workWeekdays", lang) %></label>
                    <div style="display: flex; flex-wrap: wrap; gap: 10px;">
                        <label><input type="checkbox" name="workWeekdays" value="MON"> <%= I18nUtil.getWeekdayDisplay("MON", lang) %></label>
                        <label><input type="checkbox" name="workWeekdays" value="TUE"> <%= I18nUtil.getWeekdayDisplay("TUE", lang) %></label>
                        <label><input type="checkbox" name="workWeekdays" value="WED"> <%= I18nUtil.getWeekdayDisplay("WED", lang) %></label>
                        <label><input type="checkbox" name="workWeekdays" value="THU"> <%= I18nUtil.getWeekdayDisplay("THU", lang) %></label>
                        <label><input type="checkbox" name="workWeekdays" value="FRI"> <%= I18nUtil.getWeekdayDisplay("FRI", lang) %></label>
                        <label><input type="checkbox" name="workWeekdays" value="SAT"> <%= I18nUtil.getWeekdayDisplay("SAT", lang) %></label>
                        <label><input type="checkbox" name="workWeekdays" value="SUN"> <%= I18nUtil.getWeekdayDisplay("SUN", lang) %></label>
                    </div>
                </div>

                <div class="form-group">
                    <label for="dailyStartHour"><%= I18nUtil.get("job.create.dailyStartHour", lang) %></label>
                    <select id="dailyStartHour" name="dailyStartHour" required>
                        <option value=""><%= "zh".equals(lang) ? "请选择整点" : "Select hour" %></option>
                        <% for (int h = 0; h < 24; h++) {
                            String hourValue = String.format("%02d:00", h);
                        %>
                            <option value="<%= hourValue %>"><%= hourValue %></option>
                        <% } %>
                    </select>
                </div>

                <div class="form-group">
                    <label for="dailyEndHour"><%= I18nUtil.get("job.create.dailyEndHour", lang) %></label>
                    <select id="dailyEndHour" name="dailyEndHour" required>
                        <option value=""><%= "zh".equals(lang) ? "请选择整点" : "Select hour" %></option>
                        <% for (int h = 0; h < 24; h++) {
                            String hourValue = String.format("%02d:00", h);
                        %>
                            <option value="<%= hourValue %>"><%= hourValue %></option>
                        <% } %>
                    </select>
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
