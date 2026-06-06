# Fase 1: Build com Maven
FROM maven:3.9.6-eclipse-temurin-11 AS build

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src

# Apagar possível ponteiro Git LFS e baixar o ficheiro OSM real
RUN rm -f src/main/resources/data/maputo-centro.osm && \
    mkdir -p src/main/resources/data && \
    curl -L -o src/main/resources/data/maputo-centro.osm "COLOQUE_AQUI_O_LINK_DIRETO_DO_OSM"

RUN mvn clean package -DskipTests


# Fase 2: Runtime leve
FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

COPY --from=build /app/target/urban-nav-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-Xmx400m", "-Xms128m", "-jar", "app.jar"]