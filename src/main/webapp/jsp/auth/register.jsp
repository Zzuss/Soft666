<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <div class="card register-container">
            <h1 class="text-center">Create Account</h1>
            <p class="text-center" style="color: #718096; margin-bottom: 30px;">Join TA Recruitment System</p>
            
            <% if (request.getAttribute("error") != null) { %>
                <div class="error"><%= request.getAttribute("error") %></div>
            <% } %>
            
            <form action="${pageContext.request.contextPath}/auth" method="post">
                <input type="hidden" name="action" value="register">
                
                <div class="form-group">
                    <label for="username">Username *</label>
                    <input type="text" id="username" name="username" required>
                </div>
                
                <div class="form-group">
                    <label for="password">Password * (at least 6 characters)</label>
                    <input type="password" id="password" name="password" required minlength="6">
                </div>
                
                <div class="form-group">
                    <label for="confirmPassword">Confirm Password *</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" required>
                </div>
                
                <div class="form-group">
                    <label for="name">Full Name *</label>
                    <input type="text" id="name" name="name" required>
                </div>
                
                <div class="form-group">
                    <label for="email">Email *</label>
                    <input type="email" id="email" name="email" required>
                </div>
                
                <div class="form-group">
                    <label for="role">Register as *</label>
                    <select id="role" name="role" required>
                        <option value="TA">Teaching Assistant (TA)</option>
                        <option value="MO">Module Organiser (MO)</option>
                    </select>
                </div>
                
                <button type="submit" class="btn btn-primary btn-block">Register</button>
            </form>
            
            <p class="text-center mt-20">
                Already have an account? <a href="${pageContext.request.contextPath}/auth?action=login" class="link">Login here</a>
            </p>
        </div>
    </div>
</body>
</html>