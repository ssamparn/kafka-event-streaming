## Introduction
8. Module used to demo Kafka Avro Serialization and Deserialization

### Kafka Topics, Partitions & Replication Factor

```bash
$ docker exec -it kafka-broker-1 bash

# create kafka topics
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic pos-avro --partitions 5 --replication-factor 3 --config segment.bytes=1000000

# describe kafka topics
$ kafka-topics.sh --bootstrap-server localhost:9092 --list
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic pos-avro --describe

# delete kafka topics
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic pos-avro --delete
```

## Kafka Consumer

#### To consume messages
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic pos-avro
```

#### To consume from beginning
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic pos-avro --from-beginning
```

#### Consume AVRO Messages

- This  command should take care of logging in to the Schema Registry container.

```bash
$ docker exec -it schema-registry bash
```

- Run the **kafka-avro-console-consumer**
```bash
$ kafka-avro-console-consumer --bootstrap-server kafka1:9092 --topic pos-avro
$ kafka-avro-console-consumer --bootstrap-server kafka1:9092 --topic pos-avro --from-beginning
```