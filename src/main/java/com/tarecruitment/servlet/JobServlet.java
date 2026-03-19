package com.tarecruitment.servlet;

import com.tarecruitment.model.Job;
import com.tarecruitment.model.User;
import com.tarecruitment.service.AuthService;
import com.tarecruitment.service.JobService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/jobs/*")
public class JobServlet extends HttpServlet {
    private JobService jobService;
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        this.jobService = new JobService();
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
            listJobs(request, response);
        } else if (pathInfo.equals("/create")) {
            showCreateForm(request, response);
        } else if (pathInfo.equals("/detail")) {
            showJobDetail(request, response);
        } else if (pathInfo.equals("/myjobs")) {
            listMyJobs(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/jobs/list");
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

        if ("/create".equals(pathInfo)) {
            createJob(request, response);
        } else if ("/close".equals(pathInfo)) {
            closeJob(request, response);
        } else if ("/delete".equals(pathInfo)) {
            deleteJob(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/jobs/list");
        }
    }

    private void listJobs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String type = request.getParameter("type");

        List<Job> jobs = jobService.searchJobs(keyword, type);
        request.setAttribute("jobs", jobs);
        request.setAttribute("keyword", keyword);
        request.setAttribute("type", type);
        request.getRequestDispatcher("/jsp/jobs/list.jsp").forward(request, response);
    }

    private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession(false).getAttribute("user");
        if (!user.isMO() && !user.isAdmin()) {
            request.setAttribute("error", "Only MO or Admin can create jobs");
            request.getRequestDispatcher("/jsp/jobs/list.jsp").forward(request, response);
            return;
        }
        request.getRequestDispatcher("/jsp/jobs/create.jsp").forward(request, response);
    }

    private void showJobDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String jobId = request.getParameter("id");
        Job job = jobService.getJobById(jobId);
        
        if (job == null) {
            response.sendRedirect(request.getContextPath() + "/jobs/list");
            return;
        }
        
        request.setAttribute("job", job);
        request.getRequestDispatcher("/jsp/jobs/detail.jsp").forward(request, response);
    }

    private void listMyJobs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession(false).getAttribute("user");
        List<Job> jobs = jobService.getJobsByMo(user.getUserId());
        request.setAttribute("jobs", jobs);
        request.getRequestDispatcher("/jsp/jobs/myjobs.jsp").forward(request, response);
    }

    private void createJob(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = (User) request.getSession(false).getAttribute("user");
            
            String title = request.getParameter("title");
            String type = request.getParameter("type");
            String description = request.getParameter("description");
            String requirements = request.getParameter("requirements");
            int positions = Integer.parseInt(request.getParameter("positions"));
            String deadline = request.getParameter("deadline");

            jobService.createJob(title, type, description, requirements, positions, deadline, user.getUserId());
            
            response.sendRedirect(request.getContextPath() + "/jobs/myjobs?success=Job created successfully");
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/jsp/jobs/create.jsp").forward(request, response);
        }
    }

    private void closeJob(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String jobId = request.getParameter("id");
            jobService.closeJob(jobId);
            response.sendRedirect(request.getContextPath() + "/jobs/myjobs?success=Job closed successfully");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/jobs/myjobs?error=" + e.getMessage());
        }
    }

    private void deleteJob(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String jobId = request.getParameter("id");
            jobService.deleteJob(jobId);
            response.sendRedirect(request.getContextPath() + "/jobs/myjobs?success=Job deleted successfully");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/jobs/myjobs?error=" + e.getMessage());
        }
    }
}