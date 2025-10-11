FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY target/backend_blashape-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/uploads
ENTRYPOINT ["java", "-jar", "app.jar"]