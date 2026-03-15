# ===== Build Stage =====
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== Runtime Stage =====
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S banking && adduser -S banking -G banking

# create log directory
RUN mkdir -p /var/log/banking-api && chown -R banking:banking /var/log/banking-api

# Copy jar from build stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown -R banking:banking /app
USER banking

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
