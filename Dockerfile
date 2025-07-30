# Stage 1: build the JAR
FROM eclipse-temurin:21 AS builder
WORKDIR /app

# Copy Maven wrapper and download deps
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and compile
COPY src src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: run the app
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built JAR from builder
COPY --from=builder /app/target/asiankitchen-backend-*.jar app.jar


# Expose the port your Spring Boot listens on
EXPOSE 8080

# Launch the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
