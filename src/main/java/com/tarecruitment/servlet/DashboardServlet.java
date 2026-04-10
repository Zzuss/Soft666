package com.tarecruitment.servlet;

import com.tarecruitment.model.User;
import com.tarecruitment.service.AuthService;
import com.tarecruitment.service.ApplicationService;
import com.tarecruitment.service.JobService;
import com.tarecruitment.service.NotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private AuthService authService;
    private JobService jobService;
    private ApplicationService applicationService;
    private NotificationService notificationService;

    @Override
    public void init() throws ServletException {
        this.authService = new AuthService();
        this.jobService = new JobService();
        this.applicationService = new ApplicationService();
        this.notificationService = new NotificationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!authService.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        User user = (User) session.getAttribute("user");
        
        request.setAttribute("user", user);
        request.setAttribute("openJobsCount", jobService.getOpenJobs().size());
        request.setAttribute("myJobsCount", jobService.getJobsByMo(user.getUserId()).size());
        
        if (user.isTA()) {
            request.setAttribute("myApplicationsCount", applicationService.getUserApplications(user.getUserId()).size());
            request.setAttribute("unreadNotificationCount", notificationService.countUnread(user.getUserId()));
        }
        
        request.getRequestDispatcher("/jsp/dashboard.jsp").forward(request, response);
    }
}
