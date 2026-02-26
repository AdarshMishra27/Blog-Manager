# Step 1: Use a lightweight Java runtime
FROM eclipse-temurin:21-jre-alpine

# Step 2: Set the working directory
WORKDIR /app

# Step 3: Copy the JAR file built by Maven
# Note: 'target/*.jar' assumes you are using Maven
COPY target/*.jar app.jar

# Step 4: Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]