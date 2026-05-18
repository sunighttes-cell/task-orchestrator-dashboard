# Use official Java 21 image
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy everything into container
COPY . .

# Make mvnw executable
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port (Render uses dynamic port, but this is fine)
EXPOSE 8080

# Start the app
CMD ["sh", "-c", "java -jar target/*.jar"]