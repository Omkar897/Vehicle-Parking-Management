# Stage 1: build
FROM maven:3.9.2-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only the necessary files first to leverage Docker cache
COPY pms/pom.xml ./

# Download dependencies first (cache this layer)
RUN mvn -B dependency:go-offline -f pom.xml

# Copy source files
COPY pms/src ./src

# Build the application
RUN mvn -B package -DskipTests

# Stage 2: runtime
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Set JVM options for better memory management
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Expose port
EXPOSE 8080

# Start the Spring Boot app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
