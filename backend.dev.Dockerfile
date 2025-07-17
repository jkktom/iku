FROM openjdk:17-jdk-slim

WORKDIR /app

# 개발용 도구 설치
RUN apt-get update && apt-get install -y \
    curl \
    procps \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Gradle 직접 설치
RUN wget https://services.gradle.org/distributions/gradle-8.5-bin.zip \
    && unzip gradle-8.5-bin.zip \
    && rm gradle-8.5-bin.zip \
    && mv gradle-8.5 /opt/gradle \
    && ln -s /opt/gradle/bin/gradle /usr/bin/gradle

# 전체 프로젝트 복사
COPY . .

# Gradle로 의존성 다운로드 (wrapper 대신 직접 설치한 gradle 사용)
RUN gradle :backend:dependencies --no-daemon

# 개발 모드로 실행
EXPOSE 8080
CMD ["gradle", ":backend:bootRun", "--no-daemon"]
