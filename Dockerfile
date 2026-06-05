# Use a lightweight JRE 21 runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the compiled JAR from the target directory (built by maven on the host)
COPY target/app.jar app.jar

# Expose port 8080 (matches Spring Boot application server port)
EXPOSE 8080

# Configure JVM flags for efficient resource utilization in container environments
CMD ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
