FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -q -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN useradd -r -u 10001 springuser

COPY --from=builder /app/target/server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

USER springuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
