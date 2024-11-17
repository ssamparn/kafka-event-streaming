## Kafka Streams API

### Interacting with Kafka

```bash
$ docker exec -it kafka-broker-1 bash
```

## Kafka Producer

#### To Produce Messages
```bash
$ kafka-console-producer.sh --bootstrap-server localhost:9092 --topic greetings
$ kafka-console-producer.sh --bootstrap-server localhost:9092 --topic greetings --property "key.separator=-" --property "parse.key=true"
$ kafka-console-producer.sh --bootstrap-server localhost:9092 --topic greetings-spanish --property "key.separator=-" --property "parse.key=true"
```

## Kafka Consumer

#### To Consume Messages

- Command to consume messages from the Kafka topic.

```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic greetings-uppercase
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic greetings-uppercase --from-beginning -property "key.separator= - " --property "print.key=true"
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