#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="/Users/fanzj/Desktop/Soft666-1"
JAVA_HOME_DEFAULT="/Library/Java/JavaVirtualMachines/jdk-24.jdk/Contents/Home"
MAVEN_BIN_DEFAULT="/tmp/apache-maven-3.9.6/bin/mvn"
TOMCAT_HOME_DEFAULT="/tmp/apache-tomcat-10.1.24"
TOMCAT_VERSION_DEFAULT="10.1.24"
APP_NAME="ta-recruitment"
APP_URL="http://localhost:8080/${APP_NAME}/"
WAR_PATH="$PROJECT_DIR/target/$APP_NAME.war"
BUILD_DIR="$PROJECT_DIR/target/$APP_NAME"
CLASSES_DIR="$PROJECT_DIR/target/classes"
JSON_JAR="$HOME/.m2/repository/org/json/json/20231013/json-20231013.jar"
SERVLET_JAR="$HOME/.m2/repository/jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar"

JAVA_HOME="${JAVA_HOME:-$JAVA_HOME_DEFAULT}"
MAVEN_BIN="${MAVEN_BIN:-$MAVEN_BIN_DEFAULT}"
TOMCAT_HOME="${TOMCAT_HOME:-$TOMCAT_HOME_DEFAULT}"
TOMCAT_VERSION="${TOMCAT_VERSION:-$TOMCAT_VERSION_DEFAULT}"

cd "$PROJECT_DIR"

load_local_env() {
  if [[ -f "$PROJECT_DIR/.env.local" ]]; then
    set -a
    # shellcheck disable=SC1091
    source "$PROJECT_DIR/.env.local"
    set +a
  fi
}

require_file() {
  local file_path="$1"
  local message="$2"
  if [[ ! -f "$file_path" ]]; then
    echo "ERROR: $message"
    echo "Missing: $file_path"
    exit 1
  fi
}

require_dir() {
  local dir_path="$1"
  local message="$2"
  if [[ ! -d "$dir_path" ]]; then
    echo "ERROR: $message"
    echo "Missing: $dir_path"
    exit 1
  fi
}

resolve_java_home() {
  if [[ -d "$JAVA_HOME" ]]; then
    return
  fi
  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    local detected_java_home
    detected_java_home="$(/usr/libexec/java_home 2>/dev/null || true)"
    if [[ -n "$detected_java_home" && -d "$detected_java_home" ]]; then
      JAVA_HOME="$detected_java_home"
    fi
  fi
}

resolve_maven_bin() {
  if [[ -f "$MAVEN_BIN" ]]; then
    return
  fi
  local detected_maven
  detected_maven="$(command -v mvn 2>/dev/null || true)"
  if [[ -n "$detected_maven" && -f "$detected_maven" ]]; then
    MAVEN_BIN="$detected_maven"
    return
  fi
  local candidate
  for candidate in \
    "$MAVEN_BIN_DEFAULT" \
    /opt/homebrew/bin/mvn \
    /usr/local/bin/mvn \
    /opt/homebrew/Cellar/maven/*/bin/mvn \
    /usr/local/Cellar/maven/*/bin/mvn \
    /tmp/apache-maven-*/bin/mvn; do
    if [[ -f "$candidate" ]]; then
      MAVEN_BIN="$candidate"
      return
    fi
  done
  MAVEN_BIN=""
}

resolve_tomcat_home() {
  if [[ -f "$TOMCAT_HOME/bin/startup.sh" && -f "$TOMCAT_HOME/bin/shutdown.sh" ]]; then
    return
  fi
  local candidate
  for candidate in \
    "$TOMCAT_HOME_DEFAULT" \
    /opt/homebrew/opt/tomcat/libexec \
    /usr/local/opt/tomcat/libexec \
    /opt/homebrew/Cellar/tomcat/*/libexec \
    /usr/local/Cellar/tomcat/*/libexec \
    /tmp/apache-tomcat-*; do
    if [[ -f "$candidate/bin/startup.sh" && -f "$candidate/bin/shutdown.sh" ]]; then
      TOMCAT_HOME="$candidate"
      return
    fi
  done
  TOMCAT_HOME=""
}

install_tomcat_if_missing() {
  if [[ -n "$TOMCAT_HOME" ]]; then
    return
  fi
  local archive="/tmp/apache-tomcat-$TOMCAT_VERSION.tar.gz"
  local url="https://archive.apache.org/dist/tomcat/tomcat-10/v$TOMCAT_VERSION/bin/apache-tomcat-$TOMCAT_VERSION.tar.gz"

  echo "Tomcat was not found. Downloading Tomcat $TOMCAT_VERSION..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$url" -o "$archive"
  else
    echo "ERROR: curl is not available, so Tomcat cannot be downloaded automatically."
    exit 1
  fi

  echo "Extracting Tomcat..."
  tar -xzf "$archive" -C /tmp
  TOMCAT_HOME="/tmp/apache-tomcat-$TOMCAT_VERSION"
}

check_runtime() {
  resolve_java_home
  resolve_maven_bin
  resolve_tomcat_home
  install_tomcat_if_missing

  require_dir "$JAVA_HOME" "JAVA_HOME is not available. Update JAVA_HOME in this script or export it before running."
  if [[ -z "$MAVEN_BIN" && ! -f "$WAR_PATH" ]]; then
    echo "ERROR: Maven is not available and no existing WAR was found."
    echo "Install Maven, set MAVEN_BIN, or put a built WAR at: $WAR_PATH"
    exit 1
  fi
  if [[ -z "$TOMCAT_HOME" ]]; then
    echo "ERROR: Tomcat is not available."
    echo "Install Tomcat, set TOMCAT_HOME, or put it back at: $TOMCAT_HOME_DEFAULT"
    exit 1
  fi
  require_file "$TOMCAT_HOME/bin/startup.sh" "Tomcat startup script is not available. Update TOMCAT_HOME in this script or export it before running."
  require_file "$TOMCAT_HOME/bin/shutdown.sh" "Tomcat shutdown script is not available. Update TOMCAT_HOME in this script or export it before running."
}

stop_tomcat() {
  echo "Stopping Tomcat..."
  "$TOMCAT_HOME/bin/shutdown.sh" || true
  sleep 2
}

start_tomcat() {
  echo "Starting Tomcat..."
  export JAVA_HOME
  export CATALINA_OPTS="-Dtarec.data.dir=$PROJECT_DIR/data ${CATALINA_OPTS:-}"
  "$TOMCAT_HOME/bin/startup.sh"
  echo
  echo "TA Recruitment System is starting:"
  echo "$APP_URL"
}

run_tomcat_foreground() {
  echo "Starting Tomcat in foreground..."
  export JAVA_HOME
  export CATALINA_OPTS="-Dtarec.data.dir=$PROJECT_DIR/data ${CATALINA_OPTS:-}"
  echo
  echo "TA Recruitment System:"
  echo "$APP_URL"
  echo
  if command -v open >/dev/null 2>&1; then
    (sleep 4; open "$APP_URL" >/dev/null 2>&1 || true) &
  fi
  exec "$TOMCAT_HOME/bin/catalina.sh" run
}

build_and_deploy() {
  if [[ -n "$MAVEN_BIN" ]]; then
    echo "Building WAR with Maven: $MAVEN_BIN"
    "$MAVEN_BIN" clean package -DskipTests
  elif [[ -f "$JSON_JAR" && -f "$SERVLET_JAR" ]]; then
    echo "Maven was not found. Building WAR with javac and jar..."
    rm -rf "$BUILD_DIR" "$CLASSES_DIR"
    mkdir -p "$BUILD_DIR" "$CLASSES_DIR" "$BUILD_DIR/WEB-INF/classes" "$BUILD_DIR/WEB-INF/lib"
    cp -R "$PROJECT_DIR/src/main/webapp/." "$BUILD_DIR/"
    cp "$JSON_JAR" "$BUILD_DIR/WEB-INF/lib/"
    find "$PROJECT_DIR/src/main/java" -name "*.java" > "$PROJECT_DIR/target/java-sources.list"
    javac -encoding UTF-8 -cp "$JSON_JAR:$SERVLET_JAR" -d "$CLASSES_DIR" @"$PROJECT_DIR/target/java-sources.list"
    cp -R "$CLASSES_DIR/." "$BUILD_DIR/WEB-INF/classes/"
    (cd "$BUILD_DIR" && jar -cf "$WAR_PATH" .)
  else
    echo "Maven was not found and local compile dependencies are missing. Using existing WAR:"
    echo "$WAR_PATH"
  fi

  require_file "$WAR_PATH" "WAR file is not available after build."

  echo "Deploying WAR to Tomcat..."
  mkdir -p "$TOMCAT_HOME/webapps"
  rm -rf "$TOMCAT_HOME/webapps/$APP_NAME" "$TOMCAT_HOME/webapps/$APP_NAME.war"
  cp "$WAR_PATH" "$TOMCAT_HOME/webapps/"
}

open_browser() {
  if command -v open >/dev/null 2>&1; then
    sleep 4
    open "$APP_URL" >/dev/null 2>&1 || true
  fi
}

usage() {
  echo "Usage: $0 {run|start|stop|restart|build}"
  echo
  echo "run/restart: load .env.local, build WAR, deploy, restart Tomcat"
  echo "start:       start Tomcat without rebuilding"
  echo "foreground:  start Tomcat in the current terminal"
  echo "stop:        stop Tomcat"
  echo "build:       build and deploy WAR without starting Tomcat"
}

main() {
  local action="${1:-run}"
  load_local_env
  check_runtime

  case "$action" in
    run|restart)
      build_and_deploy
      stop_tomcat
      start_tomcat
      open_browser
      ;;
    start)
      start_tomcat
      open_browser
      ;;
    foreground)
      run_tomcat_foreground
      ;;
    stop)
      stop_tomcat
      ;;
    build)
      build_and_deploy
      ;;
    *)
      usage
      exit 1
      ;;
  esac
}

main "$@"
