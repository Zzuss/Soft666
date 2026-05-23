@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "PROJECT_DIR=%~dp0.."
for %%I in ("%PROJECT_DIR%") do set "PROJECT_DIR=%%~fI"

set "JAVA_HOME_DEFAULT=C:\Program Files\Java\jdk-24"
set "MAVEN_BIN_DEFAULT=%TEMP%\apache-maven-3.9.6\bin\mvn.cmd"
set "TOMCAT_HOME_DEFAULT=%TEMP%\apache-tomcat-10.1.24"
set "TOMCAT_VERSION=10.1.24"
set "APP_NAME=ta-recruitment"
set "APP_URL=http://localhost:8080/%APP_NAME%/"
set "WAR_PATH=%PROJECT_DIR%\target\%APP_NAME%.war"

cd /d "%PROJECT_DIR%"

call :load_local_env
call :check_runtime
if errorlevel 1 exit /b 1

set "ACTION=%~1"
if "%ACTION%"=="" set "ACTION=run"

if /I "%ACTION%"=="run" goto run
if /I "%ACTION%"=="restart" goto run
if /I "%ACTION%"=="start" goto start
if /I "%ACTION%"=="foreground" goto foreground
if /I "%ACTION%"=="stop" goto stop
if /I "%ACTION%"=="build" goto build
goto usage

:run
call :build_and_deploy
if errorlevel 1 exit /b 1
call :stop_tomcat
call :start_tomcat
call :open_browser
exit /b 0

:start
call :start_tomcat
if errorlevel 1 exit /b 1
call :open_browser
exit /b 0

:foreground
call :run_tomcat_foreground
exit /b %ERRORLEVEL%

:stop
call :stop_tomcat
exit /b 0

:build
call :build_and_deploy
exit /b %ERRORLEVEL%

:usage
echo Usage: %~nx0 {run^|start^|stop^|restart^|build^|foreground}
echo.
echo run/restart: load .env.local, build WAR, deploy, restart Tomcat
echo start:       start Tomcat without rebuilding
echo foreground:  start Tomcat in the current terminal
echo stop:        stop Tomcat
echo build:       build and deploy WAR without starting Tomcat
exit /b 1

:load_local_env
if not exist "%PROJECT_DIR%\.env.local" exit /b 0
for /f "usebackq tokens=1,* delims==" %%A in ("%PROJECT_DIR%\.env.local") do (
    set "ENV_KEY=%%A"
    set "ENV_VALUE=%%B"
    if not "!ENV_KEY!"=="" if not "!ENV_KEY:~0,1!"=="#" (
        set "!ENV_KEY!=!ENV_VALUE:"=!"
    )
)
exit /b 0

:check_runtime
call :resolve_java_home
call :resolve_maven_bin
call :resolve_tomcat_home
call :install_tomcat_if_missing
if errorlevel 1 exit /b 1

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo ERROR: JAVA_HOME is not available. Install JDK 17+ or set JAVA_HOME.
    echo Missing: %JAVA_HOME%\bin\java.exe
    exit /b 1
)

if not defined MAVEN_BIN if not exist "%WAR_PATH%" (
    echo ERROR: Maven is not available and no existing WAR was found.
    echo Install Maven, set MAVEN_BIN, or put a built WAR at: %WAR_PATH%
    exit /b 1
)

if not defined TOMCAT_HOME (
    echo ERROR: Tomcat is not available. Install Tomcat 10.1+ or set TOMCAT_HOME.
    exit /b 1
)

if not exist "%TOMCAT_HOME%\bin\catalina.bat" (
    echo ERROR: Tomcat startup script is not available.
    echo Missing: %TOMCAT_HOME%\bin\catalina.bat
    exit /b 1
)

if not exist "%TOMCAT_HOME%\bin\shutdown.bat" (
    echo ERROR: Tomcat shutdown script is not available.
    echo Missing: %TOMCAT_HOME%\bin\shutdown.bat
    exit /b 1
)

exit /b 0

:resolve_java_home
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" exit /b 0

for %%D in (
    "%JAVA_HOME_DEFAULT%"
    "%ProgramFiles%\Java\jdk-24"
    "%ProgramFiles%\Java\jdk-23"
    "%ProgramFiles%\Java\jdk-22"
    "%ProgramFiles%\Java\jdk-21"
    "%ProgramFiles%\Java\jdk-17"
    "%ProgramFiles%\Eclipse Adoptium\jdk-24*"
    "%ProgramFiles%\Eclipse Adoptium\jdk-21*"
    "%ProgramFiles%\Eclipse Adoptium\jdk-17*"
) do (
    if exist "%%~D\bin\java.exe" (
        set "JAVA_HOME=%%~D"
        exit /b 0
    )
)

if not defined JAVA_HOME set "JAVA_HOME=%JAVA_HOME_DEFAULT%"
exit /b 0

:resolve_maven_bin
if defined MAVEN_BIN if exist "%MAVEN_BIN%" exit /b 0

for /f "delims=" %%I in ('where mvn.cmd 2^>NUL') do (
    set "MAVEN_BIN=%%I"
    exit /b 0
)

for /f "delims=" %%I in ('where mvn 2^>NUL') do (
    set "MAVEN_BIN=%%I"
    exit /b 0
)

for %%M in (
    "%MAVEN_BIN_DEFAULT%"
    "%PROJECT_DIR%\apache-maven-3.9.6\bin\mvn.cmd"
    "%USERPROFILE%\apache-maven-3.9.6\bin\mvn.cmd"
    "C:\apache-maven-3.9.6\bin\mvn.cmd"
) do (
    if exist "%%~M" (
        set "MAVEN_BIN=%%~M"
        exit /b 0
    )
)

set "MAVEN_BIN="
exit /b 0

:resolve_tomcat_home
if defined TOMCAT_HOME if exist "%TOMCAT_HOME%\bin\catalina.bat" exit /b 0

for %%T in (
    "%TOMCAT_HOME_DEFAULT%"
    "%PROJECT_DIR%\apache-tomcat-%TOMCAT_VERSION%"
    "%USERPROFILE%\apache-tomcat-%TOMCAT_VERSION%"
    "C:\apache-tomcat-%TOMCAT_VERSION%"
) do (
    if exist "%%~T\bin\catalina.bat" (
        set "TOMCAT_HOME=%%~T"
        exit /b 0
    )
)

set "TOMCAT_HOME="
exit /b 0

:install_tomcat_if_missing
if defined TOMCAT_HOME exit /b 0

set "TOMCAT_ARCHIVE=%TEMP%\apache-tomcat-%TOMCAT_VERSION%-windows-x64.zip"
set "TOMCAT_URL=https://archive.apache.org/dist/tomcat/tomcat-10/v%TOMCAT_VERSION%/bin/apache-tomcat-%TOMCAT_VERSION%-windows-x64.zip"

echo Tomcat was not found. Downloading Tomcat %TOMCAT_VERSION%...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%TOMCAT_URL%' -OutFile '%TOMCAT_ARCHIVE%'"
if errorlevel 1 (
    echo ERROR: Tomcat could not be downloaded automatically.
    exit /b 1
)

echo Extracting Tomcat...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%TOMCAT_ARCHIVE%' -DestinationPath '%TEMP%' -Force"
if errorlevel 1 (
    echo ERROR: Tomcat could not be extracted automatically.
    exit /b 1
)

set "TOMCAT_HOME=%TOMCAT_HOME_DEFAULT%"
exit /b 0

:stop_tomcat
echo Stopping Tomcat...
call "%TOMCAT_HOME%\bin\shutdown.bat"
exit /b 0

:start_tomcat
echo Starting Tomcat...
set "CATALINA_OPTS=-Dtarec.data.dir=%PROJECT_DIR%\data %CATALINA_OPTS%"
call "%TOMCAT_HOME%\bin\startup.bat"
echo.
echo TA Recruitment System is starting:
echo %APP_URL%
exit /b %ERRORLEVEL%

:run_tomcat_foreground
echo Starting Tomcat in foreground...
set "CATALINA_OPTS=-Dtarec.data.dir=%PROJECT_DIR%\data %CATALINA_OPTS%"
echo.
echo TA Recruitment System:
echo %APP_URL%
echo.
start "" "%APP_URL%"
call "%TOMCAT_HOME%\bin\catalina.bat" run
exit /b %ERRORLEVEL%

:build_and_deploy
if defined MAVEN_BIN (
    echo Building WAR with Maven: %MAVEN_BIN%
    call "%MAVEN_BIN%" clean package -DskipTests
    if errorlevel 1 exit /b 1
) else (
    echo Maven was not found. Using existing WAR:
    echo %WAR_PATH%
)

if not exist "%WAR_PATH%" (
    echo ERROR: WAR file is not available after build.
    echo Missing: %WAR_PATH%
    exit /b 1
)

echo Deploying WAR to Tomcat...
if not exist "%TOMCAT_HOME%\webapps" mkdir "%TOMCAT_HOME%\webapps"
if exist "%TOMCAT_HOME%\webapps\%APP_NAME%" rmdir /s /q "%TOMCAT_HOME%\webapps\%APP_NAME%"
if exist "%TOMCAT_HOME%\webapps\%APP_NAME%.war" del /q "%TOMCAT_HOME%\webapps\%APP_NAME%.war"
copy /Y "%WAR_PATH%" "%TOMCAT_HOME%\webapps\" >NUL
if errorlevel 1 exit /b 1
exit /b 0

:open_browser
timeout /t 4 /nobreak >NUL
start "" "%APP_URL%"
exit /b 0
