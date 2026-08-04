FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17.0.10_7-jre-alpine
WORKDIR /app
RUN addgroup -S fpe && adduser -S -G fpe -u 10001 fpe
COPY --from=build /workspace/target/faculty-performance-common-0.1.0.jar /app/app.jar
RUN chown -R fpe:fpe /app
EXPOSE 8080
USER fpe
HEALTHCHECK --interval=10s --timeout=3s --retries=12 CMD wget -qO- http://localhost:8080/api/health >/dev/null || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
