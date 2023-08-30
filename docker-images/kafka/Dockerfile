FROM confluentinc/cp-kafka:7.1.1
USER root
RUN mkdir /opt/prometheus
RUN chmod +rx /opt/prometheus
COPY jmx-exporter-0.20.0.jar /opt/prometheus
COPY kafka-jmx-exporter.yml /opt/prometheus