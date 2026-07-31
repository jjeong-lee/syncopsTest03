FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY pom.xml mvnw ./
COPY src ./src
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:17.0.13_11-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/faculty-performance-common-0.1.0.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
