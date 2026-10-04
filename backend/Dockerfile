# ============ ÉTAPE 1 : BUILD ============
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copier pom.xml et télécharger les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source et builder
COPY src ./src
RUN mvn clean package -DskipTests

# ============ ÉTAPE 2 : RUNTIME ============
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copier le .jar depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port Spring Boot
EXPOSE 8080

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
