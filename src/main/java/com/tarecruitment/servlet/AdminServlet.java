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
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {
    private static final int WORKLOAD_ALERT_THRESHOLD = 2;

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
        if ("/workload/export".equals(pathInfo)) {
            exportWorkloadCsv(request, response);
            return;
        }
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
        request.setAttribute("workloadThreshold", WORKLOAD_ALERT_THRESHOLD);
        request.getRequestDispatcher("/jsp/admin/workload.jsp").forward(request, response);
    }

    private void exportWorkloadCsv(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        List<User> taUsers = userService.getAllTAs();
        Map<String, Integer> workloadMap = applicationService.getTAWorkload();
        taUsers.sort(
                Comparator.comparingInt((User ta) -> workloadMap.getOrDefault(ta.getUserId(), 0))
                        .reversed()
                        .thenComparing(User::getName, String.CASE_INSENSITIVE_ORDER)
        );

        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"ta_workload.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            // BOM helps Excel correctly read UTF-8 CSV.
            writer.write('\uFEFF');
            writer.println("ta_name,email,approved_assignments,is_overloaded,skills");
            for (User ta : taUsers) {
                int approvedCount = workloadMap.getOrDefault(ta.getUserId(), 0);
                boolean overloaded = approvedCount > WORKLOAD_ALERT_THRESHOLD;
                String skills = (ta.getSkills() == null || ta.getSkills().isEmpty())
                        ? ""
                        : String.join(";", ta.getSkills());
                writer.println(
                        escapeCsv(ta.getName()) + ","
                                + escapeCsv(ta.getEmail()) + ","
                                + approvedCount + ","
                                + overloaded + ","
                                + escapeCsv(skills)
                );
            }
            writer.flush();
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "\"\"";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
