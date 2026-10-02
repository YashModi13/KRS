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

# Create a dedicated system user and group for safety
RUN addgroup -S spring && adduser -S spring -G spring

# Copy jar with spring ownership
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Switch to non-root user
USER spring:spring

EXPOSE 8081
ENV PORT=8081

ENTRYPOINT ["java", "-jar", "app.jar"]
