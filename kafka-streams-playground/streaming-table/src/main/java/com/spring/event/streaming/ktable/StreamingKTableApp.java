package com.spring.event.streaming.ktable;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;

import java.util.Properties;

/* *
 * We have been working with KStream objects. We created a KStream by subscribing to a Kafka Topic. Then we performed different types of transformations on them such as
 * filter(), mapValues() and transformValues(). All these operations gave us a new KStream. And in most of the cases, we pushed the final KStream to a different Kafka topic.
 * However, we also realized that working with KStream is like working with individual messages one at a time. Right? If you have a need to remember past information, or if you want to work with a group of messages,
 * you are going to need a local state store. We have already seen one example where we wanted to calculate total rewards for a customer. And, we achieved it using a local state store.
 *
 * Let's think about another scenario. You are asked to calculate an hourly average invoice value. For example, you got 10 invoices between 9.00 AM and 10.00 AM. What is the average invoice value for this one-hour window?
 * But can you do it using KStream and not using state store? You can't. You need a state store, where you can hold all the invoices coming between 9.00 AM to 10 AM. Once you have all the invoices, at 10.00 AM, you can quickly compute the average. Right?
 * It is a matter of taking an average. And look at the state store. Doesn't it look like a table, which holds all the invoices in an hour window.
 *
 * The point straight forward. Any data processing system, even if it is a real-time stream processing, must have a notion of Tables and Aggregates. And Kafka Streams is no different.
 * Kafka streams API allows you to create tables using an abstraction called KTable. You can think of the KTable as a local key/value state store where you can store your message key and value.
 * You can create KTable in the same way you create KStream. For example, we created KStream by opening a stream to a Kafka Topic. Right?
 * Same way, you can create a KTable by opening a table against a Kafka Topic. When you create a KTable on a topic, all the messages from that topic will start flowing to the table, and they will go and sit in a local state store.
 *
 * Let's assume you opened a KTable for stock tick topic. Each message in the topic comes with a key and a value. The message key is the stock symbol. And the value contains some fields like timestamp, last traded price, and last traded quantity.
 * You got your first record as HDFCBANK with the value. The record will go and sit in your KTable. In this case, the KTable will have an internal local state store. So the record will go and sit in the state store.
 * You got the next record as TCS. This record will also go and sit in the local state store. So, all the new symbols that you are receiving will go to the state store.
 * But what happens when you receive a second tick for the HDFCBANK? Since the state store is a Key/Value state store, you cannot have duplicates. So, the next tick for HDFCBANK will replace the earlier tick.
 * And that's a standard thing for tables. Right? It is an update operation. So, the old record for the HDFCBANK is updated with the new and the most recent value of the HDFCBANK. Right?
 *
 * So a KTable is a Table like structure in Kafka Streams which offers an UPSERT operation. If we get a new record, it should be inserted. But if we get a new value for an existing key, we should be able to update the old value.
 * So, to summarize, KTable is an abstraction of an updated stream. We also call it a changelog stream. You can visualize it as a table with a primary key, and each data record in a KTable is an UPSERT.
 *
 * Now one last thing? How do we delete a record from the KTable? Well, if we wanted a table, we must be able to implement the CRUD operation. We know we can Create, Read, and Update in a KTable.
 * What about Delete? Well, that's simple. If you get a record for a key with value as null, the KTable will delete the old record for the same key.
 * For example, if we get a new record for HDFCBANK with a null value, the KTable will consider it as delete operation and remove the HDFCBANK record from the state store.
 *
 * Great!
 *
 * We are going to create a super simple example to understand some details of using KTable. We will take a problem solution approach. So, let's define the problem.
 * Problem Statement: We want to implement the stock ticker scenario.
 * Create a Kafka topic that will receive a key-value pair of stock ticks. Here is an example of a stock tick message.
 *
 *  HDFCBANK:1250.00
 *  TCS:2150.00
 *  KOTAK:1570.00
 *  HDFCBANK:1255.00
 *  HDFCBANK:
 *  HDFCBANK:1260.00
 *  ICICI:2500.00
 *  AXIS:3200.00
 *
 * The message key is a stock symbol. And the value is a numeric value representing the last traded price. We want to keep it simple. We want to send the following sequence of stock ticks to the Kafka topic.
 * Now, you are asked to create a Kafka Streams application to demonstrate the KTable CRUD behavior as explained below.
 *   1. Read the topic as KTable.
 *   2. Filter out all other symbols except HDFCBANK and TCS. We just want to track two symbols.
 *   3. Store the filtered messages in a separate KTable.
 *   4. Show the contents of the Final KTable.
 *
 * KTable Summary:
 *   1. KTable is an updated stream backed by a local state store.
 *   2. Records are Upserted into the KTable. That means, new records are inserted, and existing records are updated.
 *   3. A record with an existing key and a null value is a delete operation for the KTable.
 *   4. You can use KTable in the same way you are using KStream. That means you can read messages from Kafka into a KTable, apply transformations on the KTable and also convert them to KStream if needed.
 *
 * One final note, In a KStream, all the records will flow in a sequence. There is no relation to the current record with any previous record.
 * But in KTable, a new record might update an older record if it already exists in your table.
 * */
@Slf4j
public class StreamingKTableApp {

    private static final String applicationID = "StreamingTable";
    private static final String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final String topicName = "stock-tick";
    private static final String stateStoreLocation = "tmp/state-store";
    private static final String stateStoreName = "kt01-store";
    private static final String regExSymbol = "(?i)HDFCBANK|TCS";

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationID);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(StreamsConfig.STATE_DIR_CONFIG, stateStoreLocation);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KTable<String, String> stockTickerKTable = streamsBuilder.table(topicName, Consumed.with(Serdes.String(), Serdes.String())); // Here Consumed.with(Serdes.String(), Serdes.String()) is optional
        stockTickerKTable.toStream().print(Printed.<String, String>toSysOut().withLabel("stockTickerSourceKTable")); // Here printing for debugging purpose. KTable does not offer a print(), so we have to convert the KTable to a KStream in order to print it.

        /* *
         * If we don't provide a state store name, the API will automatically assign some internal name, and that's fine in most of the cases.
         * But when you want to work directly with your internal state store, you better give it a proper name.
         * */
        // filtering HDFCBANK and TCS stock symbols
        KTable<String, String> filteredKTable = stockTickerKTable.filter((key, value) -> key.matches(regExSymbol) && !value.isEmpty(), Materialized.as(stateStoreName));
        filteredKTable.toStream().print(Printed.<String, String>toSysOut().withLabel("filteredStockKTable"));

        Topology topology = streamsBuilder.build();
        KafkaStreams streams = new KafkaStreams(topology, properties);

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down servers");
            streams.close();
        }));

    }
}
