# EBU6304 课程项目 - 技术架构文档

## 教学助理招聘系统（TA Recruitment System）

---

## 1. 技术选型

### 1.1 架构选择
**轻量级Java Servlet/JSP Web应用**

选型理由：
- 完全符合课程架构限制（无Spring Boot、无数据库）
- 使用Java标准Servlet API，无需额外框架依赖
- JSP适合快速开发简单Web页面
- 易于理解和学习

### 1.2 技术栈
| 组件 | 技术 |
|------|------|
| Web容器 | Apache Tomcat 10.x |
| Java版本 | JDK 17 |
| 前端技术 | HTML5 + CSS3 + JavaScript |
| 模板引擎 | JSP |
| 数据存储 | JSON文件 |
| JSON处理 | org.json库 |
| 构建工具 | Maven |

### 1.3 项目结构
```
TARecruitmentSystem/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── tarecruitment/
│       │           ├── Main.java
│       │           ├── model/
│       │           │   ├── User.java
│       │           │   ├── Job.java
│       │           │   ├── Application.java
│       │           │   └── Resume.java
│       │           ├── dao/
│       │           │   ├── UserDAO.java
│       │           │   ├── JobDAO.java
│       │           │   ├── ApplicationDAO.java
│       │           │   └── ResumeDAO.java
│       │           ├── service/
│       │           │   ├── UserService.java
│       │           │   ├── JobService.java
│       │           │   ├── ApplicationService.java
│       │           │   └── AuthService.java
│       │           ├── servlet/
│       │           │   ├── AuthServlet.java
│       │           │   ├── UserServlet.java
│       │           │   ├── JobServlet.java
│       │           │   ├── ApplicationServlet.java
│       │           │   └── AdminServlet.java
│       │           └── util/
│       │               ├── JsonUtil.java
│       │               ├── PasswordUtil.java
│       │               └── FileUtil.java
│       └── webapp/
│           ├── index.jsp
│           ├── login.jsp
│           ├── register.jsp
│           ├── dashboard.jsp
│           ├── jobs/
│           │   ├── list.jsp
│           │   ├── detail.jsp
│           │   └── create.jsp
│           ├── applications/
│           │   ├── list.jsp
│           │   └── manage.jsp
│           ├── profile/
│           │   └── edit.jsp
│           ├── css/
│           │   └── style.css
│           └── js/
│               └── main.js
├── data/
│   ├── users.json
│   ├── jobs.json
│   ├── applications.json
│   └── resumes/
│       └── (上传的简历文件)
├── pom.xml
└── README.md
```

---

## 2. 系统架构图

### 2.1 整体架构
```
┌─────────────────────────────────────────────────────────┐
│                    Web浏览器                             │
└─────────────────────┬───────────────────────────────────┘
                      │ HTTP请求
                      ▼
┌─────────────────────────────────────────────────────────┐
│                  Servlet容器 (Tomcat)                    │
│  ┌─────────────────────────────────────────────────┐    │
│  │              Servlet处理层                       │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐        │    │
│  │  │AuthServlet│ │JobServlet│ │AppServlet│        │    │
│  │  └──────────┘ └──────────┘ └──────────┘        │    │
│  └─────────────────────────────────────────────────┘    │
│                          │                               │
│                          ▼                               │
│  ┌─────────────────────────────────────────────────┐    │
│  │              Service业务层                       │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐        │    │
│  │  │UserService│ │JobService│ │AppService│        │    │
│  │  └──────────┘ └──────────┘ └──────────┘        │    │
│  └─────────────────────────────────────────────────┘    │
│                          │                               │
│                          ▼                               │
│  ┌─────────────────────────────────────────────────┐    │
│  │              DAO数据访问层                       │    │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐        │    │
│  │  │ UserDAO  │ │  JobDAO  │ │  AppDAO  │        │    │
│  │  └──────────┘ └──────────┘ └──────────┘        │    │
│  └─────────────────────────────────────────────────┘    │
│                          │                               │
└──────────────────────────┼───────────────────────────────┘
                           ▼
┌─────────────────────────────────────────────────────────┐
│                  数据存储层                              │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐                │
│  │users.json│ │jobs.json │ │apps.json │                │
│  └──────────┘ └──────────┘ └──────────┘                │
│  ┌──────────┐                                           │
│  │ resumes/ │  (文件系统)                               │
│  └──────────┘                                           │
└─────────────────────────────────────────────────────────┘
```

### 2.2 请求处理流程
```
用户请求 → Servlet → Service → DAO → JSON文件
                ↑              ↓
                └──────────────┘
                     响应数据
```

---

## 3. 核心组件设计

### 3.1 数据模型（Model）

#### User.java
```java
public class User {
    private String userId;
    private String username;
    private String password;
    private String name;
    private String email;
    private String role;        // TA, MO, ADMIN
    private List<String> skills;
    private String availableTime;
    private boolean enabled;
    private Timestamp createdAt;
}
```

#### Job.java
```java
public class Job {
    private String jobId;
    private String title;
    private String type;        // MODULE, INVIGILATION, OTHER
    private String description;
    private String requirements;
    private int positions;
    private String postedBy;
    private String deadline;
    private String status;      // OPEN, CLOSED
    private Timestamp createdAt;
}
```

#### Application.java
```java
public class Application {
    private String applicationId;
    private String jobId;
    private String userId;
    private String status;      // PENDING, APPROVED, REJECTED
    private Timestamp appliedAt;
    private String reviewedBy;
    private Timestamp reviewedAt;
}
```

### 3.2 数据访问层（DAO）

#### UserDAO.java
```java
public class UserDAO {
    private static final String DATA_FILE = "data/users.json";

    public void addUser(User user) { }
    public void updateUser(User user) { }
    public void deleteUser(String userId) { }
    public User getUserById(String userId) { }
    public User getUserByUsername(String username) { }
    public List<User> getAllUsers() { }
    public List<User> getUsersByRole(String role) { }
    public List<User> getTAs() { }
}
```

#### JobDAO.java
```java
public class JobDAO {
    private static final String DATA_FILE = "data/jobs.json";

    public void addJob(Job job) { }
    public void updateJob(Job job) { }
    public void deleteJob(String jobId) { }
    public Job getJobById(String jobId) { }
    public List<Job> getAllJobs() { }
    public List<Job> getOpenJobs() { }
    public List<Job> getJobsByMo(String moId) { }
}
```

#### ApplicationDAO.java
```java
public class ApplicationDAO {
    private static final String DATA_FILE = "data/applications.json";

    public void addApplication(Application app) { }
    public void updateApplication(Application app) { }
    public Application getApplication(String jobId, String userId) { }
    public List<Application> getApplicationsByJob(String jobId) { }
    public List<Application> getApplicationsByUser(String userId) { }
    public List<Application> getApprovedApplicationsByUser(String userId) { }
}
```

### 3.3 业务服务层（Service）

#### AuthService.java
```java
public class AuthService {
    public User register(String username, String password, String name, String email, String role) { }
    public User login(String username, String password) { }
    public void logout(HttpSession session) { }
    public boolean isLoggedIn(HttpSession session) { }
    public User getCurrentUser(HttpSession session) { }
}
```

#### UserService.java
```java
public class UserService {
    public void updateProfile(String userId, String name, String email, List<String> skills, String availableTime) { }
    public List<User> getAllTAs() { }
    public Map<String, Integer> getTAWorkload() { }
}
```

#### JobService.java
```java
public class JobService {
    public Job createJob(String title, String type, String description, String requirements, int positions, String deadline, String moId) { }
    public void updateJob(Job job) { }
    public void closeJob(String jobId) { }
    public List<Job> getOpenJobs() { }
    public List<Job> searchJobs(String keyword, String type) { }
}
```

#### ApplicationService.java
```java
public class ApplicationService {
    public Application applyForJob(String jobId, String userId) { }
    public void approveApplication(String applicationId, String moId) { }
    public void rejectApplication(String applicationId, String moId) { }
    public List<Application> getUserApplications(String userId) { }
    public List<Application> getJobApplications(String jobId) { }
}
```

### 3.4 Servlet控制层

#### AuthServlet.java
- `doGet`: 显示登录/注册页面
- `doPost`: 处理登录/注册请求

#### JobServlet.java
- `doGet`: 获取职位列表/详情
- `doPost`: 创建职位
- `doPut`: 更新职位
- `doDelete`: 删除职位

#### ApplicationServlet.java
- `doGet`: 获取申请列表
- `doPost`: 提交申请
- `doPut`: 审核申请

#### UserServlet.java
- `doGet`: 获取用户信息
- `doPut`: 更新用户资料

#### AdminServlet.java
- `doGet`: 管理员Dashboard
- `doPost`: 导出数据

---

## 4. 数据存储设计

### 4.1 JSON文件结构

#### users.json
```json
{
  "users": [
    {
      "userId": "u001",
      "username": "john",
      "password": "5f4dcc3b5aa765d61d8327deb882cf99",
      "name": "John Smith",
      "email": "john@bupt.edu.cn",
      "role": "TA",
      "skills": ["Java", "Python"],
      "availableTime": "Mon/Wed/Fri",
      "enabled": true,
      "createdAt": "2026-03-01T10:00:00"
    }
  ]
}
```

#### jobs.json
```json
{
  "jobs": [
    {
      "jobId": "j001",
      "title": "Software Engineering TA",
      "type": "MODULE",
      "description": "Help with SE course",
      "requirements": "Good programming skills",
      "positions": 2,
      "postedBy": "mo001",
      "deadline": "2026-04-01",
      "status": "OPEN",
      "createdAt": "2026-03-10T10:00:00"
    }
  ]
}
```

#### applications.json
```json
{
  "applications": [
    {
      "applicationId": "a001",
      "jobId": "j001",
      "userId": "u001",
      "status": "PENDING",
      "appliedAt": "2026-03-15T10:00:00",
      "reviewedBy": null,
      "reviewedAt": null
    }
  ]
}
```

### 4.2 文件系统
- 简历存储路径: `data/resumes/`
- 文件命名: `{userId}_{timestamp}.pdf`

---

## 5. 安全性设计

### 5.1 认证
- 密码使用MD5加密存储
- Session管理用户登录状态
- 登录超时验证

### 5.2 授权
- 基于角色的访问控制（RBAC）
- 页面访问权限检查
- Servlet方法级权限验证

### 5.3 输入验证
- 用户名格式验证
- 邮箱格式验证
- 必填字段验证
- 文件类型和大小验证

---

## 6. 部署配置

### 6.1 pom.xml关键依赖
```xml
<dependencies>
    <!-- Servlet API -->
    <dependency>
        <groupId>jakarta.servlet</groupId>
        <artifactId>jakarta.servlet-api</artifactId>
        <version>6.0.0</version>
        <scope>provided</scope>
    </dependency>

    <!-- JSP -->
    <dependency>
        <groupId>jakarta.servlet.jsp</groupId>
        <artifactId>jakarta.servlet.jsp-api</artifactId>
        <version>3.1.1</version>
        <scope>provided</scope>
    </dependency>

    <!-- JSON处理 -->
    <dependency>
        <groupId>org.json</groupId>
        <artifactId>json</artifactId>
        <version>20231013</version>
    </dependency>
</dependencies>
```

### 6.2 Tomcat配置
- 端口: 8080
- Context Path: /ta-recruitment
- Session超时: 30分钟

---

## 7. 开发计划

### 第一轮迭代（Week 1-2）
1. 项目环境搭建
2. 用户注册/登录功能
3. 基础数据模型和DAO

### 第二轮迭代（Week 3-4）
1. 职位发布和浏览
2. 职位申请功能
3. 申请审核功能

### 第三轮迭代（Week 5-6）
1. 简历上传
2. 工作量统计
3. 管理员功能

### 第四轮迭代（Week 7-8）
1. 测试和优化
2. 文档编写

---

**文档版本**: 1.0
**创建日期**: 2026年3月19日
**项目**: BUPT International School TA Recruitment System