package com.spring.event.streaming.producer.serializationwithschemaregistry;

/* *
 * Avro Serializer and Schema Registry:
 *
 * We need a serializer for our messages and Kafka Producer API offers some basic serializer classes such as StringSerializer and IntegerSerializer.
 * These classes are not enough for real-life use cases, and you may need to implement some custom serializer class for yourself.
 * However, a serializer class is a standard component which you can reuse instead of creating one for yourself.
 * However, JSON serializer is heavy on the network because it embeds a lot of schema information in the message itself.
 * An alternative to this problem is Avro serializer. The Avro serialization is designed explicitly for Big data systems.
 * Avro doesn't embed schema information in the message record and hence turns out to be more compact and efficient.
 * But you need an Avro Serializer and Deserializer class to use Avro serialization.
 * Well, that's a difficult job, but we have good news.
 * We already have an open source version of Avro serializer and deserializer offered by Confluent.io.
 * The confluent Avro serializer and deserializer are part of Confluent Schema Registry project, and you can use serializer along with the Confluent schema registry.
 * Well, you might be wondering, what is the Schema Registry?
 * A typical Kafka message would be something like a data-record from a database table that is represented as a Java object and serialized using your favorite serializer
 * before it can be transmitted over the network. However, each record would have a schema which will evolve over time as the business grows.
 * When this happens, I mean when your schema evolves, you might end up into multiple versions of a schema for the same event flowing into your system.
 * How do you deal with this problem? Handling multiple version of the schema is a fairly complex thing.
 * You must be able to version your schemas, implement compatibility checks between the versions, translate newer versions into older versions to allow backward compatibility.
 * The point is straightforward. Supporting multiple versions of a schema for the same object is a bit of complex implementation that must be taken care transparently by serializer and deserializer.
 * The application logic should be unaware of the internal versions. How to do it?
 * The serializer/deserializer can achieve all this by implementing a common location called schema registry, where they would maintain all compatible versions of a schema for each event type.
 * Your serializer would publish schemas to the schema registry. The schema registry would take care of all the compatibility check and versioning activity.
 * Since the schema registry is a common location, it can be accessed by the deserializer to learn how to deserialize the events back to a Java Object.
 * Implementing all this could be a nightmare, and hence you can resolve to an open source or a commercially available schema registry implementation such as Confluent Schema registry.
 * So, everything is already there. An Avro serializer, A deserializer, and the schema registry.
 * */
public class SerializationAndSchemaRegistry {

}
