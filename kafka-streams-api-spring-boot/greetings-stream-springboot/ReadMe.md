# Kafka Streams API

## Set up Kafka Environment using Docker

- This should set up the Zookeeper and Kafka Broker in your local environment

```bash
$ cd workspace/
$ docker-compose up
```

### Verify the Local Kafka Environment

- Run this below command

```bash
$ docker ps
```

### Interacting with Kafka

- logging in to the Kafka container.

```bash
$ docker exec -it broker bash
```

#### Produce Messages

```bash
$ kafka-console-producer --broker-list localhost:9092 --topic greetings
```

- Publish to **greetings** topic with key and value

```bash
$ kafka-console-producer --broker-list localhost:9092 --topic greetings --property "key.separator=-" --property "parse.key=true"
```

#### Consume Messages

```bash
$ docker exec -it broker bash
```
- Command to consume messages from the Kafka topic.

```bash
$ kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings
$ kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings-output
```

- Command to consume with Key

```bash
$ kafka-console-consumer --bootstrap-server localhost:9092 --topic greetings-uppercase --from-beginning -property "key.separator= - " --property "print.key=true"
```

- Other Helpful Kafka Consumer commands

```bash
$ kafka-console-consumer --bootstrap-server localhost:9092 --topic general-orders
```

```bash
$ kafka-console-consumer --bootstrap-server localhost:9092 --topic restaurant-orders
```

```bash
$ kafka-console-consumer --bootstrap-server localhost:9092 --topic ktable-words-store-changelog --from-beginning
```

- Command to read from the Internal Aggregate topic

```bash
$ kafka-console-consumer --bootstrap-server localhost:9092 --topic aggregate-KSTREAM-AGGREGATE-STATE-STORE-0000000003-changelog --from-beginning -property "key.separator= - " --property "print.key=true"
```

### List Topics

- This  command should take care of logging in to the Kafka container.

```bash
$ docker exec -it broker bash
```

- Command to list the topics.

```bash
$ kafka-topics --bootstrap-server localhost:9092 --list
```


## KafkaStreams using SpringBoot

### How Spring Boot AutoConfiguration works for Kafka Streams?

- Adding the annotation @EnableKafkaStreams is going to invoke the **KafkaStreamsDefaultConfiguration** class
    - **KafkaStreamsAnnotationDrivenConfiguration** supplies the **KafkaStreamsConfiguration** bean
    - This class takes care of building the **StreamsBuilderFactoryBean** which is responsible for supplying the StreamsBuilder instance.
        - This **StreamsBuilderFactoryBean** class also takes care of managing the Lifecycle of the **KafkaStreams** App.

