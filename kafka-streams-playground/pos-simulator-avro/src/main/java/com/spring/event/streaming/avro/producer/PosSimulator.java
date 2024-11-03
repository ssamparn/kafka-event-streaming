package com.spring.event.streaming.avro.producer;

import com.spring.event.streaming.avro.generated.PosInvoice;
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/* *
 * Problem Statement:
 *  We are going to create a point of sale simulator. The POS simulator is a producer that generates an infinite number of random
 * but realistic invoices and sends it to the Kafka broker. The simulator takes 3 arguments from the command line.
 *   1. Topic name - Which topic do you want the producer to send all the invoices.
 *   2. Number of Producer threads - How many parallel threads do you want to create for this application.
 *   3. Producer speed - The number of milliseconds that each thread will wait between two invoices. So if you are creating 10 threads and giving a 100 milliseconds sleep time.
 * That means, each thread will send 10 messages in one second, and since you have 10 threads, you will generate 100 messages per second.
 *
 * However, this application would not generate random text string but will generate JSON formatted realistic invoices.
 * Earlier we always sent plain string messages. But now, we have an Invoice which is a reasonably complex Java Object.
 * The invoice object has got several fields, and they are of different data types. Some are String, but others are of Integer, number and Long types.
 * The DeliveryAddress field is a more sophisticated type which in itself is an object. The InvoiceLineItems is an array of objects.
 * So, basically an invoice is not a plain string, but it is a complex document with a predefined structure.
 * You are supposed to serialize this complex PosInvoice Java Object before you can transmit it over the network.
 * You also need to be sure that the serialized Invoice, once received at the consumer, can be correctly de-serialized back into a PosInvoice Java Object.
 * This is why a Kafka producer needs a serializer configuration. We have used StringSerializer in earlier examples, but this time, the StringSerializer does not fit our requirement.
 * We need a better alternative. There are two popular alternatives in the Kafka world.
 *    1. JSON Serializer and a Deserializer.
 *    2. Avro Serializer and a Deserializer.
 * The JSON Serializer is easy to use because JSON serialized objects are represented as strings, and that makes them a convenient option.
 * You can easily cast them to a String and print it on the console or in your logs. The simplicity of JSON makes debugging your data issues quite simple.
 * Hence, they are commonly used and supported by many data integration tools. However, JSON serialized messages are large in size.
 * The JSON format includes field names with each data element. These field names may increase the size of your serialized messages by 2X or more,
 * and ultimately, it causes more delays at the network layer to transmit these messages.
 * The alternative is the Avro Serializer. The Avro is a binary format, and the serialization is much more compact.
 * So, if you are using Avro serialization, your messages will be shorter over the network, giving you a more substantial network bandwidth.
 * But in this example, we want to use JSON Serializer. You do not need to implement a JSON serializer yourself because it is a standard thing,
 * and a JsonSerializer class is included in this project.
 * You are also free to use it in any other application wherever you want to serialize your messages as JSON.
 * */
@Slf4j
public class PosSimulator {

    private final static String applicationID = "PosSimulator";
    private final static String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    public final static String schemaRegistryServers = "http://localhost:8081";

    public static void main(String[] args) {
        String topicName = "pos-avro";
        int noOfProducers = 5;
        int produceSpeed = 2000;

        Properties properties = new Properties();
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, applicationID);
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        properties.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryServers);

        KafkaProducer<String, PosInvoice> kafkaProducer = new KafkaProducer<>(properties);
        ExecutorService executor = Executors.newFixedThreadPool(noOfProducers);
        final List<PosSimulatorRunnable> runnableProducers = new ArrayList<>();

        for (int i = 0; i < noOfProducers; i++) {
            PosSimulatorRunnable runnableProducer = new PosSimulatorRunnable(i, kafkaProducer, topicName, produceSpeed);
            runnableProducers.add(runnableProducer);
            executor.submit(runnableProducer);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (PosSimulatorRunnable p : runnableProducers)
                p.shutdown();
            executor.shutdown();
            log.info("Closing Executor Service");
            try {
                executor.awaitTermination(produceSpeed * 2, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
    }

}
