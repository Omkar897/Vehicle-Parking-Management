# Stage 1: build
FROM maven:3.9.2-eclipse-temurin-17 AS build

WORKDIR /app

# Copy POM files first (this layer is cached if pom.xml doesn't change)
COPY pms/pom.xml ./

# Download all dependencies (this layer is cached unless pom.xml changes)
RUN mvn -B dependency:go-offline

# Copy source files
COPY pms/src ./src

# Build the application (this layer is rebuilt when any source file changes)
RUN mvn -B clean package -DskipTests

# Stage 2: runtime
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Set JVM options for better memory management
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Create a non-root user and switch to it
RUN addgroup --system --gid 1001 appuser && \
    adduser --system --uid 1001 --gid 1001 appuser && \
    chown -R appuser:appuser /app

USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Start the Spring Boot app
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
