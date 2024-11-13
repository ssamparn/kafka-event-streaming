## Introduction
Module used to demo Kafka real time stream aggregation using KTable aggregate()

### Kafka Topics, Partitions & Replication Factor

```bash
$ docker exec -it kafka-broker-1 bash

$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic employees --partitions 5 --replication-factor 3 --config segment.bytes=1000000

$ kafka-topics.sh --bootstrap-server localhost:9092 --list
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic pos --describe
```

## Kafka Producer

#### To produce messages
```bash
$ kafka-console-producer.sh --bootstrap-server localhost:9092 --topic employees --property parse.key=true --property key.separator=":"
```

## Kafka Consumer

#### To consume messages
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic employees
```