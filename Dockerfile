FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY target/SchoolApplicationSpring-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]