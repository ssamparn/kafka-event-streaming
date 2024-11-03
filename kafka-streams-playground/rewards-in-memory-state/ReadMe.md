## Introduction
Module used to demo Kafka InMemory State Store

### Kafka Topics, Partitions & Replication Factor

```bash
$ docker exec -it kafka-broker-1 bash

$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic pos --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic loyalty --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic customer-rewards --partitions 5 --replication-factor 3 --config segment.bytes=1000000


$ kafka-topics.sh --bootstrap-server localhost:9092 --list
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic pos --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic loyalty --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic customer-rewards --describe
```

## Kafka Consumer

#### To consume messages
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic pos
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic loyalty
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic customer-rewards
```

#### To consume from beginning
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-kafka --from-beginning
```