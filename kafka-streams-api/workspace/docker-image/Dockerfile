FROM eclipse-temurin:21-jre-noble

ADD https://downloads.apache.org/kafka/3.8.0/kafka_2.13-3.8.0.tgz kafka.tgz

RUN tar -xvzf kafka.tgz

RUN rm kafka.tgz

RUN mv /kafka_2.13-3.8.0 /kafka

ENV PATH=${PATH}:/kafka/bin

WORKDIR kafka-events

ADD runner.sh runner.sh

CMD [ "sh", "runner.sh" ]