package com.spring.event.streaming.kafkautil;

import com.spring.event.streaming.generated.PosInvoice;
import org.apache.kafka.streams.processor.StreamPartitioner;

/* *
 * Why a Custom Partitioner is required?
 * Without the custom partitioner, the program should work perfectly fine as long as we are running one single-threaded instance of this application. The moment we scale it, we will start getting crazy with unexpected results.
 * The example assumes that the Invoices are flowing to Apache Kafka topic from retail stores. The pos-simulator imitates the exact situation.
 * For some good reason, we decided to use storeId as a key and used default partitioner while sending the invoices to the Kafka topic.
 * We already learned that the default partitioner would use the hash value of the message-key to route the messages to a partition.
 * Hence, all the invoices for the same store (key) would go to the same partition.
 * So, in our example, all the invoices generated at a specific store would always land in the same partition because we used store id as a key.
 * So far, so good. However, we already learned that the Kafka Streams is a distributed application framework, and the topic partitions are used to share the workload among the multiple instances of the same application.
 * Let's assume that we have 3 stores, 3 partitions, and we are executing 3 instances of the same application on 3 different machines.
 * In this setup, each instance would be processing data from one partition to achieve workload sharing and parallel processing. Hence, in this configuration, each application instance could be processing data from one store.
 * Right? Do you see the problem now? Let me elaborate.
 *
 * A Customer C1 purchased something from store 1. The invoice goes to partition 1 and Task 1 processes the invoice. It computes the loyalty points, let’s say 50, and stores it in the local state store.
 * Next day, the customer goes to store 2 and buys something there. The invoice goes to partition 2, which is picked by Task 2. Task 2 again computes the loyalty. Let’s say 30 points.
 * But it doesn’t have the earlier state for the customer. So, it stores 30 points in his local state store. And that’s where things start going wrong. We wanted this new 30 points to be added to the earlier 50 points.
 * It didn’t happen. Why? Because state stores are local to the task. They do not know about other states. How do you fix this problem? Well, the solution is simple.
 * Make sure all the invoices for the customer C1 comes to the same partition. No matter from which store is she buying, all her invoices must land to the same partition.
 * If that happens, then the same task would be processing all her invoices and will never make such mistakes. How do you ensure that?
 *
 * There are 2 ways to solve this problem.
 *   1. Use the right message key with the default partitioner.
 *   2. The second approach is, Ignore the message key, and use a custom partitioner.
 *
 * Let’s try to understand the first approach. If the message key is the customer ID, and you are using a default partitioner, the invoice will always land in the same partition.
 * Why? Because the default partitioner uses the hash value of the message key and computes the target partition number. As long as the message key is same, you are going to get the same hash value and hence the same partition number.
 * However, in our example, we have a problem. The message key is the store ID. But we wanted it to be Customer ID. How do you fix it? Change the message key and send the message to an intermediate and temporary topic.
 * In this process, you are keeping the message unchanged, but you will change the key. Then, you can again read from this intermediate topic and apply transform values. Since you are resending the message to a new topic,
 * messages are going to be repartitioned using the customer id. This additional step will fix your problem and bring all invoices of a single customer to the same partition. However, all this could be hectic and make your DAG a little more complicated.
 * It seems repartitioning is the only solution to this problem.
 *
 * However, we have another alternative which could be a little simpler. In this process, we still send the messages to a new intermediate topic. But we do not change anything.
 * Neither the message value nor the key. However, we apply a custom partitioner. All we wanted was to use customer id instead of the current message key. So, we implement a custom partitioner to do the same.
 *
 * And Kafka API makes this thing simple and straightforward to implement. Repartitioning a Kafka Stream is a common requirement for a complex stream processing application, and it can be easily accomplished by using the KStream.through() processor.
 *
 * V.Imp Note:
 * Repartitioning is an expensive activity, and it impacts the performance of your streaming application.
 * So, you should design your message key to minimize the need for repartitioning and avoid it wherever you can. However, the need for repartition is inevitable for any distributed processing framework, and Kafka Streams is no different.
 * You should also make sure that you create the intermediate topic beforehand and configure the number of partitions and retention period appropriately.
 * */
public class RewardsPartitioner implements StreamPartitioner<String, PosInvoice> {
    @Override
    public Integer partition(String topic, String key, PosInvoice value, int numPartitions) {
        return Math.abs(value.getCustomerCardNo().hashCode()) % numPartitions;
    }
}
