package com.tarecruitment.servlet;

import com.tarecruitment.model.User;
import com.tarecruitment.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("logout".equals(action)) {
            HttpSession session = request.getSession(false);
            authService.logout(session);
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        if ("login".equals(action) || action == null) {
            request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
        } else if ("register".equals(action)) {
            request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
        } else if ("forgotPassword".equals(action)) {
            request.getRequestDispatcher("/jsp/auth/forgot-password.jsp").forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("login".equals(action)) {
            handleLogin(request, response);
        } else if ("register".equals(action)) {
            handleRegister(request, response);
        } else if ("resetPassword".equals(action)) {
            handleResetPassword(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        User user = authService.login(username, password);

        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            session.setAttribute("name", user.getName());

            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            request.setAttribute("error", "Invalid username or password");
            request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String role = request.getParameter("role");

            if (!password.equals(confirmPassword)) {
                request.setAttribute("error", "Passwords do not match");
                request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
                return;
            }

            User user = authService.register(username, password, name, email, role);

            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            session.setAttribute("name", user.getName());

            response.sendRedirect(request.getContextPath() + "/dashboard");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
        }
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        request.setAttribute("username", username);
        request.setAttribute("email", email);

        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match");
            request.getRequestDispatcher("/jsp/auth/forgot-password.jsp").forward(request, response);
            return;
        }

        try {
            authService.resetPasswordByUsernameEmail(username, email, newPassword);
            response.sendRedirect(request.getContextPath() + "/auth?action=login&success="
                    + URLEncoder.encode("Password reset successfully. Please login with your new password.", StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/auth/forgot-password.jsp").forward(request, response);
        }
    }
}
