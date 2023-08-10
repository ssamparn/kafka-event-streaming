package com.kafka.streams.stateful;

import com.kafka.streams.stateful.topology.AggregateOperatorsTopology;
import com.kafka.streams.stateful.utils.StatefulKafkaStreamsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import java.util.List;
import java.util.Properties;

import static com.kafka.streams.stateful.utils.StatefulKafkaStreamsUtil.AGGREGATE_TOPIC;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;

@Slf4j
public class KafkaStreamAggregationApplication {

    public static void main(String[] args) {
//        Topology kTableTopology = AggregateOperatorsTopology.buildTopologyForCountWithGroupByKey();
//        Topology kTableTopology = AggregateOperatorsTopology.buildTopologyForCountWithGroupBy();
//        Topology kTableTopology = AggregateOperatorsTopology.buildTopologyForReduce();
        Topology kTableTopology = AggregateOperatorsTopology.buildTopologyForAggregate();

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "aggregate"); // consumer group
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        config.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StatefulKafkaStreamsUtil.createTopics(config, List.of(AGGREGATE_TOPIC));
        var kafkaStreams = new KafkaStreams(kTableTopology, config);

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));

        log.info("Starting Greeting streams");

        kafkaStreams.start();
    }
}
