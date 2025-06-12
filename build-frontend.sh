#!/bin/bash

echo "🔧 Building frontend..."
cd src/main/frontend
npm run build

echo "📁 Copying built files to Spring Boot static folder..."
cp -r build/* ../resources/static/

echo "✅ Frontend build complete! Your changes are ready."
echo "🚀 Now restart your Spring Boot app to see the changes."