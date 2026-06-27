FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
ARG GIT_COMMIT=unknown
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN printf 'git.commit.id.abbrev=%s\n' "${GIT_COMMIT}" > src/main/resources/git.properties \
    && mvn clean package -DskipTests -Dmaven.gitcommitid.skip=true

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl util-linux \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 10001 infratrack \
    && useradd --system --uid 10001 --gid infratrack --home-dir /app --shell /usr/sbin/nologin infratrack \
    && mkdir -p /app/data/operational-documents \
    && chown -R infratrack:infratrack /app \
    && printf '%s\n' \
        '#!/bin/sh' \
        'set -e' \
        'mkdir -p /app/data/operational-documents' \
        'chown -R infratrack:infratrack /app/data' \
        'exec runuser -u infratrack -- "$@"' \
        > /docker-entrypoint.sh \
    && chmod +x /docker-entrypoint.sh
WORKDIR /app
COPY --from=build --chown=infratrack:infratrack /app/target/infratrack-backend-*.jar app.jar
EXPOSE 4000
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://127.0.0.1:4000/actuator/health || exit 1
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["java", "-jar", "app.jar"]
