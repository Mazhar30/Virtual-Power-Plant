# Stage 1: Build the application
# FROM maven:3.9.4-eclipse-temurin-21 AS build
#
# # Set the working directory in the container
# WORKDIR /app
#
# # Copy the Maven project files to the container
# COPY pom.xml .
# COPY src ./src
#
# # Build the application
# RUN mvn clean install
#
# FROM eclipse-temurin:21
#
# WORKDIR /app
#
# # Copy the built JAR file from the build stage to the runtime stage
# COPY --from=build /app/target/cc-0.0.1-SNAPSHOT.jar battery-management-service.jar
#
# # Expose the port on which the service will run
# EXPOSE 8080
#
# # Run the JAR file
# ENTRYPOINT ["java", "-jar", "battery-management-service.jar"]

FROM openjdk:17-jdk-alpine
COPY target/cc-1.0-SNAPSHOT.jar battery-management-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/battery-management-service.jar"]