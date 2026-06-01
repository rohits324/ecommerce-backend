# Build stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy the pom.xml file to download dependencies first (caching optimization)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Production JRE stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the compiled executable JAR from build container
COPY --from=builder /app/target/*.jar app.jar

# Expose port 8080 (matches Spring Boot application server port)
EXPOSE 8080

# Configure JVM flags for efficient resource utilization in container environments
CMD ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
