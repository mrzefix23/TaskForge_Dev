# Build stage using Maven
FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app
COPY src ./src
COPY pom.xml .
RUN mvn clean package -DskipTests

# Production stage using OpenJDK
FROM eclipse-temurin:21-jdk AS production
WORKDIR /app
COPY --from=build /app/target/taskforge-0.1.0.jar app.jar

# Expose port 8080 and set the entry point
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]