package com.spring.event.streaming.stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

/* *
 * Kafka Streaming:
 *
 * Kafka Producers brings data into the Kafka cluster. Kafka producers are a critical part of your streaming applications because these producers are responsible for originating the data streams
 * and bringing them into the Kafka cluster. Once the stream starts flowing into Kafka, you are ready to tap into these streams and plug-in your stream processing applications.
 * There are many ways to create the stream processing applications. Kafka offers you 3 tools for that purpose.
 *    1. Consumer APIs
 *    2. Kafka Streams Library, and
 *    3. KSQL
 * You can use Kafka consumer APIs to consume data from Kafka brokers and create your stream processing applications. However, the consumer API approach only applies to a simple
 * and straightforward stream processing application. For example, a simple data ingestion pipeline reads a stream, performs some validations, and store valid and invalid data into two different NoSQL tables.
 * You can quickly implement such real-time data flows using Kafka consumer APIs.
 *
 * However, a more complex and sophisticated use case would require a lot more facilities that are not offered by the Kafka consumer APIs.
 * For example, you are receiving a patients pulse oximetry reading, which is also known as SpO2 reading. You are streaming them to a Kafka topic, and you want to write a stream processing application.
 * Your application reads all the values received in a five-second window, and emit the mean value to create a SpO2 trend chart. You can do that using usual Kafka Consumer APIs.
 * However, you may have to implement a five-second window by yourself. Think about it. Applying a window of five-second, filtering only those records that fall within the window,
 * and computing aggregates over the selected records. You will end up in a lot of custom coding and potential bugs. What about if your application crashes in the middle of the window?
 * You need to handle the fault tolerance and scalability scenarios as well. For that reason, Kafka offers you a brand-new library for developing real-time streaming applications.
 * This new library is built on top of Kafka producer and consumer APIs and known as Kafka Streams API. Streams API is the next level of abstraction to offer you a framework that makes real-time stream processing simple and straightforward.
 * Everything that you can do using Kafka Consumer APIs can also be achieved using Streams API but more quickly.
 *
 * KSQL is the third alternative which offers an interactive SQL like interface for stream processing on Kafka. You can write KSQL queries interactively and view the results in real-time on the KSQL CLI.
 * However, you also have an option to save a KSQL file and deploy it to production. These saved KSQL files will be executed by the KSQL servers. KSQL offers you a quick and easy way to do a lot of things.
 * However, real-life applications are designed using microservices architecture. You cannot do that using KSQL. So, end of the day, you would want to use Kafka Streams library.
 * One more point about KSQL, KSQL is offered by Confluent.io, and it does not come with an Apache 2.0 license.
 *
 * What is Kafka Streams?
 *
 *  1. Kafka Streams is a client library for processing and analyzing data stored in Kafka.
 *  2. You can easily embed Kafka streams library in your Java application and execute it on a single machine or package and deploy using docker containers.
 *  3. When you want to scale your system, you can run multiple instances of the same application on various machines.
 *  Yet, you do not need to get into the complexities of load balancing and fault tolerance. Kafka Streams API handles all of that transparently.
 *  4. The core functionality of Kafka Streams library is available in two flavors. a) Streams DSL b) Processor API.
 *
 * Streams DSL is a high-level API, which is a very well thought API set that allows you to handle most of the stream processing needs.
 * Hence, DSL is the recommended way to create Kafka Streams application, and it should cover most of your needs and most of the use cases.
 *
 * Processor APIs are the low-level API, and they provide you with more flexibility than the DSL. Using Processor API requires little extra manual work and code on the application developer side.
 * However, they allow you to handle those one-off problems that may not be possible to solve with higher-level abstraction.
 * */

/* *
 * Problem Statement:
 * Create a straightforward Kafka streams application to do the following two things.
 *   1. Connect to the Kafka cluster and start reading a data stream from a given topic.
 *   2. Print the stream on the console. We do not want to get into the processing yet, but simply dump it to the console.
 *
 * What are Serdes in Kafka Streaming?
 * Serdes is a factory-class that combines a serializer, and a deserializer. In this example, we are setting an Integer Serde for the Key and a String Serde for the value.
 * You might be wondering, why do we need a Serdes? In earlier examples, we created producers using a serializer and consumed using a deserializer. But why do we need Serdes for the Streams API?
 * Well, the answer is straightforward. This example is a data consuming stream application. We read a stream and print it. That is all we are going to do.
 * However, a typical Kafka Streams application would be reading data and writing it as well. Hence, they internally create a combination of consumer and producer.
 * So, they would need a serializer as well as a deserializer. Therefore, the Streams API takes a Serdes approach for the Key and the value. Instead of specifying two configurations all the time, it's define both at once.
 * */
@Slf4j
public class HelloStreams {

    private static final String applicationID = "HelloStreams";
    private final static String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final String topicName = "hello-kafka";

    public static void main(String[] args) {
        /* *
         * The kafka streams computational logic is straightforward as following. Open a stream to a source topic. That means, define a Kafka stream for a Kafka topic that can be used to read all the messages.
         * Process the stream. In our case, for each message, print the Key and the Value. We want to use Kafka Streams DSL for defining the computational logic.
         * Most of the DSL APIs are available through StreamsBuilder() class.
         *
         * Step 1: Define the configuration properties.
         *
         * Step 2: Create a StreamBuilder object.
         * After creating a builder, you can open a Kafka Stream using the stream() method. The stream() method takes a Kafka topic name and returns a KStream object.
         * Once you get a KStream object, you have successfully completed the first part of your computational logic that is, Opening a stream to a source topic.
         * The KStream class provides a bunch of methods for you to build your computational logic.
         *
         * Step 3: To implement the second part of our computational logic, that is, Process the stream. The foreach() method takes a lambda expression on key and value pair,
         * and the logic to work with each pair is implemented in the lambda body. All we want to do is to println the Key and the value. That`s it.
         *
         * Step 4: Finally, the last step of implementing the logic is creating a Topology. The Kafka Streams computational logic is known as a Topology and is represented by the Topology class.
         * So, whatever we defined as computational logic, we can get all that bundled into a Topology object by calling the build() method.
         * So, what it means, everything that we have done so far, starting from the builder.stream(), the foreach() call, all that is bundled inside a single object called Topology.
         * We are using a Java builder pattern, that allows for the step-by-step creation of complex objects using the correct sequence of actions. So we define a series of activities for the Topology
         * and finally, call the build() method to get the topology object. Once we have the Properties and the Topology, we can now instantiate the KafkaStreams object and start() it.
         *
         * However, there is a step 5 also. A typical Kafka stream application is an always running application. So, once started, it keeps running forever until you bring it down for some maintenance reasons.
         * However, when you want to bring it down, you must be able to perform some clean-up activity and gracefully shut down your stream. How to do it? Simple. Add a ShutdownHook.
         *
         * So the core of Kafka streams application is the Topology of the application.
         * */
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationID);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Integer().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<Integer, String> kStream = streamsBuilder.stream(topicName);
        kStream.foreach((key, value) -> log.info("Key: {}, Value: {}", key, value));

        Topology topology = streamsBuilder.build();
        KafkaStreams kafkaStreams = new KafkaStreams(topology, properties);
        log.info("Starting Stream...");
        kafkaStreams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down stream");
            kafkaStreams.close();
        }));
    }

}
