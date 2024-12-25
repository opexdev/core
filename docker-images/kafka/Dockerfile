FROM confluentinc/cp-kafka:7.8.0
USER root
RUN mkdir /opt/prometheus
RUN chmod +rx /opt/prometheus
COPY jmx-exporter-0.16.1.jar /opt/prometheus/jmx-exporter.jar
COPY kafka-jmx-exporter.yml /opt/prometheus