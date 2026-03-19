# 助教招聘系统（TA Recruitment System）

基于 Jakarta Servlet/JSP 的助教招聘系统课程项目。

## 1. 项目简介

- 技术栈：Java + Jakarta Servlet/JSP + Maven + Tomcat
- 打包方式：`WAR`（见 `pom.xml`）
- 数据存储：本地 JSON 文件（`data/` 目录），无需外部数据库
- 主要功能：
  - 用户注册 / 登录 / 退出（TA / MO 角色）
  - 职位发布、浏览与详情查看
  - 申请提交与审核
  - Dashboard 统计信息展示
  - 中英文切换

## 2. 完整依赖

### 2.1 运行与构建环境（需安装）

- JDK：**17**（推荐，项目编译目标为 17）
- Maven：**3.8+**
- Tomcat：**10.1+**（必须支持 Jakarta Servlet 6）
- 操作系统：macOS / Linux / Windows 均可

> 注意：本项目使用 `jakarta.*` 包，Tomcat 9（`javax.*`）不兼容。

### 2.2 Maven 依赖（来自 `pom.xml`）

- `jakarta.servlet:jakarta.servlet-api:6.0.0`（`provided`）
- `jakarta.servlet.jsp:jakarta.servlet.jsp-api:3.1.1`（`provided`）
- `jakarta.el:jakarta.el-api:5.0.1`（`provided`）
- `org.json:json:20231013`

### 2.3 构建插件

- `maven-compiler-plugin:3.11.0`
- `maven-war-plugin:3.4.0`

## 3. 环境检查

在终端执行：

```bash
java -version
mvn -version
```

期望结果：

- `java` 为 JDK 17
- `mvn` 命令可用

## 4. 构建项目

在项目根目录执行：

```bash
mvn clean package
```

构建产物：

- `target/ta-recruitment.war`

## 5. 启动项目（Tomcat 部署）

### 5.1 部署 WAR 包

```bash
cp target/ta-recruitment.war <TOMCAT_HOME>/webapps/
```

### 5.2（推荐）指定数据目录

本项目会读写 JSON 数据文件。建议固定到项目 `data` 目录：

macOS / Linux：

```bash
export CATALINA_OPTS="-Dtarec.data.dir=/绝对路径/Soft666-1/data"
```

Windows PowerShell：

```powershell
$env:CATALINA_OPTS="-Dtarec.data.dir=D:\绝对路径\Soft666-1\data"
```

### 5.3 启动 Tomcat

macOS / Linux：

```bash
<TOMCAT_HOME>/bin/startup.sh
```

Windows：

```bat
<TOMCAT_HOME>\bin\startup.bat
```

## 6. 访问地址

启动后访问：

- 登录页：`http://localhost:8080/ta-recruitment/auth`
- 应用首页：`http://localhost:8080/ta-recruitment/`
- 注册页：`http://localhost:8080/ta-recruitment/auth?action=register`

## 7. 数据文件说明

默认数据文件：

- `data/users.json`
- `data/jobs.json`
- `data/applications.json`

数据目录解析优先级：

1. JVM 参数 `-Dtarec.data.dir=...`
2. `<project_root>/data`
3. `<working_dir>/data`（不存在会自动创建）

如果登录账号不符合预期，可以：

- 通过 `/auth?action=register` 新注册测试账号
- 或直接修改 `data/users.json`（仅开发/测试场景）

## 8. 开发目录结构

- Java 源码：`src/main/java`
- JSP/CSS：`src/main/webapp`
- i18n 工具类：`com.tarecruitment.util.I18nUtil`
- 语言切换组件：`src/main/webapp/jsp/common/language-switcher.jsp`

## 9. 常见问题

### 9.1 `mvn: command not found`

未安装 Maven 或未加入 `PATH`。安装后重新打开终端再试。

### 9.2 部署后 404

- 确认 `<TOMCAT_HOME>/webapps/` 下已存在 `ta-recruitment.war`
- 确认 URL 包含上下文路径 `/ta-recruitment`
- 查看 Tomcat 日志（如 `catalina.out`）定位部署报错

### 9.3 `ClassNotFound` 或 Servlet API 相关错误

- 确认 Tomcat 版本为 **10.1+**
- 不要使用 Tomcat 9

### 9.4 数据没有写到预期位置

显式设置：

```bash
-Dtarec.data.dir=/绝对路径/Soft666-1/data
```

## 10. 安全说明（课程项目 / 开发环境）

当前密码哈希使用 MD5，仅适用于课程演示与本地开发，不建议用于生产环境。  
若用于生产，请替换为更安全的密码哈希算法（如 bcrypt / Argon2）。

