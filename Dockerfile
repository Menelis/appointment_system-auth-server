# Use a base image with Java 25 or the Java version your Spring Boot 3 app uses)
FROM amazoncorretto:25.0.1-alpine3.22

# Set the working directory inside the container
WORKDIR /app

# Copy the Spring Boot JAR file into the container
COPY target/*.jar app.jar

# Expose port for service
EXPOSE 8080
# Expose port for Grpc
EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
