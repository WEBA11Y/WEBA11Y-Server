FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /WEBA11Y-Server

# 시스템 의존성 설치 (Playwright 실행용)
RUN apt-get update && apt-get install -y \
    apt-utils \
    wget \
    curl \
    unzip \
    gnupg \
    libglib2.0-0 \
    libnss3 \
    libgconf-2-4 \
    libfontconfig1 \
    libxss1 \
    libxtst6 \
    libatk-bridge2.0-0 \
    libgtk-3-0 \
    libdrm2 \
    libxcomposite1 \
    libxdamage1 \
    libxrandr2 \
    libgbm1 \
    libasound2 \
    libxshmfence1 \
    xdg-utils \
    fonts-liberation \
    ca-certificates \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# 임시 디렉토리 설정
VOLUME /tmp

# JAR 파일 복사
ARG JAR_FILE=./build/libs/*.jar
ADD ${JAR_FILE} weba11y-server.jar

# 실행
ENTRYPOINT ["java", "-jar", "weba11y-server.jar"]
