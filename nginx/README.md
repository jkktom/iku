# Nginx 설정 (나중에 사용)

이 디렉토리는 **프로덕션 배포시 필요한 Nginx 설정**을 포함합니다.

## 🚀 Nginx가 필요한 시점

- **실제 도메인 배포** (https://iku-ai.com)
- **SSL/TLS 인증서 설정**
- **높은 트래픽 처리**
- **로드밸런싱**
- **CDN 연동**

## 📁 파일 구조

```
nginx/
├── nginx.conf          # Nginx 메인 설정
├── ssl/                # SSL 인증서 (추후 생성)
│   ├── cert.pem
│   └── key.pem
└── README.md           # 이 파일
```

## 🔧 활성화 방법

1. **SSL 인증서 준비**
   ```bash
   # Let's Encrypt 사용 권장
   certbot --nginx -d your-domain.com
   ```

2. **docker-compose.prod.yml에서 nginx 주석 해제**
   ```yaml
   nginx:
     image: nginx:alpine
     # ... 설정들
   ```

3. **도메인 설정**
   - DNS A 레코드 설정
   - 도메인 nginx.conf에 업데이트

## 📝 주요 기능

- **리버스 프록시**: 요청을 적절한 서비스로 전달
- **SSL 종료**: HTTPS 처리
- **압축**: Gzip으로 전송 데이터 최적화
- **캐싱**: 정적 파일 캐싱
- **보안**: Rate limiting, 보안 헤더
- **로드밸런싱**: 여러 인스턴스 간 부하 분산

## 🎯 현재 상태

**Nginx 비활성화됨** - 개발/테스트 환경에서는 불필요  
**직접 연결**: Frontend(3000) ↔ Backend(8080)
