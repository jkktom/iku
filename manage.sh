#!/bin/bash

# IKU AI 프로젝트 관리 스크립트 (Main/Dev 브랜치 통합 관리)
# 사용법: ./manage.sh [command] [branch]

set -e

# 색상 코드
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# 로그 함수들
info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

highlight() {
    echo -e "${PURPLE}[IKU-AI]${NC} $1"
}

# 도움말 표시
show_help() {
    echo "========================================"
    highlight "IKU AI 프로젝트 통합 관리 스크립트"
    echo "========================================"
    echo ""
    echo "사용법: ./manage.sh [command] [branch]"
    echo ""
    echo "Commands:"
    echo "  up [branch]       - 서비스 시작"
    echo "  down [branch]     - 서비스 중지"
    echo "  restart [branch]  - 서비스 재시작"
    echo "  logs [branch]     - 로그 확인"
    echo "  build [branch]    - 이미지 빌드"
    echo "  clean [branch]    - 컨테이너, 이미지, 볼륨 정리"
    echo "  status [branch]   - 서비스 상태 확인"
    echo "  shell [service]   - 컨테이너 쉘 접속"
    echo "  backup [branch]   - 데이터베이스 백업"
    echo "  restore [file]    - 데이터베이스 복원"
    echo "  setup             - 초기 환경 설정"
    echo "  info              - 프로젝트 정보 확인"
    echo ""
    echo "Branches:"
    echo "  main - 프로덕션 환경 (포트: 8080, 3000, 5432)"
    echo "  dev  - 개발 환경 (포트: 8081, 3001, 5433)"
    echo "  all  - 모든 서비스 (main + dev)"
    echo ""
    echo "Services (shell 명령용):"
    echo "  backend-main, backend-dev"
    echo "  frontend-main, frontend-dev"
    echo "  db-main, db-dev"
    echo ""
    echo "Examples:"
    echo "  ./manage.sh up main          - 프로덕션 환경 시작"
    echo "  ./manage.sh up dev           - 개발 환경 시작"
    echo "  ./manage.sh up all           - 모든 환경 시작"
    echo "  ./manage.sh logs dev         - 개발 환경 로그 확인"
    echo "  ./manage.sh shell backend-dev - 개발 백엔드 쉘 접속"
    echo "  ./manage.sh backup main      - 프로덕션 DB 백업"
}

# 서비스 선택
get_services() {
    local branch=$1
    case $branch in
        "main")
            echo "db-main backend-main frontend-main"
            ;;
        "dev")
            echo "db-dev backend-dev frontend-dev"
            ;;
        "all")
            echo "db-main backend-main frontend-main db-dev backend-dev frontend-dev adminer"
            ;;
        *)
            error "Unknown branch: $branch"
            error "Available branches: main, dev, all"
            exit 1
            ;;
    esac
}

# 초기 환경 설정
setup_environment() {
    highlight "초기 환경 설정을 시작합니다..."
    
    # .env 파일 확인 및 생성
    if [ ! -f "backend/.env" ]; then
        warning "backend/.env 파일이 없습니다. .env.example에서 복사합니다."
        cp backend/.env.example backend/.env
        warning "backend/.env 파일을 편집하여 실제 값을 입력하세요."
    fi
    
    if [ ! -f "frontend/.env" ]; then
        warning "frontend/.env 파일이 없습니다. .env.example에서 복사합니다."
        cp frontend/.env.example frontend/.env
        warning "frontend/.env 파일을 편집하여 실제 값을 입력하세요."
    fi
    
    # Docker 네트워크 생성
    for network in iku-main-network iku-dev-network; do
        if ! docker network ls | grep -q "$network"; then
            info "Docker 네트워크를 생성합니다: $network"
            docker network create $network
        fi
    done
    
    success "초기 환경 설정이 완료되었습니다!"
    info "다음 명령어로 서비스를 시작할 수 있습니다:"
    info "  ./manage.sh up main  (프로덕션)"
    info "  ./manage.sh up dev   (개발)"
    info "  ./manage.sh up all   (전체)"
}

# 서비스 시작
start_services() {
    local branch=$1
    local services=$(get_services $branch)
    
    highlight "[$branch] 브랜치 서비스를 시작합니다..."
    info "서비스: $services"
    
    if [ "$branch" = "all" ]; then
        docker-compose up -d
    else
        docker-compose up -d $services
    fi
    
    success "[$branch] 서비스가 시작되었습니다!"
    
    # 서비스 URL 정보 표시
    if [ "$branch" = "main" ] || [ "$branch" = "all" ]; then
        info "프로덕션 서비스:"
        info "  Frontend: https://iku.life"
        info "  Backend:  https://iku.life/api"
        info "  Database: localhost:5432"
    fi
    
    if [ "$branch" = "dev" ] || [ "$branch" = "all" ]; then
        info "개발 서비스:"
        info "  Frontend: https://iku.life (dev port 3001)"
        info "  Backend:  https://iku.life/api (dev port 8081)"
        info "  Database: localhost:5433"
    fi
    
    if [ "$branch" = "all" ]; then
        info "관리 도구:"
        info "  Adminer:  http://13.125.59.135:8082 (DB 관리)"
    fi
}

# 서비스 중지
stop_services() {
    local branch=$1
    local services=$(get_services $branch)
    
    highlight "[$branch] 서비스를 중지합니다..."
    
    if [ "$branch" = "all" ]; then
        docker-compose down
    else
        docker-compose stop $services
    fi
    
    success "[$branch] 서비스가 중지되었습니다!"
}

# 서비스 재시작
restart_services() {
    local branch=$1
    local services=$(get_services $branch)
    
    highlight "[$branch] 서비스를 재시작합니다..."
    
    for service in $services; do
        info "재시작 중: $service"
        docker-compose restart $service
    done
    
    success "[$branch] 서비스가 재시작되었습니다!"
}

# 로그 확인
show_logs() {
    local branch=$1
    local services=$(get_services $branch)
    
    highlight "[$branch] 로그를 표시합니다... (Ctrl+C로 종료)"
    docker-compose logs -f $services
}

# 이미지 빌드
build_images() {
    local branch=$1
    local services=$(get_services $branch)
    
    highlight "[$branch] 이미지를 빌드합니다..."
    
    if [ "$branch" = "all" ]; then
        docker-compose build --no-cache
    else
        for service in $services; do
            if [[ $service == *"backend"* ]] || [[ $service == *"frontend"* ]]; then
                info "빌드 중: $service"
                docker-compose build --no-cache $service
            fi
        done
    fi
    
    success "[$branch] 이미지 빌드가 완료되었습니다!"
}

# 정리
clean_environment() {
    local branch=$1
    local services=$(get_services $branch)
    
    warning "[$branch] 모든 컨테이너, 이미지, 볼륨을 삭제합니다. 계속하시겠습니까? (y/N)"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        highlight "[$branch] 환경을 정리합니다..."
        
        if [ "$branch" = "all" ]; then
            docker-compose down -v --rmi all
        else
            # 개별 서비스 정리
            docker-compose stop $services
            for service in $services; do
                docker-compose rm -f $service
            done
        fi
        
        success "[$branch] 정리가 완료되었습니다!"
    else
        info "정리가 취소되었습니다."
    fi
}

# 상태 확인
show_status() {
    local branch=$1
    local services=$(get_services $branch)
    
    highlight "[$branch] 서비스 상태:"
    docker-compose ps $services
    
    echo ""
    info "네트워크 상태:"
    docker network ls | grep iku
    
    echo ""
    info "볼륨 상태:"
    docker volume ls | grep iku
}

# 컨테이너 쉘 접속
connect_shell() {
    local service=$1
    
    if [ -z "$service" ]; then
        error "서비스명을 지정해주세요."
        error "사용법: ./manage.sh shell [service_name]"
        error "예시: ./manage.sh shell backend-main"
        exit 1
    fi
    
    # 컨테이너가 실행 중인지 확인
    if ! docker ps | grep -q "$service"; then
        error "컨테이너가 실행 중이지 않습니다: $service"
        info "먼저 서비스를 시작하세요: ./manage.sh up [branch]"
        exit 1
    fi
    
    highlight "[$service] 컨테이너에 접속합니다..."
    docker exec -it "iku-${service//-/-}" /bin/bash 2>/dev/null || \
    docker exec -it "iku-${service//-/-}" /bin/sh
}

# 데이터베이스 백업
backup_database() {
    local branch=$1
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    local backup_file="backup/iku_${branch}_backup_$timestamp.sql"
    local container_name="iku-${branch}-db"
    
    if [ "$branch" != "main" ] && [ "$branch" != "dev" ]; then
        error "백업은 main 또는 dev 브랜치만 지원합니다."
        exit 1
    fi
    
    highlight "[$branch] 데이터베이스를 백업합니다..."
    mkdir -p backup
    
    docker exec $container_name pg_dump -U user "iku-${branch}-db" > $backup_file
    
    success "[$branch] 백업이 완료되었습니다: $backup_file"
}

# 데이터베이스 복원
restore_database() {
    local backup_file=$1
    
    if [ -z "$backup_file" ]; then
        error "백업 파일을 지정해주세요."
        error "사용법: ./manage.sh restore [backup_file]"
        exit 1
    fi
    
    if [ ! -f "$backup_file" ]; then
        error "백업 파일을 찾을 수 없습니다: $backup_file"
        exit 1
    fi
    
    # 파일명에서 브랜치 추출
    local branch=""
    if [[ $backup_file == *"main"* ]]; then
        branch="main"
    elif [[ $backup_file == *"dev"* ]]; then
        branch="dev"
    else
        error "백업 파일에서 브랜치를 식별할 수 없습니다."
        error "파일명에 'main' 또는 'dev'가 포함되어야 합니다."
        exit 1
    fi
    
    warning "[$branch] 데이터베이스를 복원합니다. 기존 데이터가 삭제됩니다. 계속하시겠습니까? (y/N)"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        highlight "[$branch] 데이터베이스를 복원합니다..."
        docker exec -i "iku-${branch}-db" psql -U user -d "iku-${branch}-db" < $backup_file
        success "[$branch] 복원이 완료되었습니다!"
    else
        info "복원이 취소되었습니다."
    fi
}

# 프로젝트 정보 표시
show_info() {
    highlight "IKU AI 프로젝트 정보"
    echo "========================================"
    echo "프로젝트: IKU AI - 게이밍 AI 분석 플랫폼"
    echo "아키텍처: 마이크로서비스 (Spring Boot + React/Remix)"
    echo ""
    echo "백엔드 기술스택:"
    echo "  - Spring Boot 3.4.5"
    echo "  - PostgreSQL 16"
    echo "  - JWT 인증"
    echo "  - Clerk 인증"
    echo "  - Gemini AI API"
    echo "  - Riot Games API"
    echo ""
    echo "프론트엔드 기술스택:"
    echo "  - React 18"
    echo "  - Remix"
    echo "  - TypeScript"
    echo "  - Tailwind CSS"
    echo ""
    echo "인프라:"
    echo "  - Docker & Docker Compose"
    echo "  - PostgreSQL"
    echo "  - Nginx (프로덕션)"
    echo ""
    echo "환경별 포트:"
    echo "  Main (프로덕션): Frontend(3000), Backend(8080), DB(5432)"
    echo "  Dev (개발):      Frontend(3001), Backend(8081), DB(5433)"
    echo "  관리 도구:       Adminer(8082)"
    echo "========================================"
}

# 메인 로직
main() {
    local command=$1
    local branch=$2
    
    # 기본 브랜치는 main
    if [ -z "$branch" ] && [ "$command" != "setup" ] && [ "$command" != "info" ] && [ "$command" != "shell" ]; then
        branch="main"
    fi
    
    case $command in
        "up")
            start_services $branch
            ;;
        "down")
            stop_services $branch
            ;;
        "restart")
            restart_services $branch
            ;;
        "logs")
            show_logs $branch
            ;;
        "build")
            build_images $branch
            ;;
        "clean")
            clean_environment $branch
            ;;
        "status")
            show_status $branch
            ;;
        "shell")
            connect_shell $branch
            ;;
        "backup")
            backup_database $branch
            ;;
        "restore")
            restore_database $branch
            ;;
        "setup")
            setup_environment
            ;;
        "info")
            show_info
            ;;
        "help"|"--help"|"-h"|"")
            show_help
            ;;
        *)
            error "Unknown command: $command"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# 스크립트 실행
main "$@"
