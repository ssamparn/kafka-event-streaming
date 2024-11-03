## Introduction
Module used to demo implementations of transactions in kafka producers

### ## Kafka Topics, Partitions & Replication Factor

```bash
$ docker exec -it kafka-broker-1 bash

$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic hello-kafka-1 --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic hello-kafka-2 --partitions 5 --replication-factor 3 --config segment.bytes=1000000

$ kafka-configs.sh --bootstrap-server localhost:9092 --alter --entity-type topics --entity-name hello-kafka-1 --add-config min.insync.replicas=2
$ kafka-configs.sh --bootstrap-server localhost:9092 --alter --entity-type topics --entity-name hello-kafka-2 --add-config min.insync.replicas=2

$ kafka-topics.sh --bootstrap-server localhost:9092 --list
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic hello-kafka-1 --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic hello-kafka-2 --describe
```

## Kafka Consumer

#### To consume messages
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-kafka-1
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-kafka-2
```

#### To consume from beginning
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-kafka-1 --from-beginning
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-kafka-2 --from-beginning
```