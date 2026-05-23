package com.tarecruitment.servlet;

import com.tarecruitment.model.User;
import com.tarecruitment.service.AuthService;
import com.tarecruitment.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/notifications/*")
public class NotificationServlet extends HttpServlet {
    private AuthService authService;
    private NotificationService notificationService;

    @Override
    public void init() throws ServletException {
        this.authService = new AuthService();
        this.notificationService = new NotificationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (!authService.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/applications/my");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (!authService.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        String pathInfo = request.getPathInfo();
        if ("/read".equals(pathInfo)) {
            markRead(request, response, user);
            return;
        }
        if ("/read-all".equals(pathInfo)) {
            markAllRead(request, response, user);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/applications/my");
    }

    private void markRead(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        String notificationId = request.getParameter("notificationId");
        if (notificationId == null || notificationId.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/applications/my?error="
                    + encode("Notification not found"));
            return;
        }
        notificationService.markRead(notificationId.trim(), user.getUserId());
        response.sendRedirect(resolveRedirectTo(request));
    }

    private void markAllRead(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        notificationService.markAllRead(user.getUserId());
        response.sendRedirect(resolveRedirectTo(request));
    }

    private String resolveRedirectTo(HttpServletRequest request) {
        String redirectTo = request.getParameter("redirectTo");
        if (redirectTo == null || redirectTo.trim().isEmpty() || !redirectTo.startsWith("/")) {
            return request.getContextPath() + "/applications/my";
        }
        return request.getContextPath() + redirectTo;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
