# Etapa 1: build com Gradle (gera o JAR)
FROM gradle:7.5-jdk17 AS builder
WORKDIR /home/gradle/project
COPY . .

# Build e “normaliza” o artefato para um caminho fixo, evitando problemas com wildcard
RUN gradle clean build -x test --no-daemon \
 && set -eux; \
    JAR="$(ls -1 build/libs/*.jar | grep -v -- '-plain\.jar$' | head -n 1)"; \
    cp "$JAR" /tmp/app.jar

# Etapa 2: runtime baseado no seu setup (Ubuntu + LABEL)
FROM ubuntu:latest
LABEL authors="andrade"

# Instala apenas o runtime Java (JRE) necessário para rodar o .jar
RUN apt-get update \
 && apt-get install -y --no-install-recommends openjdk-17-jre-headless ca-certificates \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /tmp/app.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
