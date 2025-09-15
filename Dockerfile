# Use official OpenJDK 17
FROM openjdk:17-slim

# Set working directory
WORKDIR /app

# Copy project
COPY . /app

# Install Maven
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Build the project using system Maven (not mvnw)
RUN cd pms && mvn clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the JAR (explicit filename)
CMD ["java", "-jar", "pms/target/vehicle-parking-management-system-0.0.1-SNAPSHOT.jar"]
