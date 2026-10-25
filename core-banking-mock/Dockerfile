# Build from the repository root:  docker build -f core-banking-mock/Dockerfile .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl core-banking-mock -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 app
COPY --from=build /src/core-banking-mock/target/tellermesh-core-banking-mock-*.jar app.jar
USER app
EXPOSE 8090
ENV TZ=UTC
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
