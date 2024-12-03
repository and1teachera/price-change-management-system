FROM eclipse-temurin:21-jre-jammy AS builder

WORKDIR /app
COPY build/libs/*.jar app.jar

RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder app/dependencies/ ./
COPY --from=builder app/spring-boot-loader/ ./
COPY --from=builder app/snapshot-dependencies/ ./
COPY --from=builder app/application/ ./

HEALTHCHECK --interval=30s --timeout=3s \
    CMD ["curl", "-f", "http://localhost:8080/actuator/health"]

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]