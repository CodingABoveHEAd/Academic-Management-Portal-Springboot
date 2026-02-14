# 1️⃣ Build stage
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and config first (better layer caching)
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY .mvn .mvn
COPY pom.xml pom.xml

# ✅ FIX: Give execute permission to Maven wrapper (Linux requirement)
RUN chmod +x mvnw

# Download dependencies (cached layer)
RUN ./mvnw -q -DskipTests dependency:go-offline

# Copy source code
COPY src src

# Build application
RUN ./mvnw -q -DskipTests package


# 2️⃣ Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy only the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
