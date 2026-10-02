# Build Stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and src from Backend directory
COPY Backend/pom.xml ./pom.xml
COPY Backend/src ./src

# Build production JAR skipping tests
RUN mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENV PORT=8081

ENTRYPOINT ["java", "-jar", "app.jar"]
