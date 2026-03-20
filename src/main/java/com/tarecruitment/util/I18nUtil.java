package com.tarecruitment.util;

import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class I18nUtil {
    private static final Map<String, Map<String, String>> translations = new HashMap<>();

    static {
        Map<String, String> en = new HashMap<>();
        en.put("app.title", "TA Recruitment System");
        en.put("nav.dashboard", "Dashboard");
        en.put("nav.jobs", "Jobs");
        en.put("nav.myApplications", "My Applications");
        en.put("nav.myPostedJobs", "My Posted Jobs");
        en.put("nav.profile", "My Profile");
        en.put("nav.workload", "TA Workload");
        en.put("nav.logout", "Logout");
        en.put("login.title", "Login");
        en.put("login.username", "Username");
        en.put("login.password", "Password");
        en.put("login.submit", "Login");
        en.put("login.noAccount", "Don't have an account?");
        en.put("login.registerHere", "Register here");
        en.put("login.bupt", "BUPT International School");

        en.put("register.title", "Create Account");
        en.put("register.join", "Join TA Recruitment System");
        en.put("register.username", "Username *");
        en.put("register.password", "Password * (at least 6 characters)");
        en.put("register.confirmPassword", "Confirm Password *");
        en.put("register.name", "Full Name *");
        en.put("register.email", "Email *");
        en.put("register.role", "Register as *");
        en.put("register.ta", "Teaching Assistant (TA)");
        en.put("register.mo", "Module Organiser (MO)");
        en.put("register.submit", "Register");
        en.put("register.hasAccount", "Already have an account?");
        en.put("register.loginHere", "Login here");

        en.put("dashboard.welcome", "Welcome");
        en.put("dashboard.openPositions", "Open Positions");
        en.put("dashboard.myPostedJobs", "My Posted Jobs");
        en.put("dashboard.myApplications", "My Applications");
        en.put("dashboard.quickActions", "Quick Actions");
        en.put("dashboard.browseJobs", "Browse Jobs");
        en.put("dashboard.postNewJob", "Post New Job");
        en.put("dashboard.manageJobs", "Manage My Jobs");
        en.put("dashboard.myProfile", "My Profile");
        en.put("dashboard.taWorkload", "View TA Workload");
        en.put("dashboard.roleInfo", "Role Information");
        en.put("dashboard.taRole", "As a Teaching Assistant (TA), you can browse available positions and apply for jobs that match your skills.");
        en.put("dashboard.moRole", "As a Module Organiser (MO), you can post job positions and review applications from TA candidates.");
        en.put("dashboard.adminRole", "As an Administrator, you have full access to manage the system and view all data.");

        en.put("profile.title", "Applicant Profile");
        en.put("profile.basicInfo", "Basic Information");
        en.put("profile.name", "Full Name");
        en.put("profile.email", "Email");
        en.put("profile.skills", "Skills (comma separated)");
        en.put("profile.availableTime", "Available Time");
        en.put("profile.bio", "Personal Statement");
        en.put("profile.bioPlaceholder", "Briefly describe your strengths and experience");
        en.put("profile.update", "Save Profile");
        en.put("profile.resume", "Resume");
        en.put("profile.resumeHint", "Accepted formats: PDF, DOC, DOCX. Max size: 5MB.");
        en.put("profile.uploadResume", "Upload Resume");
        en.put("profile.currentResume", "Current Resume");
        en.put("profile.updatedAt", "Updated At");
        en.put("profile.downloadResume", "Download Resume");
        en.put("profile.noResume", "No resume uploaded yet.");

        en.put("admin.workload.title", "TA Overall Workload");
        en.put("admin.workload.taName", "TA Name");
        en.put("admin.workload.email", "Email");
        en.put("admin.workload.skills", "Skills");
        en.put("admin.workload.approved", "Approved Assignments");
        en.put("admin.workload.noData", "No TA records found.");

        en.put("jobs.available", "Available Positions");
        en.put("jobs.search", "Search jobs...");
        en.put("jobs.allTypes", "All Types");
        en.put("jobs.moduleTutor", "Module Tutor");
        en.put("jobs.invigilation", "Invigilation");
        en.put("jobs.other", "Other");
        en.put("jobs.searchBtn", "Search");
        en.put("jobs.positions", "Positions");
        en.put("jobs.deadline", "Deadline");
        en.put("jobs.viewDetails", "View Details");
        en.put("jobs.noJobs", "No jobs found. Please try a different search or check back later.");

        en.put("job.detail.title", "Job Details");
        en.put("job.detail.type", "Type");
        en.put("job.detail.description", "Description");
        en.put("job.detail.requirements", "Requirements");
        en.put("job.detail.noDesc", "No description provided.");
        en.put("job.detail.noReq", "No requirements specified.");
        en.put("job.detail.apply", "Apply Now");
        en.put("job.detail.back", "Back to Jobs");

        en.put("job.create.title", "Post New Job");
        en.put("job.create.jobTitle", "Job Title *");
        en.put("job.create.type", "Job Type *");
        en.put("job.create.desc", "Description *");
        en.put("job.create.req", "Requirements");
        en.put("job.create.numPositions", "Number of Positions *");
        en.put("job.create.deadline", "Application Deadline *");
        en.put("job.create.submit", "Post Job");
        en.put("job.create.cancel", "Cancel");

        en.put("job.my.title", "My Posted Jobs");
        en.put("job.my.postNew", "Post New Job");
        en.put("job.my.viewApps", "View Applications");
        en.put("job.my.close", "Close");
        en.put("job.my.confirmClose", "Close this job?");
        en.put("job.my.noJobs", "You haven't posted any jobs yet.");
        en.put("job.my.firstJob", "Post Your First Job");

        en.put("app.my.title", "My Applications");
        en.put("app.my.jobTitle", "Job Title");
        en.put("app.my.type", "Type");
        en.put("app.my.appliedDate", "Applied Date");
        en.put("app.my.status", "Status");
        en.put("app.my.action", "Action");
        en.put("app.my.viewJob", "View Job");
        en.put("app.my.noApps", "You haven't applied for any jobs yet.");
        en.put("app.my.browse", "Browse Jobs");

        en.put("app.manage.title", "Manage Applications");
        en.put("app.manage.job", "Job");
        en.put("app.manage.applicantName", "Applicant Name");
        en.put("app.manage.email", "Email");
        en.put("app.manage.appliedDate", "Applied Date");
        en.put("app.manage.status", "Status");
        en.put("app.manage.statusAll", "All Statuses");
        en.put("app.manage.keyword", "Search name, email, skills...");
        en.put("app.manage.filter", "Filter");
        en.put("app.manage.skills", "Skills");
        en.put("app.manage.availableTime", "Available Time");
        en.put("app.manage.actions", "Actions");
        en.put("app.manage.approve", "Approve");
        en.put("app.manage.reject", "Reject");
        en.put("app.manage.processed", "Processed");
        en.put("app.manage.noApps", "No applications received for this job yet.");
        en.put("app.manage.back", "Back to My Jobs");

        en.put("status.open", "Open");
        en.put("status.closed", "Closed");
        en.put("status.pending", "Pending");
        en.put("status.approved", "Approved");
        en.put("status.rejected", "Rejected");

        translations.put("en", en);

        Map<String, String> zh = new HashMap<>();
        zh.put("app.title", "助教招聘系统");
        zh.put("nav.dashboard", "控制面板");
        zh.put("nav.jobs", "职位");
        zh.put("nav.myApplications", "我的申请");
        zh.put("nav.myPostedJobs", "我发布的职位");
        zh.put("nav.profile", "我的档案");
        zh.put("nav.workload", "助教工作负荷");
        zh.put("nav.logout", "退出");
        zh.put("login.title", "登录");
        zh.put("login.username", "用户名");
        zh.put("login.password", "密码");
        zh.put("login.submit", "登录");
        zh.put("login.noAccount", "还没有账号？");
        zh.put("login.registerHere", "立即注册");
        zh.put("login.bupt", "北京邮电大学国际学院");

        zh.put("register.title", "创建账号");
        zh.put("register.join", "加入助教招聘系统");
        zh.put("register.username", "用户名 *");
        zh.put("register.password", "密码 *（至少6个字符）");
        zh.put("register.confirmPassword", "确认密码 *");
        zh.put("register.name", "姓名 *");
        zh.put("register.email", "邮箱 *");
        zh.put("register.role", "注册为 *");
        zh.put("register.ta", "助教 (TA)");
        zh.put("register.mo", "课程管理员 (MO)");
        zh.put("register.submit", "注册");
        zh.put("register.hasAccount", "已有账号？");
        zh.put("register.loginHere", "立即登录");

        zh.put("dashboard.welcome", "欢迎");
        zh.put("dashboard.openPositions", "开放职位");
        zh.put("dashboard.myPostedJobs", "我发布的职位");
        zh.put("dashboard.myApplications", "我的申请");
        zh.put("dashboard.quickActions", "快捷操作");
        zh.put("dashboard.browseJobs", "浏览职位");
        zh.put("dashboard.postNewJob", "发布新职位");
        zh.put("dashboard.manageJobs", "管理我的职位");
        zh.put("dashboard.myProfile", "我的档案");
        zh.put("dashboard.taWorkload", "查看助教负荷");
        zh.put("dashboard.roleInfo", "角色信息");
        zh.put("dashboard.taRole", "作为助教 (TA)，您可以浏览可用职位并申请与您技能相匹配的职位。");
        zh.put("dashboard.moRole", "作为课程管理员 (MO)，您可以发布职位并审核来自助教候选人的申请。");
        zh.put("dashboard.adminRole", "作为管理员，您拥有系统的完全访问权限，可以管理所有数据。");

        zh.put("profile.title", "申请人档案");
        zh.put("profile.basicInfo", "基础信息");
        zh.put("profile.name", "姓名");
        zh.put("profile.email", "邮箱");
        zh.put("profile.skills", "技能（逗号分隔）");
        zh.put("profile.availableTime", "可用时间");
        zh.put("profile.bio", "个人陈述");
        zh.put("profile.bioPlaceholder", "简要描述你的优势与经历");
        zh.put("profile.update", "保存档案");
        zh.put("profile.resume", "简历");
        zh.put("profile.resumeHint", "支持 PDF、DOC、DOCX，最大 5MB。");
        zh.put("profile.uploadResume", "上传简历");
        zh.put("profile.currentResume", "当前简历");
        zh.put("profile.updatedAt", "更新时间");
        zh.put("profile.downloadResume", "下载简历");
        zh.put("profile.noResume", "暂未上传简历。");

        zh.put("admin.workload.title", "助教整体工作负荷");
        zh.put("admin.workload.taName", "助教姓名");
        zh.put("admin.workload.email", "邮箱");
        zh.put("admin.workload.skills", "技能");
        zh.put("admin.workload.approved", "已批准分配数");
        zh.put("admin.workload.noData", "暂无助教数据。");

        zh.put("jobs.available", "可用职位");
        zh.put("jobs.search", "搜索职位...");
        zh.put("jobs.allTypes", "所有类型");
        zh.put("jobs.moduleTutor", "课程导师");
        zh.put("jobs.invigilation", "监考");
        zh.put("jobs.other", "其他");
        zh.put("jobs.searchBtn", "搜索");
        zh.put("jobs.positions", "职位数");
        zh.put("jobs.deadline", "截止日期");
        zh.put("jobs.viewDetails", "查看详情");
        zh.put("jobs.noJobs", "未找到职位。请尝试其他搜索条件或稍后再来。");

        zh.put("job.detail.title", "职位详情");
        zh.put("job.detail.type", "类型");
        zh.put("job.detail.description", "职位描述");
        zh.put("job.detail.requirements", "任职要求");
        zh.put("job.detail.noDesc", "暂无描述。");
        zh.put("job.detail.noReq", "暂无要求。");
        zh.put("job.detail.apply", "立即申请");
        zh.put("job.detail.back", "返回职位列表");

        zh.put("job.create.title", "发布新职位");
        zh.put("job.create.jobTitle", "职位标题 *");
        zh.put("job.create.type", "职位类型 *");
        zh.put("job.create.desc", "职位描述 *");
        zh.put("job.create.req", "任职要求");
        zh.put("job.create.numPositions", "招聘人数 *");
        zh.put("job.create.deadline", "申请截止日期 *");
        zh.put("job.create.submit", "发布职位");
        zh.put("job.create.cancel", "取消");

        zh.put("job.my.title", "我发布的职位");
        zh.put("job.my.postNew", "发布新职位");
        zh.put("job.my.viewApps", "查看申请");
        zh.put("job.my.close", "关闭");
        zh.put("job.my.confirmClose", "确定关闭此职位？");
        zh.put("job.my.noJobs", "您还没有发布任何职位。");
        zh.put("job.my.firstJob", "发布第一个职位");

        zh.put("app.my.title", "我的申请");
        zh.put("app.my.jobTitle", "职位名称");
        zh.put("app.my.type", "类型");
        zh.put("app.my.appliedDate", "申请日期");
        zh.put("app.my.status", "状态");
        zh.put("app.my.action", "操作");
        zh.put("app.my.viewJob", "查看职位");
        zh.put("app.my.noApps", "您还没有申请任何职位。");
        zh.put("app.my.browse", "浏览职位");

        zh.put("app.manage.title", "管理申请");
        zh.put("app.manage.job", "职位");
        zh.put("app.manage.applicantName", "申请人姓名");
        zh.put("app.manage.email", "邮箱");
        zh.put("app.manage.appliedDate", "申请日期");
        zh.put("app.manage.status", "状态");
        zh.put("app.manage.statusAll", "全部状态");
        zh.put("app.manage.keyword", "搜索姓名、邮箱、技能...");
        zh.put("app.manage.filter", "筛选");
        zh.put("app.manage.skills", "技能");
        zh.put("app.manage.availableTime", "可用时间");
        zh.put("app.manage.actions", "操作");
        zh.put("app.manage.approve", "批准");
        zh.put("app.manage.reject", "拒绝");
        zh.put("app.manage.processed", "已处理");
        zh.put("app.manage.noApps", "该职位还没有收到任何申请。");
        zh.put("app.manage.back", "返回我的职位");

        zh.put("status.open", "开放");
        zh.put("status.closed", "已关闭");
        zh.put("status.pending", "待审核");
        zh.put("status.approved", "已批准");
        zh.put("status.rejected", "已拒绝");

        translations.put("zh", zh);
    }

    public static String get(String key) {
        return get(key, "en");
    }

    public static String get(String key, String lang) {
        Map<String, String> langMap = translations.get(lang);
        if (langMap == null) {
            langMap = translations.get("en");
        }
        return langMap != null ? langMap.getOrDefault(key, key) : key;
    }

    public static String getLanguage(HttpServletRequest request) {
        String paramLang = request.getParameter("lang");
        if (paramLang != null && (paramLang.equals("en") || paramLang.equals("zh"))) {
            HttpSession session = request.getSession(true);
            session.setAttribute("language", paramLang);
            return paramLang;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object lang = session.getAttribute("language");
            if ("en".equals(lang) || "zh".equals(lang)) {
                return lang.toString();
            }
        }
        String headerLang = request.getHeader("Accept-Language");
        if (headerLang != null && headerLang.contains("zh")) {
            return "zh";
        }
        return "en";
    }

    public static void setLanguage(HttpServletRequest request, String lang) {
        HttpSession session = request.getSession(true);
        if (lang != null && (lang.equals("en") || lang.equals("zh"))) {
            session.setAttribute("language", lang);
        }
    }
}
