# Use a lightweight Java 21 runtime
FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

# Create a volume for temporary files (optional but good practice for Spring Boot)
VOLUME /tmp

# We will pass the JAR file name as an argument
ARG JAR_FILE=target/*.jar

# Copy the JAR file into the image and rename it to app.jar
COPY ${JAR_FILE} app.jar

# Expose port 8080
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]