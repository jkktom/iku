FROM node:20-alpine as builder

# 작업 디렉토리 설정
WORKDIR /app

# package.json과 package-lock.json 복사 (레이어 캐싱 최적화)
COPY package*.json ./

# 의존성 설치
RUN npm ci --only=production && npm cache clean --force

# 소스 코드 복사
COPY . .

# 환경변수 설정 (빌드 시)
ENV NODE_ENV=production

# 애플리케이션 빌드
RUN npm run build

# 실행 스테이지
FROM node:20-alpine

# 비루트 사용자 생성
RUN addgroup -g 1001 -S nodejs && \
    adduser -S remix -u 1001

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 파일과 필요한 파일들 복사
COPY --from=builder --chown=remix:nodejs /app/build ./build
COPY --from=builder --chown=remix:nodejs /app/public ./public
COPY --from=builder --chown=remix:nodejs /app/package*.json ./

# 프로덕션 의존성만 설치
RUN npm ci --only=production && npm cache clean --force

# 비루트 사용자로 실행
USER remix

# 포트 노출
EXPOSE 3000

# 헬스체크 추가
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:3000/ || exit 1

# 애플리케이션 실행
CMD ["npm", "start"]
