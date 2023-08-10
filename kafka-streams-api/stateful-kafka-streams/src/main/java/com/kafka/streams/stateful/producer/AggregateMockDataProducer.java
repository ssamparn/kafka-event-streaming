package com.kafka.streams.stateful.producer;

import lombok.extern.slf4j.Slf4j;

import static com.kafka.streams.stateful.utils.ProducerUtil.publishMessageSync;
import static com.kafka.streams.stateful.utils.StatefulKafkaStreamsUtil.AGGREGATE_TOPIC;

@Slf4j
public class AggregateMockDataProducer {

    public static void main(String[] args) {

        var aKey = "A";

        var aWord1 = "Apple";
        var aWord2 = "Alligator";
        var aWord3 = "Ambulance";

        var recordMetaData1 = publishMessageSync(AGGREGATE_TOPIC, aKey, aWord1);
        log.info("Published the alphabet message : {} ", recordMetaData1);

        var recordMetaData2 = publishMessageSync(AGGREGATE_TOPIC, aKey, aWord2);
        log.info("Published the alphabet message : {} ", recordMetaData2);

        var recordMetaData3 = publishMessageSync(AGGREGATE_TOPIC, aKey, aWord3);
        log.info("Published the alphabet message : {} ", recordMetaData3);

        var bKey = "B";
        var bWord1 = "Bus";
        var bWord2 = "Baby";

        var recordMetaData4 = publishMessageSync(AGGREGATE_TOPIC, bKey,bWord1);
        log.info("Published the alphabet message : {} ", recordMetaData4);

        var recordMetaData5 = publishMessageSync(AGGREGATE_TOPIC, bKey,bWord2);
        log.info("Published the alphabet message : {} ", recordMetaData5);
    }
}
