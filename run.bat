@echo off
REM =================================
REM 🚀 OSS Application Launcher
REM =================================

echo 🚀 Starting OSS Application...
echo 📁 Current directory: %CD%
echo ☕ Java version:
java -version
echo.

REM Check if JAR exists
set JAR_FILE=target\demo-0.0.1-SNAPSHOT.jar
if not exist "%JAR_FILE%" (
    echo ❌ JAR file not found: %JAR_FILE%
    echo 🔨 Building application...
    mvnw.cmd clean package -DskipTests
    if errorlevel 1 (
        echo ❌ Build failed!
        pause
        exit /b 1
    )
)

echo ✅ JAR file ready
echo.
echo 🌐 Starting web server...
echo 📍 URL: http://localhost:8080
echo 🆘 Emergency Setup: http://localhost:8080/emergency/setup
echo.
echo Press Ctrl+C to stop the application
echo =========================================
echo.

REM Run the application
java -jar "%JAR_FILE%"

pause