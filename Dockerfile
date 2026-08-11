# syntax=docker/dockerfile:1

# ---------- Build stage ----------
# Uses the Gradle wrapper committed in the repo so the exact Grails/Gradle
# versions are honoured. Produces an executable Grails WAR (embedded Tomcat).
FROM eclipse-temurin:17-jdk AS build
WORKDIR /src

# Copy the whole backend project (see .dockerignore for what is excluded).
COPY . .

# Build the WAR. Tests are skipped for the image build (they run against H2).
RUN chmod +x gradlew && \
    ./gradlew assemble --no-daemon -x test && \
    cp "$(ls build/libs/*.war | grep -v -- '-plain' | head -n1)" /app.war

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# curl is used by the docker-compose healthcheck.
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build /app.war app.war

# Grails uses the production environment when the packaged WAR is run;
# set it explicitly for clarity. DB_URL / DB_USER / DB_PASSWORD are injected
# at runtime by docker-compose (see .env).
ENV GRAILS_ENV=production \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]
