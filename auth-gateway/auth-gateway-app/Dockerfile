FROM eclipse-temurin:21-jdk
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
HEALTHCHECK --interval=45s --timeout=30s --start-period=60s --retries=5 CMD curl -sf 'http://localhost:8080/actuator/health' >/dev/null || exit 1