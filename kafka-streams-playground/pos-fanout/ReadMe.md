## Introduction
7. Module used to demo Kafka Streams Topology

### Kafka Topics, Partitions & Replication Factor

```bash
$ docker exec -it kafka-broker-1 bash

# create kafka topics
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic pos --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic shipment --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic loyalty --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic hadoop-sink --partitions 5 --replication-factor 3 --config segment.bytes=1000000

# describe kafka topics
$ kafka-topics.sh --bootstrap-server localhost:9092 --list
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic pos --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic shipment --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic loyalty --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic hadoop-sink --describe

# delete kafka topics
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic pos --delete
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic shipment --delete
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic loyalty --delete
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic hadoop-sink delete
```

## Kafka Consumer

#### To consume messages
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic pos
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic shipment
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic loyalty
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hadoop-sink
```

#### To consume from beginning
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-kafka --from-beginning
```