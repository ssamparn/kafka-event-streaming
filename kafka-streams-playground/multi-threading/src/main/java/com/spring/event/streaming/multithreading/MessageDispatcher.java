package com.spring.event.streaming.multithreading;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@Slf4j
public class MessageDispatcher implements Runnable {

    private String fileLocation;
    private String topicName;
    private KafkaProducer<Integer, String> kafkaProducer;

    public MessageDispatcher(KafkaProducer<Integer, String> kafkaProducer, String topicName, String fileLocation) {
        this.fileLocation = fileLocation;
        this.topicName = topicName;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public void run() {
        log.info("Start Processing " + fileLocation);
        File file = new File(fileLocation);
        int counter = 0;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                kafkaProducer.send(new ProducerRecord<>(topicName, null, line));
                counter++;
            }
            log.info("Finished Sending " + counter + " messages from " + fileLocation);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
