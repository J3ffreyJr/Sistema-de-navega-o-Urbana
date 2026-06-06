# Fase 1: Build com Maven
FROM maven:3.9.6-eclipse-temurin-11 AS build
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src
RUN mvn clean package -DskipTests

# Fase 2: Runtime leve
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app

# Copiar o JAR compilado
COPY --from=build /app/target/urban-nav-0.0.1-SNAPSHOT.jar app.jar

# Copiar o ficheiro OSM reduzido
COPY src/main/resources/data/maputo-centro.osm src/main/resources/data/maputo-centro.osm

# Porta que o Render usa
EXPOSE 10000

# Arrancar com memória limitada para caber no plano gratuito
ENTRYPOINT ["java", "-Xmx400m", "-Xms128m", "-jar", "app.jar"]