package com.kafka.streams.stateful.producer;

import lombok.extern.slf4j.Slf4j;

import static com.kafka.streams.stateful.utils.ProducerUtil.publishMessageSync;
import static com.kafka.streams.stateful.utils.StatefulKafkaStreamsUtil.WINDOW_WORDS_TOPIC;
import static java.lang.Thread.sleep;

@Slf4j
public class WindowsMockDataProduer {

    public static void main(String[] args) throws InterruptedException {
        //bulkMockDataProducer();
        bulkMockDataProducer_SlidingWindows();
    }

    private static void bulkMockDataProducer() throws InterruptedException {
        var key = "A";
        var word = "Apple";
        int count = 0;
        while(count < 100){
            var recordMetaData = publishMessageSync(WINDOW_WORDS_TOPIC, key,word);
            log.info("Published the alphabet message : {} ", recordMetaData);
            sleep(1000);
            count++;
        }
    }

    private static void bulkMockDataProducer_SlidingWindows() throws InterruptedException {
        String key = "A";
        String word = "Apple";
        int count = 0;
        while (count < 10) {
            var recordMetaData = publishMessageSync(WINDOW_WORDS_TOPIC, key, word);
            log.info("Published the alphabet message : {} ", recordMetaData);
            sleep(1000);
            count ++;
        }
    }
}
