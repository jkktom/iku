#!/bin/bash

# AWS EC2 배포 스크립트

set -e

echo "🚀 Starting deployment process..."

# 환경 변수 확인
if [ -z "$BRANCH" ]; then
    echo "❌ Error: BRANCH environment variable is not set"
    echo "Usage: BRANCH=main|dev ./deploy.sh"
    exit 1
fi

# Git pull (해당 브랜치로)
echo "📦 Pulling latest changes from $BRANCH branch..."
git fetch origin
git checkout $BRANCH
git pull origin $BRANCH

# 환경별 배포
case $BRANCH in
    "main")
        echo "🔵 Deploying MAIN branch..."
        # Main 서비스들만 재시작
        docker-compose up -d --build db-main backend-main frontend-main
        echo "✅ Main branch deployed successfully!"
        echo "🌐 Frontend: http://localhost:3000"
        echo "🌐 Backend: http://localhost:8080"
        echo "🗃️ Database: localhost:5432"
        ;;
    "dev")
        echo "🟡 Deploying DEV branch..."
        # Dev 서비스들만 재시작
        docker-compose up -d --build db-dev backend-dev frontend-dev
        echo "✅ Dev branch deployed successfully!"
        echo "🌐 Frontend: http://localhost:3001"
        echo "🌐 Backend: http://localhost:8081"
        echo "🗃️ Database: localhost:5433"
        ;;
    *)
        echo "❌ Error: Unknown branch '$BRANCH'"
        echo "Supported branches: main, dev"
        exit 1
        ;;
esac

# 서비스 상태 확인
echo "📊 Checking service status..."
docker-compose ps

# 로그 출력 옵션
read -p "📋 Do you want to see logs? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    if [ "$BRANCH" = "main" ]; then
        docker-compose logs -f backend-main frontend-main
    else
        docker-compose logs -f backend-dev frontend-dev
    fi
fi

echo "🎉 Deployment completed!"
