# Build e execução do backend Quarkus (modo JVM) sem precisar de Java ou Maven na máquina.
# Usado pelo serviço "backend" do docker-compose.yaml da raiz.

# ---- Build ----
FROM maven:3.9.14-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
# Cache do ~/.m2 entre builds (BuildKit): só baixa de novo o que mudou no pom.
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests

# ---- Execução ----
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build --chown=app:app /build/target/quarkus-app/lib/ ./lib/
COPY --from=build --chown=app:app /build/target/quarkus-app/*.jar ./
COPY --from=build --chown=app:app /build/target/quarkus-app/app/ ./app/
COPY --from=build --chown=app:app /build/target/quarkus-app/quarkus/ ./quarkus/
USER app
EXPOSE 8081
# Heap limitado a 75% da memória do contêiner (o limite fica no docker-compose.yaml).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Djava.util.logging.manager=org.jboss.logmanager.LogManager", "-jar", "quarkus-run.jar"]
