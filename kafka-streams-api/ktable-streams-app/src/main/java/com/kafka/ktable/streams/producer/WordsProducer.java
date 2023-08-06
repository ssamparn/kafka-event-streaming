package com.kafka.ktable.streams.producer;

import lombok.extern.slf4j.Slf4j;

import static com.kafka.ktable.streams.utils.KTableStreamUtil.WORDS_TOPIC;
import static com.kafka.ktable.streams.utils.ProducerUtil.publishMessageSync;

@Slf4j
public class WordsProducer {

    public static void main(String[] args) {
        String key = null;

        var word = "Apple";
        var word1 = "Alligator";
        var word2 = "Ambulance";

        var recordMetaData = publishMessageSync(WORDS_TOPIC, key, word);
        log.info("Published the alphabet message : {} ", recordMetaData);

        var recordMetaData1 = publishMessageSync(WORDS_TOPIC, key, word1);
        log.info("Published the alphabet message : {} ", recordMetaData1);

        var recordMetaData2 = publishMessageSync(WORDS_TOPIC, key, word2);
        log.info("Published the alphabet message : {} ", recordMetaData2);

        var bKey = "B";

        var bWord1 = "Bus";
        var bWord2 = "Baby";
        var recordMetaData3 = publishMessageSync(WORDS_TOPIC, bKey,bWord1);
        log.info("Published the alphabet message : {} ", recordMetaData2);

        var recordMetaData4 = publishMessageSync(WORDS_TOPIC, bKey,bWord2);
        log.info("Published the alphabet message : {} ", recordMetaData2);
    }
}
