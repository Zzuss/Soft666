# 助教招聘系统（TA Recruitment System）

这份 README 按“新手可直接照抄命令”的方式写。

## 0. 先看这里：最短启动流程（你当前这台机器）

你要进入的项目文件夹是：

```bash
cd /Users/fanzj/Desktop/Soft666-1
```

然后按下面命令执行（假设你已经安装了 Tomcat 10.1+）：

```bash
# 1) 进入项目目录（必须）
cd /Users/fanzj/Desktop/Soft666-1

# 2) 设置你的 Tomcat 路径（把下面路径改成你自己的）
export TOMCAT_HOME="/你的路径/apache-tomcat-10.1.xx"

# 3) 确认 WAR 包存在（你当前项目里通常已存在）
ls target/ta-recruitment.war

# 4) 复制 WAR 到 Tomcat
cp target/ta-recruitment.war "$TOMCAT_HOME/webapps/"

# 5) （推荐）固定数据目录到项目 data/
export CATALINA_OPTS="-Dtarec.data.dir=/Users/fanzj/Desktop/Soft666-1/data"

# 6) 启动 Tomcat
"$TOMCAT_HOME/bin/startup.sh"
```

启动后访问：

- 登录页：`http://localhost:8080/ta-recruitment/auth`

停止服务：

```bash
"$TOMCAT_HOME/bin/shutdown.sh"
```

也可以直接双击项目根目录下的脚本：

- `run.command`：重新编译、部署并启动项目
- `stop.command`：停止 Tomcat
- `run.bat`：Windows 上重新编译、部署并启动项目

脚本内部默认使用：

- JDK：`/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home`
- Maven：`/tmp/apache-maven-3.9.6/bin/mvn`
- Tomcat：`/tmp/apache-tomcat-10.1.24`

如果你的本机路径不同，修改 `scripts/ta-recruitment.sh` 顶部的默认路径即可。
Windows 上可设置 `JAVA_HOME`、`MAVEN_BIN`、`TOMCAT_HOME`，或修改 `scripts/ta-recruitment.bat` 顶部的默认路径。

***

## 1. 如果第 3 步找不到 WAR（需要先构建）

先安装 Maven（`mvn` 命令可用），再执行：

```bash
cd /Users/fanzj/Desktop/Soft666-1
mvn clean package
```

成功后会生成：

- `target/ta-recruitment.war`

然后回到“最短启动流程”的第 4 步继续。

## 2. 你到底要装哪些依赖

### 2.1 必装

- JDK：**17**（推荐）
- Maven：**3.8+**（用于构建）
- Tomcat：**10.1+**（必须，Tomcat 9 不兼容）

### 2.2 Maven 依赖（项目内部）

- `jakarta.servlet:jakarta.servlet-api:6.0.0`（`provided`）
- `jakarta.servlet.jsp:jakarta.servlet.jsp-api:3.1.1`（`provided`）
- `jakarta.el:jakarta.el-api:5.0.1`（`provided`）
- `org.json:json:20231013`

## 3. 访问地址（启动后）

- 登录页：`http://localhost:8080/ta-recruitment/auth`
- 首页：`http://localhost:8080/ta-recruitment/`
- 注册页：`http://localhost:8080/ta-recruitment/auth?action=register`

## 4. 数据文件位置

默认数据文件在：

- `data/users.json`
- `data/jobs.json`
- `data/applications.json`

数据目录优先级：

1. `-Dtarec.data.dir=...`
2. 项目根目录下 `data/`
3. 当前工作目录下 `data/`（不存在会自动创建）

## 4.1 大模型匹配配置（可选）

系统默认仍可使用本地规则匹配。若要启用大模型增强匹配，可在项目根目录创建 `.env.local`：

```bash
TAREC_LLM_API_KEY="你的 DeepSeek API Key"
TAREC_LLM_MODEL="deepseek-v4-flash"
TAREC_LLM_ENDPOINT="https://api.deepseek.com/chat/completions"
```

也可以用环境变量或 JVM 参数配置。优先级为：环境变量 > JVM 参数 > `.env.local` > 系统默认值。`.env.local` 已加入 `.gitignore`，不要提交真实 API Key。

启用后，TA 申请岗位和查看岗位详情时，系统会基于岗位描述、要求、TA 技能、个人陈述和可用时间生成更细的匹配度与简短 AI 匹配分析。未配置 API Key 或调用失败时，会自动回退到原有技能规则匹配。

## 5. 常见报错（按现象处理）

### 5.1 `mvn: command not found`

没装 Maven 或未加入 `PATH`。先安装 Maven，再开新终端重试。

### 5.2 打开网址 404

按顺序检查：

1. `target/ta-recruitment.war` 是否存在
2. 是否已复制到 `$TOMCAT_HOME/webapps/`
3. 网址是否包含 `/ta-recruitment`
4. Tomcat 是否真的启动成功（看日志）

### 5.3 Servlet 相关类找不到

大概率 Tomcat 版本过低。请用 **Tomcat 10.1+**。

## 6. 项目结构（开发时）

- Java 代码：`src/main/java`
- 页面与样式：`src/main/webapp`
- 国际化工具：`com.tarecruitment.util.I18nUtil`
- 语言切换组件：`src/main/webapp/jsp/common/language-switcher.jsp`

## 7. Fan

export JAVA\_HOME=/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home

/tmp/apache-tomcat-10.1.24/bin/startup.sh

/tmp/apache-tomcat-10.1.24/bin/shutdown.sh

重新编译和部署：

```shellscript
cd /Users/fanzj/Desktop/Soft666-1
```

```shellscript
/tmp/apache-maven-3.9.6/bin/mvn clean package -DskipTests

cp target/ta-recruitment.war /tmp/apache-tomcat-10.1.24/webapps/
```

```shellscript
/tmp/apache-tomcat-10.1.24/bin/shutdown.sh
/tmp/apache-tomcat-10.1.24/bin/startup.sh
```

<http://localhost:8080/ta-recruitment/>
