## Introduction
4. Module used to demo Kafka Json Serialization and Deserialization

### Kafka Topics, Partitions & Replication Factor

```bash
$ docker exec -it kafka-broker-1 bash

# create kafka topics
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic pos --partitions 5 --replication-factor 3 --config segment.bytes=1000000

# describe kafka topics
$ kafka-topics.sh --bootstrap-server localhost:9092 --list
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic pos --describe

# delete kafka topics
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic pos --delete
```

## Kafka Consumer

#### To consume messages
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic pos
```

#### To consume from beginning
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic pos --from-beginning
```