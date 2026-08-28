FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY Dockerfile .
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:17.0.10_7-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/faculty-assessment-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --start-period=20s --retries=5 CMD wget -q -O /dev/null http://localhost:8080/api/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
