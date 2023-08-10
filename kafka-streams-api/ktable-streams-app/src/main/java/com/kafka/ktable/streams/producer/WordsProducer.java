package com.kafka.ktable.streams.producer;

import lombok.extern.slf4j.Slf4j;

import static com.kafka.ktable.streams.utils.KTableStreamUtil.WORDS_TOPIC;
import static com.kafka.ktable.streams.utils.ProducerUtil.publishMessageSync;

@Slf4j
public class WordsProducer {

    public static void main(String[] args) {
        String akey = "A";

        var aWord = "Apple";
        var aWord1 = "Alligator";
        var aWord2 = "Ambulance";

        var recordMetaData = publishMessageSync(WORDS_TOPIC, akey, aWord);
        log.info("Published the alphabet message : {} ", recordMetaData);

        var recordMetaData1 = publishMessageSync(WORDS_TOPIC, akey, aWord1);
        log.info("Published the alphabet message : {} ", recordMetaData1);

        var recordMetaData2 = publishMessageSync(WORDS_TOPIC, akey, aWord2);
        log.info("Published the alphabet message : {} ", recordMetaData2);

        var bKey = "B";
        var bWord1 = "Bus";
        var bWord2 = "Baby";

        var recordMetaData3 = publishMessageSync(WORDS_TOPIC, bKey, bWord1);
        log.info("Published the alphabet message : {} ", recordMetaData3);

        var recordMetaData4 = publishMessageSync(WORDS_TOPIC, bKey, bWord2);
        log.info("Published the alphabet message : {} ", recordMetaData4);
    }
}
