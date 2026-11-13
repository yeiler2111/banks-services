# syntax=docker/dockerfile:1

# =====================================================================
# ETAPA 1 — BUILD: compila el código y genera el .jar
# Imagen pesada (JDK + Maven). NO llega a la imagen final.
# =====================================================================
FROM maven:3.9-eclipse-temurin-17 AS build

# Certificados extra (antivirus/proxy) desde el contexto "certs" (carpeta ../certs).
# Si no hay ningún .crt, el bucle no hace nada y la build sigue igual.
COPY --from=certs . /tmp/certs/
RUN for f in /tmp/certs/*.crt; do \
      [ -e "$f" ] || continue; \
      keytool -importcert -noprompt -cacerts -storepass changeit \
              -alias "$(basename "$f" .crt)" -file "$f"; \
    done

WORKDIR /workspace

# 1) Solo el pom.xml -> descarga dependencias.
#    Esta capa se cachea y solo se repite si cambia el pom.xml.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# 2) El código fuente -> compila y empaqueta (tests se ejecutan aparte).
COPY src ./src
RUN mvn -q -B -DskipTests package

# =====================================================================
# ETAPA 2 — RUNTIME: solo lo necesario para EJECUTAR el .jar
# Imagen ligera (JRE sobre Alpine), sin Maven ni código fuente.
# =====================================================================
FROM eclipse-temurin:17-jre-alpine

# Usuario sin privilegios: si alguien compromete la app, no es root.
RUN addgroup -S app && adduser -S app -G app

WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

USER app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
