FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build/spi
COPY spi/pom.xml .
RUN mvn dependency:go-offline
COPY spi/src ./src
RUN mvn clean install

FROM quay.io/keycloak/keycloak:26.1
COPY --from=builder /build/spi/target/*.jar /opt/keycloak/providers/
COPY realms/ /opt/keycloak/data/import/