# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies first to optimize caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the project
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create a lightweight production runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the generated JAR file from the build stage
COPY --from=build /app/target/expense-tracker-*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
