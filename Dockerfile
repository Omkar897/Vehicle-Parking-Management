# Use official OpenJDK 17 image
FROM openjdk:17-slim

# Set working directory
WORKDIR /app

# Copy all project files into container
COPY . /app

# Make mvnw executable
RUN chmod +x ./pms/mvnw

# Install Maven (optional, in case you want to use system mvn)
RUN apt-get update && apt-get install -y maven

# Build the Spring Boot project
RUN cd pms && ./mvnw clean package -DskipTests

# Expose port used by Spring Boot (default 8080)
EXPOSE 8080

# Start the Spring Boot app
CMD ["java", "-jar", "pms/target/*.jar"]
