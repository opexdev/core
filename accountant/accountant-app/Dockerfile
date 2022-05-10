FROM openjdk:11
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]
HEALTHCHECK --interval=45s --start-period=30s --retries=5 CMD curl -f 'http://localhost:8080/actuator/health' || exit 1