# Kafka SetUp

## Build the kafka image from docker file

```bash
$ cd avro-schema-registry/workspace/docker-image/
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

#### Kafka ClusterSetup
```bash
$ cd avro-schema-registry/workspace/kafka-cluster/
$ docker compose up
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

### Create a kafka topic called `test-topic`

#### Note: we assume that directory which contains 'kafka-topics.sh' is included in the PATH
```bash
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic test-topic
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic test-topic --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic greetings --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic coffee-orders --partitions 5 --replication-factor 3 --config segment.bytes=1000000
$ kafka-topics.sh --create --bootstrap-server localhost:9092 --topic coffee-orders-sr --partitions 5 --replication-factor 3 --config segment.bytes=1000000
```

#### List all topics
```bash
$ kafka-topics.sh --bootstrap-server localhost:9092 --list
```

#### Describe a topic
```bash
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic test-topic --describe
```

#### Delete a topic
```bash
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic test-topic --delete
```

#### Create topic with partitions
```bash
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic test-topic --create --partitions 2
```

#### Alter the number of partitions in a topic
```bash
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic test-topic --alter --partitions 4
```

#### Create topic with replication factor
```bash
$ kafka-topics.sh --bootstrap-server localhost:9092 --topic test-topic --create --partitions 2 --replication-factor 3
```

## Kafka Producer

#### To produce messages
```bash
$ kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test-topic
```

#### linger.ms
```bash
$ kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test-topic --timeout 100
```

## Kafka Consumer

#### To consume messages
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test-topic
```

#### To consume from beginning
```bash
$ kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test-topic --from-beginning
```

## Produce and Consume AVRO Records

#### Produce AVRO Messages
- Logging in to the Schema Registry container.

```bash
$ docker exec -it schema-registry bash
```

- Run the **kafka-avro-console-producer** with the Schema

```bash
$ kafka-avro-console-producer --bootstrap-server kafka1:9092 --topic greetings --property value.schema='{"type": "record","name":"Greeting","fields": [{"name": "greeting","type": "string"}]}'
```

- Publish the **Greeting** message

```json
{"greeting": "Good Morning!, AVRO"}
```

```json
{"greeting": "Good Evening!, AVRO"}
```

```json
{"greeting": "Good Night!, AVRO"}
```

#### Consume AVRO Messages

- This  command should take care of logging in to the Schema Registry container.

```bash
$ docker exec -it schema-registry bash
```

- Run the **kafka-avro-console-consumer**
```bash
$ kafka-avro-console-consumer --bootstrap-server kafka1:9092 --topic greetings
$ kafka-avro-console-consumer --bootstrap-server kafka1:9092 --topic greetings --from-beginning
```
