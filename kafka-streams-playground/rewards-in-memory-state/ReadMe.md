## Introduction
10. Module used to demo Kafka InMemory State Store

### Kafka Topics, Partitions & Replication Factor

```bash
$ docker exec -it kafka-broker-1 bash

# create kafka topics
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic pos --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic loyalty --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic customer-rewards --partitions 5 --replication-factor 3 --config segment.bytes=1000000

# describe kafka topics
$ kafka-topics.sh --bootstrap-server localhost:9092 --list
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic pos --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic loyalty --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic customer-rewards --describe

# delete kafka topics
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic pos --delete
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic loyalty --delete
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic customer-rewards --delete
```

## Kafka Consumer

#### To produce messages
> Run `pos-simulator` module

#### To consume messages
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic pos
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic loyalty
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic customer-rewards
```