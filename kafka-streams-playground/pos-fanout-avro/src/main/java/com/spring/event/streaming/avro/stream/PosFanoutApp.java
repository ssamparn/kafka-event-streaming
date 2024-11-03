package com.spring.event.streaming.avro.stream;

import com.spring.event.streaming.avro.generated.PosInvoice;
import com.spring.event.streaming.avro.mapper.RecordMapper;
import com.spring.event.streaming.avro.serdes.AppSerdes;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;

/* *
 * What is Topology?
 * A processor topology or simply Topology defines a step-by-step computational logic of the data processing that needs to be performed by a stream processing application.
 * Every Kafka streams application or the Topology starts by subscribing to some Kafka topics, because we need to consume a data stream to begin our processing.
 * So, your Topology begins by creating a source processor. Creating a source processor is straightforward. A source processor internally implements a Kafka Consumer to consume data from one or multiple Kafka topics.
 * And it produces a KStream object that will be used by its down-stream processors as an input stream.
 * Once you have a source processor and your first KStream object, you are ready to add more processors in the Topology. Adding a new processor node is as simple as calling a transformation method on the KStream object.
 * For example, we take KStream that was returned by the source node and make a call to the KStream.foreach() method. This operation simply adds a foreach() processor node to the Topology.
 * However, you can use the KStream transformation methods to create a processor nodes resulting in a more sophisticated topology.
 *
 * KStream:
 * The KStream class is an abstraction of a stream of Kafka message records, and it supports a variety of transformation operations.
 * All these transformations are defined as functions or methods such as filter(), map(), flatMap(), foreach(), and many more.
 * Some KStream transformations may return one or more KStream objects. For example, filter() and map() on a KStream will generate another KStream.
 * You can use such transformations to create a chain of processor nodes. Some other KStream transformations do not return a new KStream.
 * For example, foreach() and to() on a KStream returns a void. These transformations are known as a terminating or a sink processor.
 * Since they do not return a new KStream object, they are used at the terminal end of your processor topology.
 *
 * Problem Statement:
 * We created a POS simulator application that continuously generates random invoices and sends them to a Kafka topic, right? If you start the POS simulator application,
 * you get a continuous stream of invoices flowing to the Kafka topic. Once you have the POS simulator running, you are ready to develop a Kafka Streams application and process those invoices in real-time.
 *
 * Business requirement: XYZ is a Home Furniture and Kitchen utensils retailer. They have 20 retail stores spread all over the country.
 * XYZ management decided to transform themselves into a real-time data-driven organization. As a first step towards that goal, they started sending their invoices to a Kafka cluster in real-time.
 * The POS machines in all their stores are now sending invoices to a Kafka topic 'pos'.
 * As a next step, they want to create the following automated services.
 *
 *   - Shipment Service
 *   - Loyalty Management Service, and
 *   - Trend Analytics.
 *
 * While other teams are working on the implementation details of these 3 services, you are asked to create a Kafka Streams application that does following.
 *
 * Select Invoices where DeliveryType = "HOME-DELIVERY" and push them to the shipment service queue.
 * Select Invoices where CustomerType = "PRIME" and create a notification event for the Loyalty Management Service. The format for the new notification event is provided.
 * Select all Invoices, mask the personal information, and create records for Trend Analytics. When the records are ready, persist them to Hadoop storage for batch analytics.
 * The format for the new Hadoop record is also given.
 * Great!
 * The primary objective of this example is to help you understand the most critical ingredient of the Kafka Streams application.
 * That is How to create a Topology?
 * */
@Slf4j
public class PosFanoutApp {

    private static final String applicationID = "PosFanout";
    private final static String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final String posTopicName = "pos-avro";
    private static final String shipmentTopicName = "shipment-avro";
    private static final String notificationTopic = "loyalty-avro";
    private static final String hadoopTopic = "hadoop-sink-avro";
    private static final String CUSTOMER_TYPE_PRIME = "PRIME";
    private static final String DELIVERY_TYPE_HOME_DELIVERY = "HOME-DELIVERY";

    public static void main(String[] args) {
        Properties streamProperties = new Properties();
        streamProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationID);
        streamProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, PosInvoice> sourceKStream = streamsBuilder.stream(posTopicName, Consumed.with(AppSerdes.String(), AppSerdes.PosInvoice()));

        sourceKStream.filter((k, v) -> v.getDeliveryType().equalsIgnoreCase(DELIVERY_TYPE_HOME_DELIVERY))
                .to(shipmentTopicName, Produced.with(AppSerdes.String(), AppSerdes.PosInvoice()));

        sourceKStream.filter((k, v) -> v.getCustomerType().equalsIgnoreCase(CUSTOMER_TYPE_PRIME))
                .mapValues(RecordMapper::getNotification)
                .to(notificationTopic, Produced.with(AppSerdes.String(), AppSerdes.Notification()));

        sourceKStream.mapValues(RecordMapper::getMaskedInvoice)
                .flatMapValues(RecordMapper::getHadoopRecords)
                .to(hadoopTopic, Produced.with(AppSerdes.String(), AppSerdes.HadoopRecord()));

        Topology topology = streamsBuilder.build();
        KafkaStreams streams = new KafkaStreams(topology, streamProperties);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stopping Stream");
            streams.close();
        }));

    }

}
