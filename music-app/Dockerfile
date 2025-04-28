FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml file
COPY pom.xml .

# Download all required dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built artifact from the build stage
COPY --from=build /app/target/music-app-*.jar app.jar

# Set the command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 