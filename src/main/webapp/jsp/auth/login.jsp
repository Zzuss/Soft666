<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="auth-body">
    <div class="auth-shell">
        <section class="auth-visual-panel" aria-label="TA recruitment login illustration">
            <div class="auth-visual-decoration auth-visual-decoration-large"></div>
            <div class="auth-visual-decoration auth-visual-decoration-small"></div>
            <img class="auth-illustration" src="${pageContext.request.contextPath}/images/login-illustration.svg" alt="Secure login illustration">
            <div class="auth-visual-copy">
                <h2>Turn applications into opportunities.</h2>
                <p>Manage TA recruitment with a clear, secure workflow.</p>
            </div>
        </section>

        <main class="auth-form-panel">
            <div class="auth-form-card">
                <div class="auth-logo" aria-hidden="true">
                    <span></span>
                    <span></span>
                    <span></span>
                    <span></span>
                </div>
                <h1>Login to your Account</h1>
                <p class="auth-subtitle">BUPT International School TA Recruitment System</p>

                <jsp:include page="/jsp/common/system-warning.jsp" />

                <% if (request.getAttribute("error") != null) { %>
                    <div class="error"><%= request.getAttribute("error") %></div>
                <% } %>
                <% if (request.getParameter("success") != null) { %>
                    <div class="success"><%= request.getParameter("success") %></div>
                <% } %>

                <form action="${pageContext.request.contextPath}/auth" method="post" class="auth-form">
                    <input type="hidden" name="action" value="login">

                    <div class="form-group">
                        <label for="username">Username</label>
                        <input type="text" id="username" name="username" placeholder="Enter your username" autocomplete="username" required>
                    </div>

                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" placeholder="Enter your password" autocomplete="current-password" required>
                    </div>

                    <div class="auth-form-row">
                        <label class="auth-remember">
                            <input type="checkbox" name="remember">
                            <span>Remember me</span>
                        </label>
                        <a href="${pageContext.request.contextPath}/auth?action=forgotPassword" class="link">Forgot Password?</a>
                    </div>

                    <button type="submit" class="btn btn-primary btn-block">Login</button>
                </form>

                <p class="auth-footer">
                    Not registered yet? <a href="${pageContext.request.contextPath}/auth?action=register" class="link">Create an account</a>
                </p>
            </div>
        </main>
    </div>
</body>
</html>
