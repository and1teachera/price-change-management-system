FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .
COPY services services

RUN chmod +x ./gradlew
RUN --mount=type=cache,target=/root/.gradle ./gradlew clean build -x test

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/services/rms-integration-service/build/libs/*.jar app.jar

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s \
    CMD ["curl", "-f", "http://localhost:8080/actuator/health"]

ENTRYPOINT ["java", "-jar", "app.jar"]