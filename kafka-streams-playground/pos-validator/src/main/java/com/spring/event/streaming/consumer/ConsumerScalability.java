package com.spring.event.streaming.consumer;

/* *
 * We created a real-time data validation pipeline in PosValidator. Our application reads messages from a Kafka topic, run some validations against them, and write the valid and invalid messages in separate Kafka Topic.
 * Instead of writing to Kafka topics, you can also write them to a database or any other kind of storage system. It is just a matter of changing a few lines of code. Right?
 * In all such consume-transform-produce pipelines, your application will create a KafkaConsumer object, subscribe to the appropriate Topic, and start receiving messages, transforming them and writing the results.
 * This process should be seamless. However, your application will begin falling behind, if the rate at which producers write messages to the Topic, exceeds the rate at which you can transform
 * and write them to external storage. If you are limited to a single consumer reading and processing the data, your application may fall farther and farther behind and would no longer remain a real-time application.
 *
 * In this scenario, we need to scale consumption from topics. But the question is, How? How do we scale a consumer process?
 * We can scale a consumer application by dividing the work among multiple consumers. Just like numerous producers can write to the same Topic, we need to allow various consumers to read from the same Topic.
 * However, we also need a mechanism to split the data among the consumers, so they work with their own set of data and do not interfere with each other.
 *
 * How can you do that? This is where the topic partitions are handy. When we have multiple consumers working in a group and reading data from the same Topic, we can easily split the data among consumers by assigning them one or more partitions.
 * In this arrangement, each consumer is attached with a set of Partitions, and they read data only from the assigned partitions. For example, if you have 10 partitions in a topic,
 * and there are two consumers in the same group reading data from the same Topic, Kafka would assign 5 partitions to each consumer. This arrangement clearly divides the data among the consumers to ensure
 * that they do not read the same message. In this arrangement, every record is delivered to one and only one consumer in the group, and there is no duplicate processing.
 * But, this arrangement also adds a restriction on the scalability, that would be equal to the number of partitions in a given topic.
 * For example, if you have 10 partitions in a topic, you can add a maximum of ten consumers in a group, each assigned with a single partition to read.
 *
 * However, this limitation can be avoided by careful planning and creating enough number of partitions when we create a new topic. Now the next question is obvious.
 * How can we create a consumer group and add new consumers in the same group? Do we also need to do something for assigning partitions to the consumers?
 * Kafka offers automatic group management and re-balancing of the workload in a consumer group. All we need to do is to set the group id configuration.
 * Kafka automatically forms a consumer group, and it would also add the consumer to the same group if they have the same group id.
 * Kafka will also take care of assigning partitions to the consumer in the same group. Membership in a consumer group is maintained dynamically.
 * If a consumer fails, the partitions assigned to it will be reassigned to other consumers in the same group.
 * Similarly, if a new consumer joins the group, partitions will be moved from existing consumers to the new one to maintain the workload balance.
 *
 * Conceptually, you can think of a consumer group as being a single logical consumer that happens to be made up of multiple processes sharing the workload. Kafka automatically manages all of this,
 * and the whole process is transparent to users. You, as a programmer is responsible for setting the group id configuration and starting a new consumer process either on the same machine or on a separate computer.
 * So, the problem of scalability for the consumers is taken care of by the consumer groups.
 *
 * The issue of fault tolerance is also taken care of by the re-balancing within the consumer groups. Right? However, we still have an open question.
 * Assume that the partition was initially assigned to a consumer. The consumer processed some messages for a while and crashed.
 * Kafka automatic re-balancing will detect the failure of the consumer and reassign the unattended partition to some other consumer in the group. This is where we get a new doubt.
 * The new consumer should not reprocess the events that are already processed by the earlier consumer before it failed. Right? How would Kafka handle this?
 *
 * Let's try to understand. We already know, that an offset uniquely identifies every message in a partition. Kafka also maintains two offset positions for each partition.
 *  a. Current offset position,
 *  b. Committed offset position.
 *
 * These positions are maintained for the consumer. The Current offset position of the consumer is the offset of the next record that will be given out to the consumer for the next poll().
 * In the beginning, the current offset might be unknown or null for a new consumer subscription. In that case, you can set the auto-offset-reset configuration to the earliest or latest.
 * If you set to earliest, then Kafka will set the current offset position to the first record in the partition and start giving you all the messages from the beginning.
 * Otherwise, the default value is the latest, which will send you only upcoming messages after the consumer subscribed and ignore all earlier messages.
 * This all happens only in the beginning when the current-offset, as well as the committed-offset, is undefined.
 * Once the initial current-offset is determined, it automatically advances every time you poll() some messages. So, you never see the same record twice.
 * The current offset is persistent to the consumer session. If the consumer fails or restarts, then the current offset is determined once again.
 * For that reason, if you restart a consumer after a failure, you may start getting the records once again that were already sent in the earlier session.
 * To avoid that situation, Kafka also maintains a committed offset position. Every time you poll(), the consumer will automatically commit the earlier current-offset and send some more records.
 * These new records are then automatically committed by the next poll(). This mechanism is known as auto-commit. However, you can change this behavior by setting the enable-auto-commit to false.
 * The default value for enable-auto-commit is True. If you set it to false, then you can manually commit the offsets by calling one of the commit APIs commitSync() and commitAsync()
 * The committed offset position is the last offset that has been stored securely at the broker. So, when the consumer process fails and restarts, or the partition is reassigned to some other consumer in the group,
 * the committed offset is used to override the current offset position for the consumer.
 *
 * So, in summary, the committed offset is securely stored with the broker.  When a consumer restart or the partition is reassigned to another consumer, the committed-offset is used to avoid duplicate processing.
 * And all this happens automatically in most of the cases. But you also have options to take control in your hand and do it manually using commit APIs. The current offset is determined as latest or earliest
 * only when there is no committed offset. Otherwise, the committed-offset is used to set the current offset.
 *
 * Great, So, it looks like we have a full-proof system. But still, there are many gotchas in it. Let's look at the scenario that we implemented in PosValidator.
 * We have a consumer. Right? We poll() some messages. Segregate them into valid and invalid. Send them to different topics. And before we poll() again for more messages, the consumer crashed.
 * What will happen? The committed offset at the broker is still null. Why? Because the committed-offset is updated when we poll again. That is the time when the broker assumes
 * that we successfully processed the earlier ones so those messages should be committed to avoid a resend.
 * But in our case, we already processed but crashed before we could poll again to let the broker know about the successful processing.
 * This situation is going to create duplicates for sure.
 *
 * How do you handle this gotcha?
 * We know about implementing Kafka transactions. You can execute transactions to handle this scenario. However, that requires extra custom coding, testing and still leaves more space for introducing new bugs.
 * Similarly, think about another scenario. Instead of merely performing validation, you wanted to compute total sales by store id. How would you do it? At first sight, it looks simple. Read the store id and total sale value from the invoice.
 * Insert it to a key/value map. Where the key is the store id, and the value is the total sale. For the next invoice, do the same but this time sum it to the previous amount.
 * Simple? Isn't it? But what about fault tolerance? What if your application crashed? You would lose the in-memory map. Correct? So you need to save it somewhere. Maybe on your local disk.
 * But what if the crash was due to disk failure? Ok, so you can maintain that map in a remote database. That looks safe. Right? But you have to write a lot of custom code again, and extend your transaction up to the remote database to avoid duplicate processing.
 * Right? You also need to handle database concurrency, performance, and additional network latency.
 *
 * Now think about computing store wise total sale in a five-minute window. I mean, adding one more dimension of the time to whatever you are computing. Similarly, think about joining two topics in real-time.
 * I mean, one consumer is reading topic A, another consumer is reading topic B, and you want to join these two topics and perform some computation. All that can be done, but not that easy.
 * You will end up writing a lot of complex code, increasing the cost and complexity of your application.
 * The point is straightforward. Basic features and facilities for creating real-time stream processing applications are missing from the Kafka Consumer APIs.
 * That is where Kafka Streams API turns out to be handy and helps you to model most of the stream processing requirements easily.
 * Great!
 * */
public class ConsumerScalability {

}
