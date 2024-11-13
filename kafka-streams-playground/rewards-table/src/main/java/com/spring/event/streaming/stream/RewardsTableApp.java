package com.spring.event.streaming.stream;

import com.spring.event.streaming.generated.Notification;
import com.spring.event.streaming.generated.PosInvoice;
import com.spring.event.streaming.serde.AppSerdes;
import com.spring.event.streaming.util.NotificationsMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;

/* *
 * In rewards-in-memory-state module, we created an application to compute total rewards for our customers and send them a notification message.
 * We built that application using a custom state store and transform values API, right? However, the scenario for computing total rewards is a simple and straightforward aggregation requirement.
 * In most of the simple scenarios, you do not need to create a custom state store for calculating an aggregate manually.
 * To demonstrate the idea, we are going to rewrite the rewards computation example. In this rewrite, we will eliminate the need for manual state store and transformValues() processor.
 * Instead, we will use KStream and compute the sum of rewards using the reduce() method.
 *
 * Problem Statement: We want to transform these invoices into a Notification. Then, we want to group it on customer id.
 * And finally compute the sum of rewards by customer id.
 * */
@Slf4j
public class RewardsTableApp {

    private static final String applicationID = "RewardsTableApp";
    private static final String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final String posTopicName = "pos";
    private static final String notificationTopic = "loyalty";
    private static final String CUSTOMER_TYPE_PRIME = "PRIME";

    public static void main(String[] args) {
        Properties streamProperties = new Properties();
        streamProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationID);
        streamProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // filtering the source stream and create a stream for PRIME Customers
        KStream<String, PosInvoice> primeCustomersInvoiceStream = streamsBuilder.stream(posTopicName, Consumed.with(AppSerdes.String(), AppSerdes.PosInvoice()))
                .filter((key, value) -> value.getCustomerType().equalsIgnoreCase(CUSTOMER_TYPE_PRIME)); // Here key is storeId.

        /* *
         * We want to use the map() method, because we not only want to transform the invoice to a notification but also want to change the key from store-id to customer-id.
         * So, we will be using a key changing API. We will be changing the key from store-id to customer-id & we want to compute an aggregate on customerId.
         * This change in the key will also ensure that the stream is automatically repartitioned, and the data for the same customer id remains in a single partition.
         * */
        KGroupedStream<String, Notification> kGroupedStream = primeCustomersInvoiceStream
                .map((key, invoice) -> new KeyValue<>(invoice.getCustomerCardNo(), NotificationsMapper.getNotificationFrom(invoice)))
                .groupByKey(Grouped.with(AppSerdes.String(), AppSerdes.Notification()));  // group by customerId

        KTable<String, Notification> loyaltyPointsKTable = kGroupedStream.reduce((currentValue, newValue) -> {
            newValue.setTotalLoyaltyPoints(currentValue.getTotalLoyaltyPoints() + newValue.getTotalLoyaltyPoints());
            return newValue;
        });

        /* *
         * limitations of reduce():
         * One crucial observation about the reduce() method here. The reduce() method comes with a limitation. It does not allow you to change the type of the stream. What does it mean?
         * Here the input of the reduce() method is a KGroupedStream of notification. And the output of the reduce() method is a KTable of Notification.
         * The type of the key and the value of the reduce() method remains the same. You cannot apply a reduce() on a stream of invoices and return an aggregated table of Notifications. That’s not allowed.
         * And hence, the input key/value type and the output key/value type of the reduce() method remains the same. This is not a problem in most cases. However, it increases the number of steps in some use cases.
         *
         * For example, if the reduce() method can take a KGroupdStream of invoices and returns an aggregated table of Notifications, we do not need this map() method. I mean, the map() method is doing two things.
         *   1. Changing the key to a customer id. We can do that in the groupBy method instead of using the groupByKey(). The map method was not needed to change the key.
         *   2. The second thing that we are doing in this map() method is to turn an invoice into a notification. We can avoid the map() method if the reduce() method allows us to take an invoice and return a notification. But this is not permitted.
         *
         * And that is where the aggregate method comes in. The aggregate() method allows you to change the types. See kStream-aggregate module.
         * */

        loyaltyPointsKTable.toStream().to(notificationTopic, Produced.with(AppSerdes.String(), AppSerdes.Notification()));

        log.info("Starting Streams");
        KafkaStreams stream = new KafkaStreams(streamsBuilder.build(), streamProperties);
        stream.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stopping Streams");
            stream.cleanUp();
        }));
    }
}
