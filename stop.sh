#!/bin/bash

# =================================
# 🛑 OSS Application Stop Script
# =================================

APP_NAME="OSS Application"
PID_FILE=".app.pid"

echo "🛑 Stopping $APP_NAME..."
echo "========================="

# Stop by PID file
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p $PID > /dev/null 2>&1; then
        echo "📋 Found running process: $PID"
        kill $PID
        sleep 3
        
        # Force kill if needed
        if ps -p $PID > /dev/null 2>&1; then
            echo "⚡ Force stopping..."
            kill -9 $PID
        fi
    fi
    rm -f "$PID_FILE"
fi

# Clean up any remaining processes
echo "🧹 Cleaning up remaining processes..."
pkill -f "demo-0.0.1-SNAPSHOT.jar" 2>/dev/null || true
pkill -f "spring-boot" 2>/dev/null || true

# Wait for port to be free
echo "⏳ Waiting for port 8080 to be free..."
while lsof -i:8080 >/dev/null 2>&1; do
    sleep 1
    echo -n "."
done
echo ""

echo "✅ Application stopped successfully!"