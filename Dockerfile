# 1️⃣ Build stage
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy build files first for better cache
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd
COPY .mvn .mvn
COPY pom.xml pom.xml

# Download dependencies (will be cached)
RUN ./mvnw -q -DskipTests dependency:go-offline

# Copy source and build
COPY src src
RUN ./mvnw -q -DskipTests package

# 2️⃣ Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar into runtime image
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
