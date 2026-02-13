#!/bin/bash

# =================================
# 🎛️  OSS Application Manager
# =================================

APP_NAME="OSS Application"

show_help() {
    echo "🎛️  $APP_NAME Manager"
    echo "========================="
    echo ""
    echo "📋 Usage: ./app.sh [command]"
    echo ""
    echo "🚀 Commands:"
    echo "   start        - Start application (background)"
    echo "   stop         - Stop application"
    echo "   restart      - Stop and start application"
    echo "   status       - Show application status"
    echo "   logs         - Show live logs (tail -f)"
    echo "   build        - Build JAR file"
    echo "   rebuild      - Force rebuild and restart"
    echo "   run          - Run in foreground (interactive)"
    echo ""
    echo "🌐 URLs (when running):"
    echo "   http://localhost:8080              - Main page"
    echo "   http://localhost:8080/doi-tra      - Return/Exchange"
    echo "   http://localhost:8080/khach-hang    - Customers"
    echo "   http://localhost:8080/cai-dat      - Settings"
    echo "   http://localhost:8080/emergency/setup - Emergency Config"
    echo ""
    echo "📝 Examples:"
    echo "   ./app.sh start     - Start in background"
    echo "   ./app.sh logs      - Watch logs"
    echo "   ./app.sh rebuild   - Force rebuild"
}

case "$1" in
    start)
        echo "🚀 Starting application..."
        ./restart.sh --start-only
        ;;
    stop)
        echo "🛑 Stopping application..."
        ./stop.sh
        ;;
    restart)
        echo "🔄 Restarting application..."
        ./restart.sh
        ;;
    status)
        ./status.sh
        ;;
    logs)
        echo "📝 Showing live logs (Ctrl+C to exit)..."
        echo "========================================="
        tail -f app.log 2>/dev/null || echo "❌ Log file not found. Start the application first."
        ;;
    build)
        echo "🔨 Building application..."
        ./mvnw clean package -DskipTests
        ;;
    rebuild)
        echo "🔄 Force rebuilding and restarting..."
        ./restart.sh --rebuild
        ;;
    run)
        echo "🎮 Running in foreground (Ctrl+C to stop)..."
        ./run.sh
        ;;
    ""|help|--help|-h)
        show_help
        ;;
    *)
        echo "❌ Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac