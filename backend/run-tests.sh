#!/bin/bash
# Script pour exécuter les tests backend dans Docker avec Java 21

echo "🧪 Exécution des tests backend avec Java 21..."
docker run --rm \
  -v "$(pwd)":/app \
  -w /app \
  maven:3.9.4-eclipse-temurin-21 \
  mvn test
