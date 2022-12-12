## Interacting with Kafka

### Produce and Consume Schemaless Messages

#### Produce Messages
- Logging in to the Kafka container.
```
docker exec -it broker bash
```

- Command to produce messages in to the Kafka topic.
```
kafka-console-producer --broker-list localhost:9092 --topic test-topic
```

#### Consume Messages

- Logging in to the Kafka container.
```
docker exec -it broker bash
```

- Command to produce messages in to the Kafka topic.
```
kafka-console-consumer --bootstrap-server localhost:9092 --topic test-topic
```


### Produce and Consume AVRO Records

#### Produce AVRO Messages
- Logging in to the Schema Registry container.

```
docker exec -it schema-registry bash
```

- Run the **kafka-avro-console-producer** with the Schema

```
kafka-avro-console-producer --broker-list broker:29092 --topic greetings --property value.schema='{"type": "record","name":"Greeting","fields": [{"name": "greeting","type": "string"}]}'
```

- Publish the **Greeting** message

```
{"greeting": "Good Morning!, AVRO"}
```

```
{"greeting": "Good Evening!, AVRO"}
```

```
{"greeting": "Good Night!, AVRO"}
```

#### Consume AVRO Messages

- This  command should take care of logging in to the Schema Registry container.

```
docker exec -it schema-registry bash
```

- Run the kafka-avro-console-consumer

```
kafka-avro-console-consumer --bootstrap-server broker:29092 --topic greetings --from-beginning
```


