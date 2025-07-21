FROM openjdk:17-jdk-slim

WORKDIR /app

# Gradle wrapper 전체 복사
COPY gradlew ./
COPY gradlew.bat ./
COPY gradle ./gradle
COPY settings.gradle ./

# 백엔드 빌드 파일 복사
COPY backend/build.gradle ./backend/

# 백엔드 소스 코드 복사
COPY backend/src ./backend/src

# 실행 권한 부여
RUN chmod +x ./gradlew

# Gradle wrapper 확인 및 빌드
RUN ./gradlew --version
RUN ./gradlew :backend:build -x test --no-daemon

# JAR 파일 실행
EXPOSE 8080
CMD ["sh", "-c", "java -jar ./backend/build/libs/*.jar"]
