<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.tarecruitment.model.User" %>
<%@ page import="com.tarecruitment.model.Job" %>
<%@ page import="com.tarecruitment.util.I18nUtil" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.HashSet" %>
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
    Job editingJob = (Job) request.getAttribute("job");
    boolean editMode = Boolean.TRUE.equals(request.getAttribute("editMode")) || editingJob != null;

    String formTitle = request.getAttribute("formTitle") != null
            ? (String) request.getAttribute("formTitle")
            : (editingJob != null && editingJob.getTitle() != null ? editingJob.getTitle() : "");
    String formCourseCode = request.getAttribute("formCourseCode") != null
            ? (String) request.getAttribute("formCourseCode")
            : (editingJob != null && editingJob.getCourseCode() != null ? editingJob.getCourseCode() : "");
    String formType = request.getAttribute("formType") != null
            ? (String) request.getAttribute("formType")
            : (editingJob != null && editingJob.getType() != null ? editingJob.getType() : "MODULE");
    String formDescription = request.getAttribute("formDescription") != null
            ? (String) request.getAttribute("formDescription")
            : (editingJob != null && editingJob.getDescription() != null ? editingJob.getDescription() : "");
    String formRequirements = request.getAttribute("formRequirements") != null
            ? (String) request.getAttribute("formRequirements")
            : (editingJob != null && editingJob.getRequirements() != null ? editingJob.getRequirements() : "");
    String formRequiredSkills = request.getAttribute("formRequiredSkills") != null
            ? (String) request.getAttribute("formRequiredSkills")
            : (editingJob != null && editingJob.getRequiredSkills() != null ? editingJob.getRequiredSkills() : "");
    String formWorkStartDate = request.getAttribute("formWorkStartDate") != null
            ? (String) request.getAttribute("formWorkStartDate")
            : (editingJob != null && editingJob.getWorkStartDate() != null ? editingJob.getWorkStartDate() : "");
    String formWorkEndDate = request.getAttribute("formWorkEndDate") != null
            ? (String) request.getAttribute("formWorkEndDate")
            : (editingJob != null && editingJob.getWorkEndDate() != null ? editingJob.getWorkEndDate() : "");
    String formDailyStartHour = request.getAttribute("formDailyStartHour") != null
            ? (String) request.getAttribute("formDailyStartHour")
            : (editingJob != null && editingJob.getDailyStartHour() != null ? editingJob.getDailyStartHour() : "");
    String formDailyEndHour = request.getAttribute("formDailyEndHour") != null
            ? (String) request.getAttribute("formDailyEndHour")
            : (editingJob != null && editingJob.getDailyEndHour() != null ? editingJob.getDailyEndHour() : "");
    String formPositions = request.getAttribute("formPositions") != null
            ? (String) request.getAttribute("formPositions")
            : (editingJob != null ? String.valueOf(editingJob.getPositions()) : "1");
    String formDeadline = request.getAttribute("formDeadline") != null
            ? (String) request.getAttribute("formDeadline")
            : (editingJob != null && editingJob.getDeadline() != null ? editingJob.getDeadline() : "");

    Set<String> selectedWeekdays = new HashSet<>();
    Object formWeekdaysAttr = request.getAttribute("formWeekdays");
    if (formWeekdaysAttr instanceof List<?>) {
        for (Object value : (List<?>) formWeekdaysAttr) {
            String code = value == null ? "" : value.toString().trim().toUpperCase();
            if (!code.isEmpty()) {
                selectedWeekdays.add(code);
            }
        }
    } else if (editingJob != null && editingJob.getWorkWeekdays() != null && !editingJob.getWorkWeekdays().isEmpty()) {
        String[] values = editingJob.getWorkWeekdays().split(",");
        for (String value : values) {
            String code = value == null ? "" : value.trim().toUpperCase();
            if (!code.isEmpty()) {
                selectedWeekdays.add(code);
            }
        }
    }
    String selectedType = formType != null && !formType.isEmpty() ? formType : "MODULE";
    String startHourValue = formDailyStartHour != null ? formDailyStartHour : "";
    if (startHourValue.length() >= 5) {
        startHourValue = startHourValue.substring(0, 5);
    }
    String endHourValue = formDailyEndHour != null ? formDailyEndHour : "";
    if (endHourValue.length() >= 5) {
        endHourValue = endHourValue.substring(0, 5);
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= editMode ? I18nUtil.get("job.edit.title", lang) : I18nUtil.get("job.create.title", lang) %> - <%= I18nUtil.get("app.title", lang) %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <nav class="navbar">
            <a href="${pageContext.request.contextPath}/dashboard" class="navbar-brand"><%= I18nUtil.get("app.title", lang) %></a>
            <div class="navbar-menu">
                <a href="${pageContext.request.contextPath}/dashboard"><%= I18nUtil.get("nav.dashboard", lang) %></a>
                <a href="${pageContext.request.contextPath}/jobs/list"><%= I18nUtil.get("nav.jobs", lang) %></a>
                <% if (user.isTA() || user.isMO()) { %>
                    <a href="${pageContext.request.contextPath}/ai/chat"><%= I18nUtil.get("nav.askAI", lang) %></a>
                <% } %>
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
        <jsp:include page="/jsp/common/system-warning.jsp" />

        <div class="card" style="max-width: 600px; margin: 0 auto;">
            <h2><%= editMode ? I18nUtil.get("job.edit.title", lang) : I18nUtil.get("job.create.title", lang) %></h2>

            <% if (request.getAttribute("error") != null) { %>
                <div class="error"><%= request.getAttribute("error") %></div>
            <% } %>

            <form action="${pageContext.request.contextPath}/jobs/<%= editMode ? "update" : "create" %>" method="post">
                <% if (editMode && editingJob != null) { %>
                    <input type="hidden" name="id" value="<%= editingJob.getJobId() %>">
                <% } %>

                <div class="form-group">
                    <label for="title"><%= I18nUtil.get("job.create.jobTitle", lang) %></label>
                    <input type="text" id="title" name="title" required value="<%= formTitle %>">
                </div>

                <div class="form-group">
                    <label for="courseCode"><%= I18nUtil.get("job.create.courseCode", lang) %></label>
                    <input type="text" id="courseCode" name="courseCode" required
                           value="<%= formCourseCode %>">
                </div>

                <div class="form-group">
                    <label for="type"><%= I18nUtil.get("job.create.type", lang) %></label>
                    <select id="type" name="type" required>
                        <option value="MODULE" <%= "MODULE".equalsIgnoreCase(selectedType) ? "selected" : "" %>><%= I18nUtil.get("jobs.moduleTutor", lang) %></option>
                        <option value="INVIGILATION" <%= "INVIGILATION".equalsIgnoreCase(selectedType) ? "selected" : "" %>><%= I18nUtil.get("jobs.invigilation", lang) %></option>
                        <option value="OTHER" <%= "OTHER".equalsIgnoreCase(selectedType) ? "selected" : "" %>><%= I18nUtil.get("jobs.other", lang) %></option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="description"><%= I18nUtil.get("job.create.desc", lang) %></label>
                    <textarea id="description" name="description" rows="4" required><%= formDescription %></textarea>
                </div>

                <div class="form-group">
                    <label for="requirements"><%= I18nUtil.get("job.create.req", lang) %></label>
                    <textarea id="requirements" name="requirements" rows="3" required><%= formRequirements %></textarea>
                </div>

                <div class="form-group">
                    <label for="requiredSkills"><%= I18nUtil.get("job.create.requiredSkills", lang) %></label>
                    <textarea id="requiredSkills" name="requiredSkills" rows="2"
                              placeholder="<%= I18nUtil.get("job.create.requiredSkillsPlaceholder", lang) %>"><%= formRequiredSkills %></textarea>
                </div>

                <div class="form-group">
                    <label for="workStartDate"><%= I18nUtil.get("job.create.workStartDate", lang) %></label>
                    <input type="date" id="workStartDate" name="workStartDate" required value="<%= formWorkStartDate %>">
                </div>

                <div class="form-group">
                    <label for="workEndDate"><%= I18nUtil.get("job.create.workEndDate", lang) %></label>
                    <input type="date" id="workEndDate" name="workEndDate" required value="<%= formWorkEndDate %>">
                </div>

                <div class="form-group">
                    <label><%= I18nUtil.get("job.create.workWeekdays", lang) %></label>
                    <div style="display: flex; flex-wrap: wrap; gap: 10px;">
                        <label><input type="checkbox" name="workWeekdays" value="MON" <%= selectedWeekdays.contains("MON") ? "checked" : "" %>> <%= I18nUtil.getWeekdayDisplay("MON", lang) %></label>
                        <label><input type="checkbox" name="workWeekdays" value="TUE" <%= selectedWeekdays.contains("TUE") ? "checked" : "" %>> <%= I18nUtil.getWeekdayDisplay("TUE", lang) %></label>
                        <label><input type="checkbox" name="workWeekdays" value="WED" <%= selectedWeekdays.contains("WED") ? "checked" : "" %>> <%= I18nUtil.getWeekdayDisplay("WED", lang) %></label>
                        <label><input type="checkbox" name="workWeekdays" value="THU" <%= selectedWeekdays.contains("THU") ? "checked" : "" %>> <%= I18nUtil.getWeekdayDisplay("THU", lang) %></label>
                        <label><input type="checkbox" name="workWeekdays" value="FRI" <%= selectedWeekdays.contains("FRI") ? "checked" : "" %>> <%= I18nUtil.getWeekdayDisplay("FRI", lang) %></label>
                        <label><input type="checkbox" name="workWeekdays" value="SAT" <%= selectedWeekdays.contains("SAT") ? "checked" : "" %>> <%= I18nUtil.getWeekdayDisplay("SAT", lang) %></label>
                        <label><input type="checkbox" name="workWeekdays" value="SUN" <%= selectedWeekdays.contains("SUN") ? "checked" : "" %>> <%= I18nUtil.getWeekdayDisplay("SUN", lang) %></label>
                    </div>
                </div>

                <div class="form-group">
                    <label for="dailyStartHour"><%= I18nUtil.get("job.create.dailyStartHour", lang) %></label>
                    <select id="dailyStartHour" name="dailyStartHour" required>
                        <option value=""><%= "zh".equals(lang) ? "请选择整点" : "Select hour" %></option>
                        <% for (int h = 0; h < 24; h++) {
                            String hourValue = String.format("%02d:00", h);
                        %>
                            <option value="<%= hourValue %>" <%= hourValue.equals(startHourValue) ? "selected" : "" %>><%= hourValue %></option>
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
                            <option value="<%= hourValue %>" <%= hourValue.equals(endHourValue) ? "selected" : "" %>><%= hourValue %></option>
                        <% } %>
                    </select>
                </div>

                <div class="form-group">
                    <label for="positions"><%= I18nUtil.get("job.create.numPositions", lang) %></label>
                    <input type="number" id="positions" name="positions" min="1" value="<%= formPositions %>" required>
                </div>

                <div class="form-group">
                    <label for="deadline"><%= I18nUtil.get("job.create.deadline", lang) %></label>
                    <input type="date" id="deadline" name="deadline" required value="<%= formDeadline %>">
                </div>

                <div style="display: flex; gap: 15px;">
                    <button type="submit" class="btn btn-primary"><%= editMode ? I18nUtil.get("job.edit.submit", lang) : I18nUtil.get("job.create.submit", lang) %></button>
                    <a href="${pageContext.request.contextPath}/jobs/myjobs" class="btn btn-secondary"><%= I18nUtil.get("job.create.cancel", lang) %></a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>
