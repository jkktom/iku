#!/bin/bash
# EC2 배포 자동화 스크립트
# 사용법: ./ec2_deploy.sh <action> [branch]
set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}
log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}
log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}
log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 환경 변수 확인
check_environment() {
    log_info "환경 확인 중..."

    # Docker 설치 확인
    if ! command -v docker &> /dev/null; then
        log_error "Docker가 설치되지 않았습니다."
        install_docker
    fi

    # Docker Compose 설치 확인
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose가 설치되지 않았습니다."
        install_docker_compose
    fi

    log_success "환경 확인 완료"
}

# Docker 설치
install_docker() {
    log_info "Docker 설치 중..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    log_warning "Docker 설치 완료. 재로그인이 필요할 수 있습니다."
}

# Docker Compose 설치
install_docker_compose() {
    log_info "Docker Compose 설치 중..."
    sudo curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    log_success "Docker Compose 설치 완료"
}

# .env 템플릿 생성
create_env_template() {
    cat > .env << 'EOF'
# Database
DB_URL=jdbc:postgresql://db:5432/iku-ai-db
DB_USERNAME=user
DB_PASSWORD=5656

# JWT
JWT_SECRET=4a8NRS86guvb1rzq1vlFPXOR4Nx96iKv4lnBAzDx8xo=

# API Keys (실제 값으로 변경 필요)
GEMINI_API_KEY=AIzaSyAyHpB2ttUcb35cToXNf8oStIGIlIAXsLQ
NAVER_API_ID=your_naver_api_id_here
NAVER_API_SECRET=your_naver_api_secret_here
RIOT_API_KEY=RGAPI-89e5dfe0-e07f-4685-8f75-6b8972f06584

# Clerk (실제 값으로 변경 필요)
CLERK_SECRET_KEY=sk_test_AC10DKsePNWeXAeQPw8WqC6M5RL391ZHDrzcDdErul
CLERK_PUBLISHABLE_KEY=pk_test_Z2l2aW5nLW9jZWxvdC01OS5jbGVyay5hY2NvdW50cy5kZXYk
VITE_CLERK_PUBLISHABLE_KEY=pk_test_Z2l2aW5nLW9jZWxvdC01OS5jbGVyay5hY2NvdW50cy5kZXYk
EOF
    log_warning "⚠️  .env 파일이 생성되었습니다. 실제 API 키로 수정해주세요!"
    log_info "nano .env 명령으로 편집할 수 있습니다."
}

# 프로젝트 설정
setup_project() {
    log_info "프로젝트 설정 중..."

    # .env 파일 생성 (없는 경우)
    if [ ! -f ".env" ]; then
        log_warning ".env 파일이 없습니다. 템플릿을 생성합니다."
        create_env_template
    fi

    # frontend/.env 확인
    if [ ! -f "frontend/.env" ]; then
        log_warning "frontend/.env 파일이 없습니다."
        if [ -f ".env" ]; then
            cp .env frontend/.env
            log_info "루트 .env를 frontend로 복사했습니다."
        fi
    fi

    log_success "프로젝트 설정 완료"
}

# 브랜치별 배포
deploy_branch() {
    local branch=$1

    log_info "$branch 브랜치 배포 시작..."

    case $branch in
        "main")
            log_info "🔵 Main 브랜치 배포 중..."
            docker-compose up -d --build db-main backend-main frontend-main

            log_success "✅ Main 브랜치 배포 완료!"
            echo "🌐 Frontend: http://$(curl -s ifconfig.me):3000"
            echo "🌐 Backend: http://$(curl -s ifconfig.me):8080"
            ;;
        "dev")
            log_info "🟡 Dev 브랜치 배포 중..."
            docker-compose up -d --build db-dev backend-dev frontend-dev

            log_success "✅ Dev 브랜치 배포 완료!"
            echo "🌐 Frontend: http://$(curl -s ifconfig.me):3001"
            echo "🌐 Backend: http://$(curl -s ifconfig.me):8081"
            ;;
        "all")
            log_info "🌈 모든 브랜치 배포 중..."
            docker-compose up -d --build

            log_success "✅ 모든 브랜치 배포 완료!"
            echo "🔵 Main - Frontend: http://$(curl -s ifconfig.me):3000, Backend: http://$(curl -s ifconfig.me):8080"
            echo "🟡 Dev - Frontend: http://$(curl -s ifconfig.me):3001, Backend: http://$(curl -s ifconfig.me):8081"
            ;;
        *)
            log_error "지원하지 않는 브랜치: $branch"
            exit 1
            ;;
    esac
}

# 서비스 상태 확인
check_services() {
    log_info "서비스 상태 확인 중..."

    echo "📊 Container Status:"
    docker-compose ps

    echo ""
    echo "💾 Volume Status:"
    docker volume ls | grep iku || echo "No IKU volumes found"
}

# 메인 실행 로직
main() {
    local action=${1:-"help"}
    local branch=${2:-"all"}

    case $action in
        "setup")
            check_environment
            setup_project
            ;;
        "deploy")
            check_environment
            setup_project
            deploy_branch $branch
            check_services
            ;;
        "status")
            check_services
            ;;
        "help"|*)
            echo "🚀 EC2 배포 자동화 스크립트"
            echo ""
            echo "사용법: $0 <action> [branch]"
            echo ""
            echo "Actions:"
            echo "  setup           - 환경 설정"
            echo "  deploy <branch> - 브랜치 배포 (main|dev|all)"
            echo "  status          - 서비스 상태 확인"
            echo ""
            echo "Examples:"
            echo "  $0 setup"
            echo "  $0 deploy main"
            echo "  $0 deploy all"
            ;;
    esac
}

# 스크립트 실행
main "$@"
