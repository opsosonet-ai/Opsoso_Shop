#!/bin/bash

# =================================
# 🚀 OSS Application Launcher
# =================================

echo "🚀 Starting OSS Application..."
echo "📁 Current directory: $(pwd)"
echo "☕ Java version: $(java -version 2>&1 | head -n1)"
echo ""

# Check if JAR exists
JAR_FILE="target/demo-0.0.1-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found: $JAR_FILE"
    echo "🔨 Building application..."
    ./mvnw clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "❌ Build failed!"
        exit 1
    fi
fi

echo "✅ JAR file ready: $(ls -lh $JAR_FILE | awk '{print $5}')"
echo ""
echo "🌐 Starting web server..."
echo "📍 URL: http://localhost:8080"
echo "🆘 Emergency Setup: http://localhost:8080/emergency/setup"
echo ""
echo "Press Ctrl+C to stop the application"
echo "========================================="
echo ""

# Run the application
java -jar "$JAR_FILE"