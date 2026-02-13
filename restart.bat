@echo off
REM =================================
REM 🔄 OSS Application Restart Script
REM =================================

set APP_NAME=OSS Application
set JAR_FILE=target\demo-0.0.1-SNAPSHOT.jar
set PID_FILE=.app.pid
set LOG_FILE=app.log

echo 🔄 %APP_NAME% Restart Script
echo =================================

echo 🛑 Stopping application...

REM Kill existing Java processes
taskkill /F /IM java.exe /FI "WINDOWTITLE eq*demo-0.0.1-SNAPSHOT*" 2>nul
timeout /t 3 >nul

REM Clean up PID file
if exist "%PID_FILE%" del "%PID_FILE%"

echo ✅ Application stopped

REM Check if rebuild is requested
if "%1"=="--rebuild" (
    echo 🔨 Rebuilding application...
    mvnw.cmd clean package -DskipTests
    if errorlevel 1 (
        echo ❌ Build failed!
        pause
        exit /b 1
    )
) else (
    if not exist "%JAR_FILE%" (
        echo 🔨 Building application...
        mvnw.cmd clean package -DskipTests
        if errorlevel 1 (
            echo ❌ Build failed!
            pause
            exit /b 1
        )
    )
)

echo ✅ Build completed

echo 🚀 Starting application...
echo 📁 Current directory: %CD%
echo ☕ Java version:
java -version
echo.

REM Start application
echo 🌐 Starting web server...
echo 📍 URLs:
echo   Main: http://localhost:8080
echo   Return: http://localhost:8080/doi-tra
echo   Customers: http://localhost:8080/khach-hang
echo   Settings: http://localhost:8080/cai-dat
echo   Emergency: http://localhost:8080/emergency/setup
echo.
echo Press Ctrl+C to stop
echo =================================
echo.

java -jar "%JAR_FILE%"