#!/bin/bash

# =================================
# 🔄 OSS Application Restart Script
# =================================

APP_NAME="OSS Application"
JAR_FILE="target/demo-0.0.1-SNAPSHOT.jar"
PID_FILE=".app.pid"
LOG_FILE="app.log"

echo "🔄 $APP_NAME Restart Script"
echo "================================="

# Function to stop application
stop_app() {
    echo "🛑 Stopping application..."
    
    # Try to kill by PID file first
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p $PID > /dev/null 2>&1; then
            echo "📋 Found PID: $PID"
            kill $PID
            sleep 3
            
            # Force kill if still running
            if ps -p $PID > /dev/null 2>&1; then
                echo "⚡ Force stopping..."
                kill -9 $PID
                sleep 2
            fi
        fi
        rm -f "$PID_FILE"
    fi
    
    # Kill any remaining Java processes
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
    echo "✅ Application stopped successfully"
}

# Function to build application
build_app() {
    echo "🔨 Building application..."
    if [ ! -f "$JAR_FILE" ] || [ "$1" == "--rebuild" ]; then
        echo "📦 Compiling and packaging..."
        ./mvnw clean package -DskipTests
        if [ $? -ne 0 ]; then
            echo "❌ Build failed!"
            exit 1
        fi
        echo "✅ Build completed"
    else
        echo "✅ JAR file exists, skipping build"
    fi
}

# Function to start application
start_app() {
    echo "🚀 Starting application..."
    echo "📁 Current directory: $(pwd)"
    echo "☕ Java version: $(java -version 2>&1 | head -n1)"
    echo "📦 JAR size: $(ls -lh $JAR_FILE | awk '{print $5}')"
    echo ""
    
    # Start application in background
    nohup java -jar "$JAR_FILE" > "$LOG_FILE" 2>&1 &
    APP_PID=$!
    echo $APP_PID > "$PID_FILE"
    
    echo "🆔 Process ID: $APP_PID"
    echo "📝 Log file: $LOG_FILE"
    echo ""
    
    # Wait for startup
    echo "⏳ Waiting for application to start..."
    for i in {1..30}; do
        if curl -s http://localhost:8080 >/dev/null 2>&1; then
            echo "✅ Application started successfully!"
            echo ""
            echo "🌐 Access URLs:"
            echo "📍 Main: http://localhost:8080"
            echo "🔄 Return: http://localhost:8080/doi-tra"
            echo "👥 Customers: http://localhost:8080/khach-hang"
            echo "⚙️ Settings: http://localhost:8080/cai-dat"
            echo "🆘 Emergency: http://localhost:8080/emergency/setup"
            echo ""
            echo "📝 View logs: tail -f $LOG_FILE"
            echo "🛑 Stop app: ./stop.sh"
            return 0
        fi
        echo -n "."
        sleep 2
    done
    
    echo ""
    echo "⚠️ Application may still be starting..."
    echo "📝 Check logs: tail -f $LOG_FILE"
}

# Main execution
case "$1" in
    --rebuild)
        stop_app
        build_app --rebuild
        start_app
        ;;
    --stop-only)
        stop_app
        ;;
    --start-only)
        start_app
        ;;
    *)
        stop_app
        build_app
        start_app
        ;;
esac