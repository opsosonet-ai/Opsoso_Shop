@echo off
REM =================================
REM 🛑 OSS Application Stop Script
REM =================================

set APP_NAME=OSS Application

echo 🛑 Stopping %APP_NAME%...
echo =========================

REM Kill Java processes
echo 🧹 Stopping Java processes...
taskkill /F /IM java.exe /FI "WINDOWTITLE eq*demo-0.0.1-SNAPSHOT*" 2>nul
taskkill /F /IM java.exe /FI "COMMANDLINE eq*demo-0.0.1-SNAPSHOT.jar*" 2>nul

REM Clean up PID file
if exist ".app.pid" del ".app.pid"

timeout /t 2 >nul

echo ✅ Application stopped successfully!
pause