## Introduction
Module used to demo Kafka Producer

### Kafka Topics, Partitions & Replication Factor

```bash
$ docker exec -it kafka-broker-1 bash

$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic hello-kafka --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic sync-hello-kafka --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic callback-hello-kafka --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic partitioned-producer --partitions 2 --replication-factor 3 --config segment.bytes=1000000


$ kafka-topics.sh --bootstrap-server localhost:9092 --list
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic hello-kafka --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic sync-hello-kafka --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic callback-hello-kafka --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic partitioned-producer --describe
```

## Kafka Consumer

#### To consume messages
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-kafka
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic sync-hello-kafka
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic callback-hello-kafka
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic partitioned-producer
```

#### To consume from beginning
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic hello-kafka --from-beginning
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic sync-hello-kafka --from-beginning
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic callback-hello-kafka --from-beginning
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic partitioned-producer --from-beginning
```