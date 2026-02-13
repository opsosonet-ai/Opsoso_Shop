#!/bin/bash

# =================================
# 📊 OSS Application Status Script
# =================================

APP_NAME="OSS Application"
JAR_FILE="target/demo-0.0.1-SNAPSHOT.jar"
PID_FILE=".app.pid"
LOG_FILE="app.log"

echo "📊 $APP_NAME Status"
echo "========================="

# Check if application is running
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        echo "✅ Status: RUNNING"
        echo "🆔 Process ID: $PID"
        
        # Get process info
        MEMORY=$(ps -p $PID -o rss= | awk '{print $1/1024 " MB"}' 2>/dev/null)
        CPU=$(ps -p $PID -o %cpu= 2>/dev/null)
        UPTIME=$(ps -p $PID -o etime= 2>/dev/null | tr -d ' ')
        
        echo "💾 Memory: ${MEMORY:-N/A}"
        echo "⚡ CPU: ${CPU:-N/A}%"
        echo "⏱️  Uptime: ${UPTIME:-N/A}"
    else
        echo "❌ Status: STOPPED (PID file exists but process not found)"
        rm -f "$PID_FILE"
    fi
else
    echo "❌ Status: STOPPED"
fi

# Check port status
PORT_CHECK=$(netstat -tlnp 2>/dev/null | grep :8080 || ss -tlnp 2>/dev/null | grep :8080)
if [ -n "$PORT_CHECK" ]; then
    echo "🌐 Port 8080: ACTIVE"
    echo "   $PORT_CHECK"
else
    echo "🌐 Port 8080: FREE"
fi

# Check JAR file
if [ -f "$JAR_FILE" ]; then
    JAR_SIZE=$(ls -lh "$JAR_FILE" | awk '{print $5}')
    JAR_DATE=$(ls -l "$JAR_FILE" | awk '{print $6, $7, $8}')
    echo "📦 JAR File: EXISTS (${JAR_SIZE}, ${JAR_DATE})"
else
    echo "📦 JAR File: NOT FOUND"
fi

# Check log file
if [ -f "$LOG_FILE" ]; then
    LOG_SIZE=$(ls -lh "$LOG_FILE" | awk '{print $5}')
    LOG_LINES=$(wc -l < "$LOG_FILE")
    echo "📝 Log File: EXISTS (${LOG_SIZE}, ${LOG_LINES} lines)"
    
    # Show last few lines if not empty
    if [ -s "$LOG_FILE" ]; then
        echo ""
        echo "📋 Last 5 log entries:"
        echo "----------------------"
        tail -5 "$LOG_FILE" | sed 's/^/   /'
    fi
else
    echo "📝 Log File: NOT FOUND"
fi

echo ""
echo "🛠️  Available commands:"
echo "   ./restart.sh     - Restart application"
echo "   ./stop.sh        - Stop application"
echo "   ./run.sh         - Run in foreground"
echo "   tail -f app.log  - Follow logs"

# Test web connectivity if running
if lsof -i:8080 >/dev/null 2>&1; then
    echo ""
    echo "🌐 Testing web connectivity..."
    if curl -s --max-time 5 http://localhost:8080 >/dev/null 2>&1; then
        echo "✅ Web server: RESPONDING"
        echo "📍 Access: http://localhost:8080"
    else
        echo "⚠️  Web server: NOT RESPONDING (may still be starting)"
    fi
fi