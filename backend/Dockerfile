# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/govt-job-aggregator-1.0.0.jar app.jar
RUN mkdir -p /app/data && chmod 777 /app/data

# Render provides the PORT environment variable
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
