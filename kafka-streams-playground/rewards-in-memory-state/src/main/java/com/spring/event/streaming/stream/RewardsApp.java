package com.spring.event.streaming.stream;

import com.spring.event.streaming.generated.PosInvoice;
import com.spring.event.streaming.kafkautil.RewardsPartitioner;
import com.spring.event.streaming.kafkautil.RewardsTransformer;
import com.spring.event.streaming.serde.AppSerdes;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Repartitioned;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import java.util.Properties;

/* *
 * We know Kafka Streams DSL and building topologies.
 * However, until now, we have been working with each event in isolation. We picked up one invoice at a time, and either computed some values such as loyalty points on the invoice itself
 * or transformed it into some other event such as a Hadoop Record. In this scenario, each event is handled independently without referring to other concurrent events or earlier events.
 * Working with individual events in isolation is a common requirement. However, most of the stream processing requirements would need you to remember some information or context from past events.
 *
 * That's where states and the state stores comes into picture, which are used by stream processing application to remember some information from the past.
 *
 * Problem Statement: Let's try to understand the need to remember the information using a realistic example. We created an invoice processing example & reading invoices from a Kafka topic.
 * And we computed reward points earned by the customer on a given invoice. Then, we sent a notification to the customer in real-time.
 * However, we calculated rewards only for the current invoice. e.g: if a customer buys something for 5K, and she earned 50 points. We sent her an SMS telling her about the 50 points.
 *
 * What about the points that she made earlier? It would make more sense for the customers if we can include the past rewards and notify them of the total points. So, we want to extend the earlier example and inform the customer with her total rewards. How can we do that?
 * We cannot achieve this objective without remembering previous totals. Right? Can you try to develop a solution to this problem?
 *
 * One Possible solution can be, we can create a source processor to read a stream of invoices and filter to receive only the Prime customer invoices. Then we apply a mapValues() processor to transform the invoice into a notification object.
 * The mapValues() processor would also calculate the rewards for the current invoice and include it to the notification message. However, we want to add the current rewards to the previous points. We can do that by implementing a separate processor node.
 * This new node will query a table for the earlier total and add the current value with the previous value, right? Then update the total in the notification message. We should also update the new total points to the lookup table for the next use.
 * Finally, we sink the notification in a different topic for the SMS service. The solution looks straightforward. However, in this solution, the lookup table is the most critical component.
 * The table maintains the current State of customer rewards. In a real-time streaming application, such tables are termed as State.
 *
 * States are fundamental to an application. Almost every application maintains some state, and that would often be necessary and fundamental to building stream processing solution.
 * A processor state could be as simple as a single key-value pair, or it could be a large lookup table or maybe a bunch of tables. It all depends on the complexity and requirement.
 * Creating and maintaining a State requires a state store. You can create an in-memory state store or may want to use a database or a file system to create a persistent state store.
 * Whatever you chose to implement your state store, it must provide two features.
 *  1. Faster Performance, because we are working on a real-time project.
 *  2. Fault tolerance, because we are creating a distributed and scalable system.
 *
 * You might want to use a remote database such as Postgres or MySQL. You may also want to use a distributed database such as HBase or Cassandra. However, remote databases are always subject to a performance bottleneck due to network delays.
 * Because they are remote. A few transactions every second to your remote databases could be faster. However, a sizeable real-time application processing millions of events would eventually start suffering from database performance problems.
 * So, rather than using remote databases, Kafka Streams provide you two options.
 *  1. Fault-tolerant in-memory state stores and,
 *  2. Recoverable local persistent state stores.
 *
 * States are used by a processor in the topology. Right?
 * The State is used by a particular processor, and other processors have nothing to do with this table. So, a processor may or may not need a state. Hence, we can classify processors into two categories.
 *  1. Stateless Processors, and
 *  2. Stateful Processors
 *
 * Stateless transformations do not require State, and they do not need a state store. We have already used some of the stateless processors such as mapValues(), filter(), and flatMapValues().
 * Stateful transformations depend on the State for processing inputs and producing outputs. So, they require a state store. A lot of processors such as aggregation, joining and windowing operations in Kafka Streams are Stateful operations, and hence they need access to a state store.
 *
 * V Imp Note: The way Kafka Streams framework is designed, we create a stream builder, we create a store builder (if required), and ultimately, everything goes in the topology.
 * Why? because a Topology is a unit of execution and as per Streams Architecture, each stream task would create one instance of the topology and execute it independently.
 * So, instead of creating a state store, we put a store builder in the topology so the streams task can create its own copy of the state store using the builder. If we create a store and add it to the topology,
 * then it might cause problems because the state store would be shared. And the Kafka Streams is not designed that way. It is intended to execute multiple tasks independently to allow us to scale the application both vertically and horizontally.
 *
 * Types of Kafka State Stores: In this example, we wanted to use a key/value state store because all we wanted to store is the customer id as key and the rewards as value.
 * However, Kafka Streams API offers 3 types of state stores.
 *   1. KeyValueStore
 *   2. SessionStore, and
 *   3. WindowStore
 * And all 3 are available in 2 configurations.
 *   1. In Memory, and
 *   2. Persistent Configuration
 * So, basically, you get 6 types.
 *   1. inMemoryKeyValueStore
 *   2. inMemorySessionStore
 *   3. inMemoryWindowStore
 *   4. persistentKeyValueStore
 *   5. persistentSessionStore
 *   6. persistentWindowStore
 * */
@Slf4j
public class RewardsApp {

    private static final String applicationID = "RewardsApp";
    private static final String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final String posTopicName = "pos";
    private static final String notificationTopic = "loyalty";
    public final static String rewardsTempTopic = "customer-rewards";
    private static final String CUSTOMER_TYPE_PRIME = "PRIME";
    private final static String REWARDS_STORE = "CustomerRewardsStore";

    public static void main(String[] args) {
        Properties stremProperties = new Properties();
        stremProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationID);
        stremProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        stremProperties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 3);

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // filtering the source stream and create a stream for PRIME Customers
        KStream<String, PosInvoice> primeCustomersStream = streamsBuilder.stream(posTopicName, Consumed.with(AppSerdes.String(), AppSerdes.PosInvoice()))
                .filter((key, value) -> value.getCustomerType().equalsIgnoreCase(CUSTOMER_TYPE_PRIME));

        // create a state storage
        StoreBuilder<KeyValueStore<String, Double>> keyValueStoreBuilder = Stores.keyValueStoreBuilder(Stores.inMemoryKeyValueStore(REWARDS_STORE), AppSerdes.String(), AppSerdes.Double());

        streamsBuilder.addStateStore(keyValueStoreBuilder);

        /* *
         * through():
         * Repartitioning a Kafka Stream is a common requirement for a complex stream processing application, and it can be easily accomplished by using the KStream.through() processor.
         * The through() processor writes the current stream to a given (temporary) topic, and you can supply a custom partitioning method to the through() processor which will be used to repartition the data.
         * The through() method again reads the intermediate topic and returns a new KStream. So, you don’t have to write and again read. Both the steps are taken care of by the through() method.
         * The through() is specifically designed to achieve seamless repartitioning of your KStream.
         *
         * However, through() is deprecated and repartition() is suggested
         * */
        primeCustomersStream
//                .through(rewardsTempTopic, Produced.with(AppSerdes.String(), AppSerdes.PosInvoice(), new RewardsPartitioner()))
                .repartition(Repartitioned.with(AppSerdes.String(), AppSerdes.PosInvoice()).withStreamPartitioner(new RewardsPartitioner()))
                .transformValues(() -> new RewardsTransformer(), REWARDS_STORE) // TODO: refactor transformValues and use processValues
                .to(notificationTopic, Produced.with(AppSerdes.String(), AppSerdes.Notification()));

        log.info("Starting Streams");
        KafkaStreams stream = new KafkaStreams(streamsBuilder.build(), stremProperties);
        stream.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stopping Streams");
            stream.cleanUp();
        }));
    }
}
