<%@ page import="com.tarecruitment.util.JsonUtil" %>
<%
    String systemWarning = JsonUtil.consumeRecoveryWarning();
    if (systemWarning != null && !systemWarning.trim().isEmpty()) {
%>
    <div class="alert alert-error"><%= systemWarning %></div>
<%
    }
%>
