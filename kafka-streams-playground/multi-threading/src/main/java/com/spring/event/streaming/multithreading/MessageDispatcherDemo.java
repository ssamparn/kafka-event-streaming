package com.spring.event.streaming.multithreading;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/* *
 * Problem Statement:
 * Create a multithreaded Kafka producer that sends stock market data from a list of files to a Kafka topic, such that independent thread streams each file.
 * Assume you have multiple data files, and you want to send data from all those files to the Kafka Cluster.
 * So, basically what we want to do is to create one main thread that reads a bunch of data files and create one independent thread to process each data file.
 * For example, if you are supplying three data files to the application, it must create three threads, one for each data file.
 * Each thread is responsible for reading records from one file and sending them to Kafka cluster in parallel.
 * In all this, we do not want to create a bunch of Kafka producer instances.
 * As a recommended best practice, we want to share the same Kafka Producer Instance among all threads.
 *
 * V Imp Note: Kafka producer is thread-safe. So, your application can share the same producer object across multiple threads and send messages in parallel
 * using the same producer instance. It is not recommended to create numerous producer objects within the same application instance.
 * Sharing the same producer across the threads will be faster and less resource intensive.
 * */
@Slf4j
public class MessageDispatcherDemo {

    private static final String applicationID = "Multi-Threaded-Producer";
    private static final String topicName = "nse-eod";
    private static final String kafkaConfigFileLocation = "kafka-streams-playground/multi-threading/kafka.properties";
    private static final String[] eventFiles = { "kafka-streams-playground/multi-threading/data/NSE05NOV2018BHAV.csv", "kafka-streams-playground/multi-threading/data/NSE06NOV2018BHAV.csv"};

    public static void main(String[] args) {
        Properties properties = new Properties();
        try {
            InputStream inputStream = new FileInputStream(kafkaConfigFileLocation);
            properties.load(inputStream);
            properties.put(ProducerConfig.CLIENT_ID_CONFIG, applicationID);
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        KafkaProducer<Integer,String> kafkaProducer = new KafkaProducer<>(properties);
        Thread[] dispatchers = new Thread[eventFiles.length];

        log.info("Starting Dispatcher threads...");

        for (int i = 0; i < eventFiles.length; i++) {
            dispatchers[i] = new Thread(new MessageDispatcher(kafkaProducer, topicName, eventFiles[i]));
            dispatchers[i].start();
        }

        try {
            for (Thread thread : dispatchers)
                thread.join();
        } catch (InterruptedException e) {
            log.error("Main Thread Interrupted");
        } finally {
            kafkaProducer.close();
            log.info("Finished Dispatcher Demo");
        }
    }
}
