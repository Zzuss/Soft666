package com.tarecruitment.servlet;

import com.tarecruitment.model.User;
import com.tarecruitment.service.ApplicationService;
import com.tarecruitment.service.AuthService;
import com.tarecruitment.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {
    private AuthService authService;
    private UserService userService;
    private ApplicationService applicationService;

    @Override
    public void init() throws ServletException {
        this.authService = new AuthService();
        this.userService = new UserService();
        this.applicationService = new ApplicationService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!authService.isLoggedIn(session)) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !currentUser.isAdmin()) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || "/".equals(pathInfo) || "/workload".equals(pathInfo)) {
            showWorkload(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/admin/workload");
    }

    private void showWorkload(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<User> taUsers = userService.getAllTAs();
        Map<String, Integer> workloadMap = applicationService.getTAWorkload();
        taUsers.sort(
                Comparator.comparingInt((User ta) -> workloadMap.getOrDefault(ta.getUserId(), 0))
                        .reversed()
                        .thenComparing(User::getName, String.CASE_INSENSITIVE_ORDER)
        );

        request.setAttribute("taUsers", taUsers);
        request.setAttribute("workloadMap", workloadMap);
        request.getRequestDispatcher("/jsp/admin/workload.jsp").forward(request, response);
    }
}
