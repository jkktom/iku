# 서버 배포 설정 가이드 - 경량 버전

## 🎯 개요
디스크 공간 부족 문제를 해결한 경량 배포 시스템

## 📋 서버에서 실행할 명령어들

### 1. Webhook 서버 설치
```bash
# 서버에 SSH 접속 후
sudo npm install -g webhook

# 또는 Go 버전 설치 (더 가벼움)
sudo apt update
sudo apt install webhook
```

### 2. 배포 스크립트 생성
```bash
# /home/ubuntu/auto-deploy.sh 파일 생성
sudo nano /home/ubuntu/auto-deploy.sh
```

다음 내용을 붙여넣기:
```bash
#!/bin/bash
set -e

echo "🚀 Starting deployment process..."
echo "📅 $(date)"

# 프로젝트 디렉토리로 이동
cd /home/ubuntu/iku

# Git pull
echo "📥 Pulling latest changes..."
git fetch origin
git reset --hard origin/dev

# 메모리 상태 확인
echo "💾 Checking memory status..."
free -h

# Docker 정리 - 메모리 확보
echo "🧹 Cleaning up Docker resources..."
docker container prune -f || true
docker image prune -f || true
docker system prune -f || true

# 기존 dev 컨테이너들 중지 (순차적으로)
echo "⏹️ Stopping existing dev containers..."
docker-compose stop frontend-dev || true
docker-compose stop backend-dev || true

# 메모리 상태 재확인
echo "💾 Memory after cleanup:"
free -h

# 백엔드 먼저 빌드 및 시작
echo "🔨 Building and starting backend..."
docker-compose up -d --build --no-deps backend-dev

# 백엔드 헬스체크 대기
echo "⏳ Waiting for backend to be ready..."
timeout=300
counter=0
while ! docker exec iku-dev-backend curl -f http://127.0.0.1:8080/actuator/health 2>/dev/null; do
    sleep 5
    counter=$((counter + 5))
    if [ $counter -ge $timeout ]; then
        echo "❌ Backend health check timeout"
        exit 1
    fi
    echo "⏳ Waiting for backend... ($counter/${timeout}s)"
done

echo "✅ Backend is healthy!"

# 프론트엔드 빌드 및 시작
echo "🔨 Building and starting frontend..."
docker-compose up -d --build --no-deps frontend-dev

# 프론트엔드 헬스체크 대기
echo "⏳ Waiting for frontend to be ready..."
timeout=180
counter=0
while ! docker exec iku-dev-frontend curl -f http://127.0.0.1:3000 2>/dev/null; do
    sleep 5
    counter=$((counter + 5))
    if [ $counter -ge $timeout ]; then
        echo "❌ Frontend health check timeout"
        exit 1
    fi
    echo "⏳ Waiting for frontend... ($counter/${timeout}s)"
done

echo "✅ Frontend is healthy!"

# 전체 dev 서비스 상태 확인
echo "🔍 Checking all dev services..."
docker-compose ps | grep -E "(backend-dev|frontend-dev|db-dev|redis|nginx)"

# 최종 메모리 상태 확인
echo "💾 Final memory status:"
free -h

echo "🎉 Deployment completed successfully!"
echo "🌐 Services available at:"
echo "   - Backend: http://13.125.59.135:8080"
echo "   - Frontend: http://13.125.59.135:3000"
```

### 3. 실행 권한 부여
```bash
chmod +x /home/ubuntu/auto-deploy.sh
```

### 4. Webhook 설정 파일 생성
```bash
# /home/ubuntu/hooks.json 파일 생성
sudo nano /home/ubuntu/hooks.json
```

다음 내용을 붙여넣기:
```json
[
  {
    "id": "deploy",
    "execute-command": "/home/ubuntu/auto-deploy.sh",
    "command-working-directory": "/home/ubuntu",
    "response-message": "Deployment triggered successfully!",
    "trigger-rule": {
      "match": {
        "type": "payload-hash-sha1",
        "secret": "",
        "parameter": {
          "source": "payload",
          "name": "repository.name"
        }
      }
    }
  }
]
```

### 5. Webhook 서버 시작
```bash
# 백그라운드에서 webhook 서버 실행
nohup webhook -hooks /home/ubuntu/hooks.json -verbose -port 9000 > /home/ubuntu/webhook.log 2>&1 &

# 자동 시작 설정 (재부팅 시)
echo '@reboot webhook -hooks /home/ubuntu/hooks.json -verbose -port 9000 > /home/ubuntu/webhook.log 2>&1 &' | crontab -
```

### 6. 방화벽 설정 (필요시)
```bash
sudo ufw allow 9000
```

## 🚀 사용법

1. 위 설정 완료 후
2. `dev` 브랜치에 코드 푸시
3. GitHub Actions가 자동으로 서버에 배포 요청
4. 서버에서 자동 배포 실행

## 📊 배포 로그 확인
```bash
# 실시간 로그 확인
tail -f /home/ubuntu/webhook.log

# 배포 스크립트 수동 실행 테스트
/home/ubuntu/auto-deploy.sh
```ㅍ

## ✅ 장점
- GitHub Secrets 불필요
- 권한 문제 없음  
- 서버에서 직접 제어
- 로그 확인 가능
- 메모리 최적화 적용

이 방법이 가장 안전하고 확실합니다!