FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiar pom.xml e src
COPY pom.xml .
COPY src ./src

# Build com Maven (skip tests para更快)
RUN mvn clean package -DskipTests

# Porta do Spring Boot
EXPOSE 8080

# Executar Spring Boot
ENTRYPOINT ["java", "-jar", "target/urban-nav-0.0.1-SNAPSHOT.jar"]
