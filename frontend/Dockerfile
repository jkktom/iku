# Frontend Main 브랜치용 Dockerfile (Production)
FROM node:20-alpine

WORKDIR /app

# 패키지 파일들 복사
COPY package*.json ./

# 의존성 설치
RUN npm ci --only=production

# 소스 코드 복사
COPY . .

# 환경변수 파일 복사
COPY .env .

# 프로덕션 빌드
RUN npm run build

# 프로덕션 서버 실행
EXPOSE 3000
CMD ["npm", "start"]
