FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY target/E_Commerce_Backend_System-0.0.1-SNAPSHOT.jar /app/E_Commerce_Backend_System-0.0.1-SNAPSHOT.jar

EXPOSE 8080

# Run the jar file when the container starts
ENTRYPOINT ["java", "-jar", "E_Commerce_Backend_System-0.0.1-SNAPSHOT.jar"]