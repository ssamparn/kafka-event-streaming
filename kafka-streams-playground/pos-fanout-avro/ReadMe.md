## Introduction
Module used to demo Kafka Streams Topology with Avro

### Kafka Topics, Partitions & Replication Factor

```bash
$ docker exec -it kafka-broker-1 bash

$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic pos-avro --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic shipment-avro --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic loyalty-avro --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic hadoop-sink-avro --partitions 5 --replication-factor 3 --config segment.bytes=1000000


$ kafka-topics.sh --bootstrap-server localhost:9092 --list
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic pos-avro --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic shipment-avro --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic loyalty-avro --describe
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic hadoop-sink-avro --describe
```

#### Consume AVRO Messages

- This  command should take care of logging in to the Schema Registry container.

```bash
$ docker exec -it schema-registry bash
```

- Run the **kafka-avro-console-consumer**
```bash
$ kafka-avro-console-consumer --bootstrap-server kafka1:9092 --topic avro-pos
$ kafka-avro-console-consumer --bootstrap-server kafka1:9092 --topic shipment-avro
$ kafka-avro-console-consumer --bootstrap-server kafka1:9092 --topic loyalty-avro
$ kafka-avro-console-consumer --bootstrap-server kafka1:9092 --topic hadoop-sink-avro
```