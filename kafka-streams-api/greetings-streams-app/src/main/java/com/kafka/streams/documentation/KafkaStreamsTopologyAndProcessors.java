package com.kafka.streams.documentation;

import lombok.extern.slf4j.Slf4j;

/* *
 * Some of the terminologies of Kafka Streams API and how Kafka streams application are modeled?
 * A Kafka Streams application has a series of processors.
 *  1. Source Processor: The source processor is responsible for reading from the source topic or topics.
 *  2. Stream Processor: A stream processor is where you have your processing logic present, which is responsible for acting on the data that's read from the Kafka topic. This could be aggregating the data, transforming or joining the data. This is where the data enrichment happens.
 *  3. Sync Processor: The final step in stream processing application is the sync processor, where you have the processed data, the processed data written into an output Kafka topic.
 *
 * This concept of designing Kafka streams as connected nodes is basically a Directed Acyclic Graph (DAG).
 * The collection of processors together forms a topology, so topology represents your applications end to end flow. Kafka Streams also has a concept of sub topology.
 *
 * Let me explain this using a simple example. So we have the source processor at the beginning which is responsible for reading the data from a Kafka topic, and then the stream processor can split the messages into multiple branches and can write those into different topics, which is the sync processor.
 * In Kafka Streams, we branch the data into multiple streams and more advanced use cases.
 * From the sync processor we can attach another stream processor, which means the sync processor for one Topology can act as a source processor for another topology or another flow.
 * This particular concept is called sub topology.
 *
 * How the data flows in a Kafka streams application?
 * When the source processor reads a data from the Kafka topic, it places a record in the streams record buffer, and the data is passed into the topology from the record buffer one by one in the order it was received to all the nodes in the topology.
 * So events will be passed to each processor node in the topology and this process repeats. To summarize the data flow in Kafka streams application, at any given point of time, only one record gets processed in the topology.
 * With sub topologies in place, this rule is applicable to each sub topology.
 *
 * So these are the few terminologies (processors, topologies, DAG etc. to name a few) that we need to be aware of when building a Kafka Streams application.
 *
 * We have 2 options when it comes to building the source processor. We can build the source processor as a
 *   1. KStream
 *   2. KTable
 * using the streams DSL.
 *
 * KStream API is an abstraction on Kafka streams, which holds each event in the Kafka topic for processing. Let's say we have a Kafka topic which has all the records in it.
 * Now we have a KStream created using the Kafka streams DSL. A KStream gives you access to all the records, and it treats each event independent of each other.
 * Each event that's present in the KStream will be executed by the whole topology, which includes the source processor, stream processor and the sink processor.
 * Any new event will be available to the KStream. This is one of the reason why KStream is called a record stream or a log which represent everything that's happened.
 * The record stream is infinite, which means a KStream is infinite. It is going to constantly get the new records that's available in the Kafka topic, or that's been pushed into the Kafka topic, and it's going to have the whole topology executed from that new record.
 * As an analogy, you can think of KStream as equivalent to inserts in the DB table because any new row in a DB table is independent of the other one.
 * */
@Slf4j
public class KafkaStreamsTopologyAndProcessors {
}
