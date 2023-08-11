package com.kafka.streams.stateful.producer;

import com.kafka.streams.stateful.utils.ProducerUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.ArrayList;
import java.util.Map;

import static com.kafka.streams.stateful.utils.ProducerUtil.publishMessageSync;
import static com.kafka.streams.stateful.utils.ProducerUtil.publishMessageSyncWithDelay;
import static com.kafka.streams.stateful.utils.StatefulKafkaStreamsUtil.ALPHABETS_ABBREVATIONS_TOPIC;
import static com.kafka.streams.stateful.utils.StatefulKafkaStreamsUtil.ALPHABETS_TOPIC;
import static java.time.Instant.now;

@Slf4j
public class JoinsMockDataProducer {

    public static void main(String[] args) throws InterruptedException {
        Map<String, String> alphabetMap = Map.of(
                "A", "A is the first letter in English Alphabets.",
                "B", "B is the second letter in English Alphabets.",
                "C", "C is the third letter in English Alphabets.");

        publishMessages(alphabetMap, ALPHABETS_TOPIC);

        Map<String, String> alphabetAbbrevationMap;

        alphabetAbbrevationMap = Map.of(
                "A", "Apple",
                "B", "Bus",
                "C", "Cat"
        );
        publishMessages(alphabetAbbrevationMap, ALPHABETS_ABBREVATIONS_TOPIC);

//        alphabetAbbrevationMap = Map.of(
//                "A", "Airplane",
//                "B", "Baby"
//        );
//        publishMessages(alphabetAbbrevationMap, ALPHABETS_ABBREVATIONS_TOPIC);
    }

    private static void publishMessagesToSimulateGrace(Map<String, String> alphabetMap, String topicName, int delaySeconds) throws InterruptedException {
        var producerRecords = new ArrayList<ProducerRecord<String, String>>();
        alphabetMap
                .forEach((key, value)
                        -> producerRecords.add(new ProducerRecord<>(topicName, 0, now().toEpochMilli(), key, value)));

        Thread.sleep(delaySeconds* 1000L);
        publishMessageSync(producerRecords);
    }

    private static void publishMessagesWithDelay(Map<String, String> alphabetMap, String topic, int delaySeconds) {
        alphabetMap
                .forEach((key, value) -> {
                    var recordMetaData = publishMessageSyncWithDelay(topic, key, value, delaySeconds);
                    log.info("Published the alphabet message : {} ", recordMetaData);
                });
    }


    private static void publishMessages(Map<String, String> alphabetMap, String topic) {

        alphabetMap
                .forEach((key, value) -> {
                    var recordMetaData = publishMessageSync(topic, key, value);
                    log.info("Published the alphabet message : {} ", recordMetaData);
                });
    }


}
