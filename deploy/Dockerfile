FROM maven:3.9-eclipse-temurin-21 AS build

ARG MODULE

WORKDIR /src

COPY . .

RUN mvn -q -DskipTests install

RUN mkdir -p /out/lib \
    && mvn -q -pl "${MODULE}" dependency:copy-dependencies \
         -DincludeScope=runtime \
         -DoutputDirectory=/out/lib \
    && APP_JAR="$(find "${MODULE}/target" \
         -maxdepth 1 \
         -type f \
         -name '*.jar' \
         ! -name 'original-*' \
         ! -name '*-sources.jar' \
         ! -name '*-javadoc.jar' \
         | head -n 1)" \
    && test -n "$APP_JAR" \
    && cp "$APP_JAR" /out/app.jar


FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /out/app.jar /app/app.jar
COPY --from=build /out/lib /app/lib

RUN mkdir -p \
    /app/metric \
    /app/plugins/work \
    /app/plugins/bundles \
    /app/plugins/filter-bundles