## Introduction
Module used to demo Kafka Consumer

### Kafka Topics, Partitions & Replication Factor

```bash
$ docker exec -it kafka-broker-1 bash

$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic pos --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic valid-pos --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic invalid-pos --partitions 5 --replication-factor 3 --config segment.bytes=1000000

$ kafka-topics.sh --bootstrap-server localhost:9092 --list
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic pos --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic valid-pos --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic invalid-pos --describe
```

## Kafka Consumer

#### To consume messages
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic pos
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic valid-pos
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic invalid-pos
```

#### To consume from beginning
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic pos --from-beginning
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic valid-pos --from-beginning
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic invalid-pos --from-beginning
```