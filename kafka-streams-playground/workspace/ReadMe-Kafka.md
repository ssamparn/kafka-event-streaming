# Kafka SetUp

## Build the kafka image from docker file

```bash
$ cd kafka-streams-playground/workspace/docker-image/
$ docker build --tag=<repository-name>/<imagae-name> .
$ docker build --tag=ssamantr/kafka-broker .
```

#### Push the kafka image to docker hub
```bash
$ docker push <repository-name>/<imagae-name>
$ docker push ssamantr/kafka-broker
```

#### Run the kafka image
```bash
$ docker run <repository-name>/<imagae-name>
$ docker run ssamantr/kafka-broker
```

> Alternative: You can also run docker image from docker compose

#### Kafka Single Node Setup
```bash
$ cd kafka-streams-playground/workspace/kafka-single-node/
$ docker compose up
```

#### To get access to running single node kafka container
```bash
$ docker exec -it <container-name> bash
$ docker exec -it kafka-broker bash
```

#### Kafka ClusterSetup
```bash
$ cd kafka-streams-playground/workspace/kafka-cluster/
$ docker compose up
```
```bash
$ docker ps
```

#### To get access to running one of the node of kafka clusters
```bash
$ docker exec -it <container-name> bash
$ docker exec -it kafka-broker-1 bash
$ docker exec -it kafka-broker-2 bash
$ docker exec -it kafka-broker-3 bash
```

## Kafka Topics, Partitions & Replication Factor

### Create a kafka topic called `hello-kafka`

#### Note: we assume that directory which contains 'kafka-topics.sh' is included in the PATH
```bash
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic hello-kafka
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic hello-kafka --partitions 5 --replication-factor 3 --config segment.bytes=1000000

$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic test-topic --partitions 5 --replication-factor 3 --config segment.bytes=1000000
```

#### List all topics
```bash
$ kafka-topics.sh --bootstrap-server localhost:9092 --list
```

#### Describe a topic
```bash
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic hello-kafka --describe
```

#### Delete a topic
```bash
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic hello-kafka --delete
```

#### Create topic with partitons
```bash
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic hello-kafka --create --partitions 2
```

#### Alter the number of partitions in a topic
```bash
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic hello-kafka --alter --partitions 4
```

#### Create topic with replication factor
```bash
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic hello-kafka --create --partitions 2 --replication-factor 3
```

## Kafka Producer

#### To produce messages
```bash
$ kafka-console-producer.sh --bootstrap-server localhost:9092 --topic hello-kafka
```

#### linger.ms
```bash
$ kafka-console-producer.sh --bootstrap-server localhost:9092 --topic hello-kafka --timeout 100
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

## Print Offset

#### To print offset, time etc
```bash
$ kafka-console-consumer.sh \
--bootstrap-server localhost:9092 \
--topic hello-kafka \
--property print.offset=true \
--property print.timestamp=true
```

## Consumer Group

#### Create console producer
```bash
$ kafka-console-producer.sh \
--bootstrap-server localhost:9092 \
--topic hello-kafka \
--property key.separator=: \
--property parse.key=true
```

#### Create console consumer with a consumer group
```bash
$ kafka-console-consumer.sh \
--bootstrap-server localhost:9092 \
--topic hello-kafka \
--property print.offset=true \
--property print.key=true \
--group <group-name>
```

#### List all the consumer groups
```bash
$ kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```

#### Describe a consumer group
```bash
$ kafka-consumer-groups.sh \
--bootstrap-server localhost:9092 \
--group <group-name> \
--describe
```
