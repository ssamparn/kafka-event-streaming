## Interacting with Kafka

### Produce and Consume Schemaless Messages

#### Produce Messages

- Log in to the Kafka server running as docker container.

```bash
$ docker exec -it <container-name> bash
$ docker exec -it broker bash
```

- Command to produce messages in to the Kafka topic.
```bash
$ kafka-console-producer --broker-list localhost:9092 --topic test-topic
```

#### Consume Messages

- Logging in to the Kafka container.

```bash
$ docker exec -it broker bash
```

- Command to consume messages in to the Kafka topic.

```bash
$ kafka-console-consumer --bootstrap-server localhost:9092 --topic test-topic
```

### Produce and Consume AVRO Records

#### Produce AVRO Messages
- Logging in to the Schema Registry container.

```bash
$ docker exec -it schema-registry bash
```

- Run the **kafka-avro-console-producer** with the Schema

```bash
$ kafka-avro-console-producer --broker-list broker:29092 --topic greetings --property value.schema='{"type": "record","name":"Greeting","fields": [{"name": "greeting","type": "string"}]}'
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

- Run the kafka-avro-console-consumer

```bash
$ kafka-avro-console-consumer --bootstrap-server broker:29092 --topic greetings --from-beginning
```


