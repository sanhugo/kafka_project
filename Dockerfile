# syntax=docker/dockerfile:1

# ---- Build: the jars of all modules are built right in Docker ----
# This stage does not depend on MODULE, so when several services are built
# BuildKit runs it once and reuses the result.
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /src
COPY . .

# gradlew from a Windows working copy may have CRLF line endings, which break it on Linux
RUN sed -i 's/\r$//' gradlew

# The gradle cache (distribution and dependencies) is kept between builds
RUN --mount=type=cache,target=/root/.gradle,sharing=locked \
    ./gradlew bootJar --no-daemon --console=plain

# ---- Run ----
FROM eclipse-temurin:21-jre-alpine

# Gradle module name: order_producer, order_processor or dlq_handler
ARG MODULE

WORKDIR /app
COPY --from=build /src/${MODULE}/build/libs/${MODULE}-*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
