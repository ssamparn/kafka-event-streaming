## Introduction
Module used to demo Kafka Real Time Aggregation using KTable count.

### Kafka Topics, Partitions & Replication Factor

```bash
$ docker exec -it kafka-broker-1 bash

$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic person-age --partitions 5 --replication-factor 3 --config segment.bytes=1000000

$ kafka-topics.sh --bootstrap-server localhost:9092 --list
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic person-age --describe
```

## Kafka Producer

#### To produce messages
```bash
$ kafka-console-producer.sh --bootstrap-server localhost:9092 --topic person-age --property parse.key=true --property key.separator=":"
```

## Kafka Consumer

#### To consume messages
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic person-age
```

#### To consume from beginning
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic person-age --from-beginning
```