FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY server/pom.xml server/pom.xml
COPY server/.mvn server/.mvn
COPY server/mvnw server/mvnw
RUN chmod +x server/mvnw
WORKDIR /app/server
RUN ./mvnw -q -DskipTests dependency:go-offline
COPY server/src server/src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /app/server/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
