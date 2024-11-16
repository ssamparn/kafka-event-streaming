## Introduction
17. Module used to demo Kafka real time stream aggregation using KTable aggregate()

### Kafka Topics, Partitions & Replication Factor

```bash
$ docker exec -it kafka-broker-1 bash

# create kafka topics
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic simple-invoice --partitions 5 --replication-factor 3 --config segment.bytes=1000000

# describe kafka topics
$ kafka-topics.sh --bootstrap-server localhost:9092 --list
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic simple-invoice --describe

# delete kafka topics
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic simple-invoice --delete
```

## Kafka Producer

#### To produce messages
```bash
$ kafka-console-producer.sh --bootstrap-server localhost:9092 --topic simple-invoice --property parse.key=true --property key.separator=":"
```

## Kafka Consumer

#### To consume messages
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic simple-invoice
```