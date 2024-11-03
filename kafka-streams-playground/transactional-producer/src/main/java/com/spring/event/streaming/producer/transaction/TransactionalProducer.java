package com.spring.event.streaming.producer.transaction;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

/* *
 * Transactions in Kafka Producer:
 * The transactional producer goes one step ahead of idempotent producer and provides the transactional guarantee,
 * i.e. the ability to write to several partitions atomically. The atomicity has the same meaning as in databases,
 * that means, either all messages within the same transaction are committed, or none of them are saved.
 *
 * Problem Statement:
 * Let's say we have two topics. hello-kafka-1 and hello-kafka-2.
 * We are going to implement a transaction that would send some messages to both the topics.
 * When we commit the transaction, the messages will be delivered to both the topics. If we abort or rollback the transaction,
 * our messages should not be sent to any of these topics. That's what atomicity means. Right?
 * Implementing transactions in Kafka requires some mandatory topic level configurations.
 * All topics which are included in a transaction should be configured with
 * 1. Replication factor of at least 3. i.e: >= 3
   2. The min.insync.replicas for these topics should be set to at least 2. i.e: >= 2. (See ReadMe.md of this section)

 * After the config changes to the topics, they can participate in the transaction.
 *
 * Setting a TRANSACTIONAL_ID for the kafka producer is a mandatory requirement to implement producer transaction,
 * and there are two critical points to remember here.
 *    1. When you set the transactional id, idempotence is automatically enabled because transactions are dependent on idempotence.
 *    2. Second and most important one - TRANSACTIONAL_ID_CONFIG must be unique for each producer instance. What does that mean?
 * That means you can't run two instances of a producer with the same transactional id. If you do so, then one of those transactions will be aborted
 * because two instances of the same transaction are illegal. The primary purpose of the transactional id is to roll back the
 * older unfinished transactions for the same transaction id in case of producer application bounces or restarts.
 * You might be wondering, then how do I scale? How do I run multiple instances of the producer to achieve horizontal scalability. Well, that is simple.
 * Each instance can set its own unique transaction id, and all of those would be sending data to the same topic implementing similar transaction
 * but all those transactions would be different and will have their own transaction id. Right?
 * And that should be the case anyway. Two customers performing two transactions in parallel should have two unique transaction ids. Isn't it?
 * That's all about transaction id. Setting a transaction id is a mandatory requirement for the producer to implement a transaction.
 * It is recommended to always keep the transaction id outside the producer code in a kafka.properties file.
 *
 * Implementing transaction in the producer is a three-step process.
 *     1. The first step is to initialize the transaction by calling initTransactions(). This method performs the necessary check to ensures
 * that any other transaction initiated by previous instances of the same producer is closed. That means, if an application instance dies,
 * the next instance can be guaranteed that any unfinished transactions have been either completed or aborted, leaving the new instance in a clean state before resuming the work.
 * It also retrieves an internal producer_id that will be used in all future messages sent by the producer. The producer_id is used by the broker to implement idempotence.
 *     2. The next step is to wrap all your send() API calls within a pair of beginTransaction() and commitTransaction().
 *     3. In case you receive an exception that you can't recover from, abort the transaction and finally close the producer instance.
 * All messages sent between the beginTransaction() and commitTransaction() will be part of a single transaction.
 *
 * We created two topics for this example. I send one message to the first topic and another message to the second topic.
 * I am assuming that this transaction will begin here. The loop will run twice. I will send two messages in each loop, one message to each topic.
 * We don't expect any exceptions, and hence the code is not likely to get into the catch block.
 * In normal condition, we will commit the transaction here. So far, so good. Now, I want to add some code for the rollback scenario.
 * This would be the second transaction. The loop will again execute twice, and will send a total of 4 messages. Two messages to each topic.
 * But this time, instead of committing the transaction, it will be aborted. So, all the 4 messages that I am sending in this second transaction will be rolled back.
 * I would receive only four messages sent by the first transaction.
 * The messages sent by the second transactions should not appear in any of the topics.
 *
 * V.Imp Note: One final note about the transactions. The same producer cannot have multiple open transactions.
 * You must commit or abort the transaction before you can begin a new one.
 * The commitTransaction() will flush any unsent records before committing the transaction.
 * If any of the send calls failed with an irrecoverable error, that means, even if a single message is not successfully delivered to Kafka,
 * the commitTransaction() call will throw the exception, and you are supposed to abort the whole transaction. And that's reasonable because that is what a transaction means,
 * either all or nothing. In a multithreaded producer implementation, you will call the send() API from different threads.
 * However, you must call the beginTransaction() before starting those threads and either commit or abort when all the threads are complete.
 * */

@Slf4j
public class TransactionalProducer {
    private static final String applicationID = "HelloProducer";
    private static final String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final String topicName1 = "hello-kafka-1";
    private static final String topicName2 = "hello-kafka-2";
    private static final int numEvents = 2;
    private static final String transaction_id = "Hello-Producer-Trans";

    public static void main(String[] args) {
        log.info("Creating Kafka Producer...");
        Properties properties = new Properties();
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, applicationID);
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, transaction_id);

        KafkaProducer<Integer, String> producer = new KafkaProducer<>(properties);
        producer.initTransactions();

        log.info("Starting First Transaction...");
        producer.beginTransaction();
        try {
            for (int i = 1; i <= numEvents; i++) {
                producer.send(new ProducerRecord<>(topicName1, i, "Hello Kafka-Commit-" + i));
                producer.send(new ProducerRecord<>(topicName2, i, "Hello Kafka-Commit-" + i));
            }
            log.info("Committing First Transaction.");
            producer.commitTransaction();
        } catch (Exception e){
            log.error("Exception in First Transaction. Aborting...");
            producer.abortTransaction();
            producer.close();
            throw new RuntimeException(e);
        }

        log.info("Starting Second Transaction...");
        producer.beginTransaction();
        try {
            for (int i = 1; i <= numEvents; i++) {
                producer.send(new ProducerRecord<>(topicName1, i, "Hello Kafka-Abort-" + i));
                producer.send(new ProducerRecord<>(topicName2, i, "Hello Kafka-Abort-" + i));
            }
            log.info("Aborting Second Transaction.");
            producer.abortTransaction();
        } catch (Exception e){
            log.error("Exception in Second Transaction. Aborting...");
            producer.abortTransaction();
            producer.close();
            throw new RuntimeException(e);
        }
        log.info("Finished - Closing Kafka Producer.");
        producer.close();
    }
}
