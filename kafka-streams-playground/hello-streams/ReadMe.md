## Introduction
6. Module used to demo Kafka Streams

### Kafka Topics, Partitions & Replication Factor

```bash
$ docker exec -it kafka-broker-1 bash

# create kafka topics
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic hello-kafka --partitions 5 --replication-factor 3 --config segment.bytes=1000000

# describe kafka topics
$ kafka-topics.sh --bootstrap-server localhost:9092 --list
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic hello-kafka --describe

# delete kafka topics
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic hello-kafka --delete
```

## Kafka Consumer

#### To consume messages
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-kafka
```

#### To consume from beginning
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-kafka --from-beginning
```