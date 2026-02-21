FROM maven:3.9.11-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy build metadata first to leverage layer caching.
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src/ src/
RUN mvn -B -DskipTests clean package
RUN JAR_FILE="$(ls target/*.jar | grep -v '\.original$' | head -n 1)" \
    && test -n "$JAR_FILE" \
    && cp "$JAR_FILE" target/app.jar

FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

COPY --from=builder /workspace/target/app.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
