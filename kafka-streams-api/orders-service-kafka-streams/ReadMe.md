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

### List Topics

- Command to list the topics.

```bash
$ kafka-topics --bootstrap-server localhost:9092 --list
```

#### Produce Messages

- Command to produce messages to the Kafka topic.

```bash
$ kafka-console-producer --broker-list localhost:9092 --topic orders
```

#### Consume Messages

- Command to consume messages from the Kafka topic.

```bash
$ kafka-console-consumer --bootstrap-server localhost:9092 --topic general-orders
```

```bash
$ kafka-console-consumer --bootstrap-server localhost:9092 --topic restaurant-orders
```

- Command to read from the internal katble topic.

```bash
$ kafka-console-consumer --bootstrap-server localhost:9092 --topic words-ktable-app-words-store-changelog --from-beginning
```

- Command to read from the Internal Aggregate topic

```bash
$ kafka-console-consumer --bootstrap-server localhost:9092 --topic aggregate-KSTREAM-AGGREGATE-STATE-STORE-0000000003-changelog --from-beginning -property "key.separator= - " --property "print.key=true"
```