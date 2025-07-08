# Backend Main 브랜치용 Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# Gradle Wrapper 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 다운로드 및 빌드
RUN ./gradlew build -x test

# 애플리케이션 실행
EXPOSE 8080
CMD ["./gradlew", "bootRun"]
