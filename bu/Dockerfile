FROM openjdk:17-jdk-alpine
COPY target/bu-1.0-SNAPSHOT.jar battery-update-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/battery-update-service.jar"]