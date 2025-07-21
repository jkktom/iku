FROM node:20-alpine

WORKDIR /app

# 패키지 파일들 복사
COPY frontend/package*.json ./

# 의존성 설치
RUN npm ci --only=production

# 소스 코드 복사
COPY frontend/ ./

# 프로덕션 빌드
RUN npm run build

# 프로덕션 서버 실행
EXPOSE 3000
CMD ["npm", "start"]
