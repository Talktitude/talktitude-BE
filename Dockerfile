FROM openjdk:21-jdk-slim

# ONNX Runtime과 DJL에 필요한 시스템 라이브러리 설치
RUN apt-get update && apt-get install -y \
    libstdc++6 \
    libgomp1 \
    libprotobuf-dev \
    libc6 \
    libgcc-s1 \
    curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY talktitude.jar /app/talktitude.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "talktitude.jar"]