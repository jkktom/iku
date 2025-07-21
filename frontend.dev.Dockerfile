FROM node:20-alpine

WORKDIR /app

# 개발용 도구 설치
RUN apk add --no-cache git

# package.json과 package-lock.json 먼저 복사
COPY frontend/package*.json ./

# node_modules 캐시 무효화 및 클린 설치
RUN rm -rf node_modules package-lock.json
RUN npm cache clean --force
RUN npm install

# 소스 코드 복사
COPY frontend/ ./

# Remix 개발 서버 설정
ENV HOST=0.0.0.0
ENV PORT=3000

# 개발 서버 실행 (SSR 안정화)
EXPOSE 3000
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0", "--port", "3000"]
