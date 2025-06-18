#!/bin/bash

# Backend Development Server Script
echo "Starting Liquor Store Backend Development Server..."

# Start the Spring Boot application in development mode
echo "Starting Spring Boot application on http://localhost:8080"
./mvnw spring-boot:run -Dspring-boot.run.profiles=local