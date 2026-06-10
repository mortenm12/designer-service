# Stage 1: Build
FROM eclipse-temurin:25-jdk-noble AS build

WORKDIR /workspace

COPY gradlew .
COPY gradle/ gradle/
COPY settings.gradle.kts .
COPY build.gradle.kts .

RUN ./gradlew dependencies --no-daemon --quiet

COPY src/ src/

RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-noble AS runtime

RUN groupadd --system --gid 1001 appgroup && \
    useradd --system --uid 1001 --gid appgroup --no-create-home appuser

WORKDIR /app

COPY --from=build --chown=appuser:appgroup /workspace/build/libs/app.jar app.jar

USER appuser

ENV JAVA_OPTS="-XX:+UseZGC -XX:+ZGenerational -Xmx512m -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
