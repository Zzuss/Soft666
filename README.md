# TA Recruitment System

A Java web application for managing Teaching Assistant recruitment. The system supports student TA applications, module organiser job posting, profile and resume management, application review, notifications, and optional AI-assisted job matching.

## Features

- User registration and login for TA, MO, and Admin roles
- TA profile editing, availability management, skills, and resume upload
- MO job creation, editing, closing, and application management
- Job search and filtering by keyword, type, and required skills
- Rule-based TA/job matching with optional LLM-enhanced analysis
- Application submission, status review, and notification display
- English/Chinese language switching

## Tech Stack

- Java 17
- Maven
- Jakarta Servlet 6 / JSP 3
- Tomcat 10.1+
- JSON file storage under `data/`
- JUnit 5 for test programs

## Project Structure

```text
src/main/java/com/tarecruitment/
  dao/       JSON data access classes
  model/     User, Job, Application, Notification models
  service/   Business logic and matching services
  servlet/   Web controllers
  util/      Password, JSON, and i18n helpers

src/main/webapp/
  css/       Application styles
  images/    Static images
  jsp/       JSP pages
  WEB-INF/   Web configuration

src/test/java/com/tarecruitment/
  model/     Model tests
  service/   Service tests
  util/      Utility tests

data/        Local JSON data files
scripts/     macOS/Linux and Windows helper scripts
```

## Requirements

- JDK 17 or newer
- Maven 3.8+
- Apache Tomcat 10.1+

Tomcat 9 is not compatible because this project uses Jakarta Servlet APIs.

## Build

```bash
mvn clean package
```

The WAR file will be generated at:

```text
target/ta-recruitment.war
```

## Run

Deploy the WAR file to Tomcat:

```bash
cp target/ta-recruitment.war "$TOMCAT_HOME/webapps/"
"$TOMCAT_HOME/bin/startup.sh"
```

Then open:

```text
http://localhost:8080/ta-recruitment/
```

To stop Tomcat:

```bash
"$TOMCAT_HOME/bin/shutdown.sh"
```

The project also includes helper scripts:

- `run.command` for macOS
- `run.bat` for Windows
- `stop.command` for macOS
- `scripts/ta-recruitment.sh`
- `scripts/ta-recruitment.bat`

Adjust the JDK, Maven, and Tomcat paths inside the scripts if your local installation paths are different.

## Tests

Run the test programs with:

```bash
mvn test
```

Current tests cover:

- Password hashing and verification
- Legacy password hash compatibility
- Job and TA skill matching
- Job creation and schedule validation
- User role helpers and job display labels

## Data Storage

The application stores local data in JSON files:

```text
data/users.json
data/jobs.json
data/applications.json
data/notifications.json
```

You can override the data directory with:

```bash
-Dtarec.data.dir=/path/to/data
```

## Optional AI Matching

The system works without an AI API key. If configured, AI matching enhances the normal rule-based score.

Create a local `.env.local` file or set environment variables:

```bash
TAREC_LLM_API_KEY="your-api-key"
TAREC_LLM_MODEL="deepseek-v4-flash"
TAREC_LLM_ENDPOINT="https://api.deepseek.com/chat/completions"
```

Do not commit real API keys.
