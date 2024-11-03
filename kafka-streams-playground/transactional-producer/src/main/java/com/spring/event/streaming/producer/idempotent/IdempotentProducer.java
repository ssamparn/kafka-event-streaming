package com.spring.event.streaming.producer.idempotent;

/* *
 * Idempotence basically means Exactly once.
 *
 * At Least Once Semantics:
 * Apache Kafka provides message durability guaranties by committing the message at the partition log. The durability simply means,
 * once the data is persisted by the leader broker in the leader partition, we can't lose that message till the leader is alive. However, if the leader broker goes down,
 * we may lose the data. To protect the loss of records due to leader failure, Kafka implements replication, right? Kafka implements replication using followers.
 * The followers will copy messages from the leader and provide fault tolerance in case of leader failure. In other words, when the data is persisted to the leader
 * as well as the followers in the ISR list, we consider the message to be fully committed. Once the message is fully committed, we can't lose the record until the leader,
 * and all the replicas are lost, which is an unlikely case. However, in all this, we still have a possibility of committing duplicate messages due to the producer retry mechanism.
 * If the producer I/O thread fails to get a success acknowledgment from the broker, it will retry to send the same message. Now, assume that the I/O thread transmits a record to the broker.
 * The broker receives the data and stores it into the partition log. The broker then sends an acknowledgment for the success, and the response does not reach back to the I/O thread due to a network error.
 * In that case, the producer I/O thread will wait for the acknowledgment and ultimately send the record again assuming a failure. The broker again receives the data,
 * but it doesn't have a mechanism to identify that the message is a duplicate of an earlier message. Hence, the broker saves the duplicate record causing a duplication problem.
 * This implementation is known as at-least-once semantics, where we cannot lose messages because we are retrying until we get a success acknowledgment. However, we may have duplicates
 * because we do not have a method to identify a duplicate message. For that reason, Kafka is said to provide at-least-once semantics.
 *
 * At Most Once Semantics:
 * Kafka also allows you to implement at-most-once semantics. How?
 * Well, that`s easy. You can achieve at-most-once by configuring the retires to zero.
 * In that case, you may lose some records, but you will never have a duplicate record committed to Kafka logs.
 *
 * However, some use cases want to implement exactly-once semantics. I mean, we don't lose anything, and at the same time, we don't create duplicate records.
 *
 * Exactly Once Semantics:
 * To meet exactly once requirement, Kafka offers an idempotent producer configuration.
 * All you need to do is to enable idempotence, and Kafka takes care of implementing exactly-once.
 * To enable idempotence, you should set the enable.idempotence producer configuration to true. Once you configure the idempotence, the behavior of the producer API is changed.
 * There are many things that happen internally, but at a high level, the producer API will do two things.
 *   1. It will perform an initial handshake with the leader broker and ask for a unique producer id.
 *      At the broker side, the broker dynamically assigns a unique ID to each producer.
 *   2. The next thing that happens is the message sequencing. The producer API will start assigning a sequence number to each message.
 *      This sequence number starts from zero and monotonically increments per partition. Now, when the I/O thread sends a message to a leader, the message is uniquely identified by the producer id and a sequence number.
 *      Now, the broker knows that the last committed message sequence number is X, and the next expected message sequence number is X+1.
 *      This allows the broker to identify duplicates as well as missing sequence numbers.
 * So, setting enable.idempotence to true will help you ensure that the messages are neither lost not duplicated. How exactly it happens is not much relevant.
 * We leave that on producer API and broker. All you need to do is to set the configuration to activate this behavior.
 * However, you must always remember one thing. If you are sending duplicate messages at your application level, this configuration cannot protect you from duplicates.
 * That should be considered as a bug in your application. Even if two different threads or two producer instances are sending duplicates, that too is an application design problem.
 * The idempotence is only guaranteed for the producer retires. And you should not try to resend the messages at the application level.
 * Idempotence is not guaranteed for the application level message resends or duplicates send by the application itself.
 * */
public class IdempotentProducer {


}
