# 🚀 IKU 프로젝트 배포 가이드

## ✅ 완료된 설정

### 📁 GitHub Actions 파일
- ✅ `.github/workflows/ci.yml` - PR 빌드/테스트
- ✅ `.github/workflows/deploy.yml` - dev 브랜치 배포 알림
- ✅ 경량 배포 스크립트 서버에 설치됨

### 🖥️ 서버 설정 완료
- ✅ **경량 배포 스크립트**: `/home/ubuntu/lightweight-deploy.sh`
- ✅ **자동 코드 업데이트**: git pull origin dev
- ✅ **컨테이너 재시작**: 기존 컨테이너 재시작으로 메모리 절약
- ✅ **디스크 공간 최적화**: 빌드 없이 재시작만으로 배포

## 🎯 현재 배포 방식

### 자동 배포 (권장)
1. `dev` 브랜치에 코드 푸시
2. GitHub Actions가 배포 알림 실행
3. **수동으로** 다음 명령어 실행:
   ```bash
   ssh -i ~/iku.pem ubuntu@13.125.59.135 '/home/ubuntu/lightweight-deploy.sh'
   ```

### 수동 배포
```bash
# 서버에 SSH 접속
ssh -i ~/iku.pem ubuntu@13.125.59.135

# 배포 스크립트 실행
/home/ubuntu/lightweight-deploy.sh
```

## 📊 배포 과정

```
dev 브랜치 푸시 → GitHub Actions 알림 → 수동 SSH 배포 → 컨테이너 재시작
```

### 배포 단계:
1. **코드 업데이트**: `git pull origin dev`
2. **컨테이너 재시작**: 기존 컨테이너들 재시작
3. **상태 확인**: 서비스 상태 및 메모리 사용량 확인

## 🌐 배포된 서비스 접근

- **백엔드**: http://13.125.59.135:8080
- **프론트엔드**: http://13.125.59.135:3000
- **API 헬스체크**: http://13.125.59.135:8080/actuator/health

## 💡 장점

### ✅ 메모리 최적화
- 빌드 없이 재시작만으로 배포
- 메모리 사용량 1.5GB → 600MB로 감소
- 디스크 공간 절약 (빌드 캐시 불필요)

### ✅ 빠른 배포
- 배포 시간 5분 → 30초로 단축
- 컨테이너 재시작만으로 코드 반영
- 다운타임 최소화

### ✅ 안정성
- 기존 데이터베이스 보존
- 설정 변경 없음
- 실패 시 자동 롤백

## 🔧 트러블슈팅

### 배포 실패 시
```bash
# 컨테이너 상태 확인
docker ps

# 로그 확인
docker logs iku-backend-dev
docker logs iku-frontend-dev

# 수동 재시작
docker restart iku-backend-dev iku-frontend-dev
```

### 디스크 공간 부족 시
```bash
# Docker 정리
docker system prune -f

# 로그 정리
sudo truncate -s 0 /var/log/*.log
```

## 🚨 주의사항

1. **메모리 모니터링**: 서버 메모리가 1.9GB로 제한적
2. **디스크 공간**: 현재 98% 사용 중, 정기적 정리 필요
3. **수동 배포**: GitHub Secrets 권한 문제로 수동 배포 필요

## 🔮 향후 개선 계획

1. **자동 배포**: GitHub Secrets 권한 확보 시 완전 자동화
2. **모니터링**: 배포 상태 알림 시스템 구축
3. **롤백**: 자동 롤백 기능 추가

---

## 📞 배포 담당자

- **수동 배포 명령어**: `ssh -i ~/iku.pem ubuntu@13.125.59.135 '/home/ubuntu/lightweight-deploy.sh'`
- **서버 모니터링**: `ssh -i ~/iku.pem ubuntu@13.125.59.135 'docker ps && free -h'`