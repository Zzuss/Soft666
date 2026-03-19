package com.tarecruitment.servlet;

import com.tarecruitment.model.Application;
import com.tarecruitment.model.Job;
import com.tarecruitment.model.User;
import com.tarecruitment.service.ApplicationService;
import com.tarecruitment.service.AuthService;
import com.tarecruitment.service.JobService;
import com.tarecruitment.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/applications/*")
public class ApplicationServlet extends HttpServlet {
    private ApplicationService applicationService;
    private JobService jobService;
    private UserService userService;
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        this.applicationService = new ApplicationService();
        this.jobService = new JobService();
        this.userService = new UserService();
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!authService.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
            listApplications(request, response);
        } else if (pathInfo.equals("/my")) {
            listMyApplications(request, response);
        } else if (pathInfo.equals("/manage")) {
            manageApplications(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/applications/my");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!authService.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        String pathInfo = request.getPathInfo();

        if ("/apply".equals(pathInfo)) {
            applyForJob(request, response);
        } else if ("/approve".equals(pathInfo)) {
            approveApplication(request, response);
        } else if ("/reject".equals(pathInfo)) {
            rejectApplication(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/applications/my");
        }
    }

    private void listApplications(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession(false).getAttribute("user");
        
        if (user.isTA()) {
            List<Application> applications = applicationService.getUserApplications(user.getUserId());
            request.setAttribute("applications", applications);
            request.getRequestDispatcher("/jsp/applications/list.jsp").forward(request, response);
        } else {
            List<Application> allApps = applicationService.getUserApplications(user.getUserId());
            request.setAttribute("applications", allApps);
            request.getRequestDispatcher("/jsp/applications/list.jsp").forward(request, response);
        }
    }

    private void listMyApplications(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession(false).getAttribute("user");
        List<Application> applications = applicationService.getUserApplications(user.getUserId());
        
        for (Application app : applications) {
            Job job = jobService.getJobById(app.getJobId());
            request.setAttribute("job_" + app.getJobId(), job);
        }
        
        request.setAttribute("applications", applications);
        request.getRequestDispatcher("/jsp/applications/my.jsp").forward(request, response);
    }

    private void manageApplications(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String jobId = request.getParameter("jobId");
        
        if (jobId == null || jobId.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/jobs/myjobs");
            return;
        }
        
        List<Application> applications = applicationService.getJobApplications(jobId);
        
        for (Application app : applications) {
            User applicant = userService.getUserById(app.getUserId());
            request.setAttribute("user_" + app.getUserId(), applicant);
        }
        
        Job job = jobService.getJobById(jobId);
        request.setAttribute("job", job);
        request.setAttribute("applications", applications);
        request.getRequestDispatcher("/jsp/applications/manage.jsp").forward(request, response);
    }

    private void applyForJob(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = (User) request.getSession(false).getAttribute("user");
            String jobId = request.getParameter("jobId");

            applicationService.applyForJob(jobId, user.getUserId());
            
            response.sendRedirect(request.getContextPath() + "/applications/my?success=Application submitted successfully");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/jobs/list?error=" + e.getMessage());
        }
    }

    private void approveApplication(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = (User) request.getSession(false).getAttribute("user");
            String applicationId = request.getParameter("id");
            String jobId = request.getParameter("jobId");

            applicationService.approveApplication(applicationId, user.getUserId());
            
            response.sendRedirect(request.getContextPath() + "/applications/manage?jobId=" + jobId + "&success=Application approved");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/applications/manage?error=" + e.getMessage());
        }
    }

    private void rejectApplication(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = (User) request.getSession(false).getAttribute("user");
            String applicationId = request.getParameter("id");
            String jobId = request.getParameter("jobId");

            applicationService.rejectApplication(applicationId, user.getUserId());
            
            response.sendRedirect(request.getContextPath() + "/applications/manage?jobId=" + jobId + "&success=Application rejected");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/applications/manage?error=" + e.getMessage());
        }
    }
}