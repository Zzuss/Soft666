<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reset Password - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <div class="card login-container">
            <h1 class="text-center">Reset Password</h1>
            <p class="text-center" style="color: #718096; margin-bottom: 30px;">Verify your username and registered email</p>
            <jsp:include page="/jsp/common/system-warning.jsp" />

            <% if (request.getAttribute("error") != null) { %>
                <div class="error"><%= request.getAttribute("error") %></div>
            <% } %>

            <form action="${pageContext.request.contextPath}/auth" method="post">
                <input type="hidden" name="action" value="resetPassword">

                <div class="form-group">
                    <label for="username">Username</label>
                    <input type="text" id="username" name="username" required value="<%= request.getAttribute("username") != null ? request.getAttribute("username") : "" %>">
                </div>

                <div class="form-group">
                    <label for="email">Registered Email</label>
                    <input type="email" id="email" name="email" required value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>">
                </div>

                <div class="form-group">
                    <label for="newPassword">New Password (at least 6 characters)</label>
                    <input type="password" id="newPassword" name="newPassword" minlength="6" required>
                </div>

                <div class="form-group">
                    <label for="confirmPassword">Confirm New Password</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" minlength="6" required>
                </div>

                <button type="submit" class="btn btn-primary btn-block">Reset Password</button>
            </form>

            <p class="text-center mt-20">
                <a href="${pageContext.request.contextPath}/auth?action=login" class="link">Back to Login</a>
            </p>
        </div>
    </div>
</body>
</html>
