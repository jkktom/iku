#!/bin/bash

# 서비스 관리 스크립트

set -e

show_help() {
    echo "🛠️  IKU Multi-Branch Service Manager"
    echo ""
    echo "Usage: ./manage.sh [COMMAND] [BRANCH]"
    echo ""
    echo "Commands:"
    echo "  start [main|dev|all]    - 서비스 시작"
    echo "  stop [main|dev|all]     - 서비스 중지"
    echo "  restart [main|dev|all]  - 서비스 재시작"
    echo "  logs [main|dev|all]     - 로그 확인"
    echo "  status                  - 전체 상태 확인"
    echo "  clean                   - 모든 컨테이너 및 볼륨 삭제"
    echo "  setup                   - 초기 설정"
    echo ""
    echo "Examples:"
    echo "  ./manage.sh start main   # Main 브랜치 서비스 시작"
    echo "  ./manage.sh logs dev     # Dev 브랜치 로그 확인"
    echo "  ./manage.sh restart all  # 모든 서비스 재시작"
}

start_services() {
    local branch=$1
    
    case $branch in
        "main")
            echo "🔵 Starting MAIN services..."
            docker-compose up -d db-main backend-main frontend-main
            echo "✅ Main services started!"
            echo "🌐 Frontend: http://localhost:3000"
            echo "🌐 Backend: http://localhost:8080"
            ;;
        "dev")
            echo "🟡 Starting DEV services..."
            docker-compose up -d db-dev backend-dev frontend-dev
            echo "✅ Dev services started!"
            echo "🌐 Frontend: http://localhost:3001" 
            echo "🌐 Backend: http://localhost:8081"
            ;;
        "all")
            echo "🌈 Starting ALL services..."
            docker-compose up -d
            echo "✅ All services started!"
            echo "🔵 Main - Frontend: http://localhost:3000, Backend: http://localhost:8080"
            echo "🟡 Dev - Frontend: http://localhost:3001, Backend: http://localhost:8081"
            ;;
        *)
            echo "❌ Invalid branch: $branch"
            echo "Use: main, dev, or all"
            exit 1
            ;;
    esac
}

stop_services() {
    local branch=$1
    
    case $branch in
        "main")
            echo "🔵 Stopping MAIN services..."
            docker-compose stop backend-main frontend-main db-main
            ;;
        "dev")
            echo "🟡 Stopping DEV services..."
            docker-compose stop backend-dev frontend-dev db-dev
            ;;
        "all")
            echo "🌈 Stopping ALL services..."
            docker-compose down
            ;;
        *)
            echo "❌ Invalid branch: $branch"
            exit 1
            ;;
    esac
    echo "✅ Services stopped!"
}

restart_services() {
    echo "🔄 Restarting services..."
    stop_services $1
    sleep 2
    start_services $1
}

show_logs() {
    local branch=$1
    
    case $branch in
        "main")
            echo "📋 Showing MAIN services logs..."
            docker-compose logs -f backend-main frontend-main
            ;;
        "dev")
            echo "📋 Showing DEV services logs..."
            docker-compose logs -f backend-dev frontend-dev
            ;;
        "all")
            echo "📋 Showing ALL services logs..."
            docker-compose logs -f
            ;;
        *)
            echo "❌ Invalid branch: $branch"
            exit 1
            ;;
    esac
}

show_status() {
    echo "📊 Service Status:"
    docker-compose ps
    echo ""
    echo "💾 Volume Status:"
    docker volume ls | grep iku
    echo ""
    echo "🌐 Network Status:"
    docker network ls | grep iku
}

clean_all() {
    echo "🧹 Cleaning up..."
    read -p "⚠️  This will remove all containers, volumes, and networks. Continue? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker-compose down -v --remove-orphans
        docker system prune -f
        echo "✅ Cleanup completed!"
    else
        echo "❌ Cleanup cancelled."
    fi
}

setup_environment() {
    echo "🔧 Setting up environment..."
    
    # 실행 권한 부여
    chmod +x deploy.sh
    chmod +x manage.sh
    
    # .env 파일 확인
    if [ ! -f "frontend/.env" ]; then
        echo "⚠️  frontend/.env not found. Please create it with your environment variables."
    fi
    
    # 네트워크 생성 (필요시)
    docker network create iku-main-network 2>/dev/null || true
    docker network create iku-dev-network 2>/dev/null || true
    
    echo "✅ Environment setup completed!"
}

# 메인 로직
case $1 in
    "start")
        start_services $2
        ;;
    "stop")
        stop_services $2
        ;;
    "restart")
        restart_services $2
        ;;
    "logs")
        show_logs $2
        ;;
    "status")
        show_status
        ;;
    "clean")
        clean_all
        ;;
    "setup")
        setup_environment
        ;;
    *)
        show_help
        exit 1
        ;;
esac
