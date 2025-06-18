#!/bin/bash

# Backend Build Script for Liquor Store App
echo "Building Liquor Store Backend..."

# Clean and build the Spring Boot application
echo "Cleaning previous build..."
./mvnw clean

echo "Building Spring Boot application..."
./mvnw package -DskipTests

echo "Backend build completed successfully!"
echo "JAR file is available in the 'target' directory"