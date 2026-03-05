FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :rate-limiter-demo:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/rate-limiter-demo/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]