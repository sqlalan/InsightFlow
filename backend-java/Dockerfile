# ---------------------------------------------------------------------------
# Imagem do Back-end publicada no Render.
#
# Java e Python precisam conviver no mesmo container: o AnaliseService chama o
# script de tratamento com ProcessBuilder, e um processo so enxerga o outro se
# estiverem na mesma maquina. Por isso a imagem final tem JRE + Python.
#
# Contexto de build: a RAIZ do repositorio (precisa enxergar analytics-python/).
#   docker build -f backend-java/Dockerfile -t cti-insights-api .
# ---------------------------------------------------------------------------

# --------------------------- etapa 1: compilar -----------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# baixa as dependencias antes do codigo: a camada fica em cache entre deploys
COPY backend-java/pom.xml .
RUN mvn -B -q dependency:go-offline

COPY backend-java/src ./src
RUN mvn -B -q clean package -DskipTests

# --------------------------- etapa 2: executar -----------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 python3-pip python3-venv \
    && rm -rf /var/lib/apt/lists/*

# ambiente virtual isolado para as bibliotecas de Ciencia de Dados
ENV VIRTUAL_ENV=/opt/venv
RUN python3 -m venv $VIRTUAL_ENV
ENV PATH="$VIRTUAL_ENV/bin:$PATH"

COPY analytics-python/requirements.txt /app/analytics-python/requirements.txt
RUN pip install --no-cache-dir -r /app/analytics-python/requirements.txt

COPY analytics-python /app/analytics-python
COPY --from=build /build/target/*.jar /app/app.jar

# caminhos que o application.properties le por variavel de ambiente
ENV PYTHON_BIN=python3 \
    ANALYTICS_DIR=/app/analytics-python \
    UPLOAD_DIR=/app/uploads \
    PORT=8080

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
