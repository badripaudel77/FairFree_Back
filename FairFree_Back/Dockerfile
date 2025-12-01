# Development Dockerfile for Spring Boot with Maven
FROM maven:3.9-eclipse-temurin-25-alpine

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Expose ports
# 8080 - Application
# 5005 - Debug port (Java Debug Wire Protocol)
EXPOSE 8080 5005

# Run Maven with spring-boot plugin and debug mode enabled
# Uses spring-boot:run for development with hot reload support
ENTRYPOINT ["mvn", "spring-boot:run", "-Dspring-boot.run.jvmArguments='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005'"]
