# Stage 1: build  
FROM eclipse-temurin:21-jdk-jammy AS build
RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY pms/pom.xml ./
RUN mvn -B dependency:go-offline
COPY pms/src ./src
RUN mvn -B clean package -DskipTests

# Stage 2: runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
