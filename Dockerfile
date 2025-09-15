# Stage 1: build
FROM maven:3.9.2-eclipse-temurin-17 AS build

WORKDIR /app

# Copy project files
COPY . /app

# Build using Maven
RUN mvn -f pms/pom.xml clean package -DskipTests

# Stage 2: runtime
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/pms/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Start the Spring Boot app
CMD ["java", "-jar", "app.jar"]
