<<<<<<< HEAD
﻿# ---------- builder ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- runtime ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/target/catalog-service-*.jar app.jar
EXPOSE 8081
=======
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/catalog-service-1.0.0.jar app.jar
EXPOSE 8082
>>>>>>> 668c9acc40df4baace1d588ee8a747e9f8a21abc
ENTRYPOINT ["java", "-jar", "app.jar"]
