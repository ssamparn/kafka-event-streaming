package com.kafka.streams.documentation;

import lombok.extern.slf4j.Slf4j;

/* *
 * Introduction to Kafka APIs:
 * At the core of Kafka architecture, we have the Kafka Broker. Kafka broker is where the actual data resides, and the broker is responsible for handling client requests.
 * We have two basic client APIs named Producer API and Consumer API.
 *
 * The Producer API is responsible for producing or publishing data into the Kafka topic.
 * The Consumer API is responsible for consuming events from a Kafka topic and take action on those events.
 * In addition to this, we also have 2 advanced client APIs. Kafka Connect API and the Kafka Streams API.
 *
 * Let's start with the Connect API.
 * The Connect API is further divided into 2 types. The source connector and The sink connector.
 * The source Connector is responsible for reading the data from an external data source and publish the data as events into the Kafka topic.
 * The Sink Connector is responsible for extracting the data from a Kafka topic and write it to an external data source.
 *
 * The next advanced API is the Streams API. This API basically reads the data from the Kafka topic, and there can be many different things that can be performed on the data.
 * We can apply transformations, data enrichment, branching the data into multiple data streams, aggregating the data, or joining the data from multiple Kafka topics and then writing it back to the Kafka topic.
 * */
@Slf4j
public class KafkaApiIntroduction {

}
