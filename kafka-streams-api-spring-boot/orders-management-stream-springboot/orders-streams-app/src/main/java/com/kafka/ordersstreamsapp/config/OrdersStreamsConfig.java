package com.kafka.ordersstreamsapp.config;

import com.kafka.ordersstreamsapp.exceptionhandler.StreamsProcessorCustomErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.streams.RecoveringDeserializationExceptionHandler;

import java.util.Map;

@Slf4j
@Configuration
public class OrdersStreamsConfig {

    public static final String ORDERS_TOPIC = "orders";
    public static final String GENERAL_ORDERS_TOPIC = "general-orders";
    public static final String GENERAL_ORDERS_COUNT_TOPIC = "general-orders-count";
    public static final String GENERAL_ORDERS_COUNT_WINDOWS_TOPIC = "general-orders-count-window";
    public static final String GENERAL_ORDERS_REVENUE_TOPIC = "general-orders-revenue";
    public static final String GENERAL_ORDERS_REVENUE_WINDOWS_TOPIC = "general-orders-revenue-window";

    public static final String RESTAURANT_ORDERS_TOPIC = "restaurant-orders";
    public static final String RESTAURANT_ORDERS_COUNT_TOPIC = "restaurant-orders-count";
    public static final String RESTAURANT_ORDERS_REVENUE_TOPIC = "restaurant-orders-revenue";
    public static final String RESTAURANT_ORDERS_COUNT_WINDOWS_TOPIC = "restaurant-orders-count-window";
    public static final String RESTAURANT_ORDERS_REVENUE_WINDOWS_TOPIC = "restaurant-orders-revenue-window";
    public static final String STORES_TOPIC = "stores";

    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamConfig() {

        Map<String, Object> streamProperties = kafkaProperties.buildStreamsProperties();

        streamProperties.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, RecoveringDeserializationExceptionHandler.class);
        streamProperties.put(RecoveringDeserializationExceptionHandler.KSTREAM_DESERIALIZATION_RECOVERER, consumerRecordRecoverer);
        //streamProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new KafkaStreamsConfiguration(streamProperties);
    }

    @Bean
    public StreamsBuilderFactoryBeanConfigurer streamsBuilderFactoryBeanConfigurer(){
        log.info("Inside streamsBuilderFactoryBeanConfigurer");
        return factoryBean -> {
            factoryBean.setStreamsUncaughtExceptionHandler(new StreamsProcessorCustomErrorHandler());
        };
    }

    public DeadLetterPublishingRecoverer recoverer() {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> {
                    log.error("Exception in Deserializing the message : {} and the record is : {}", ex.getMessage(),record,  ex);
                    return new TopicPartition("recoverer-dlq", record.partition());
                });
    }

    private ConsumerRecordRecoverer consumerRecordRecoverer = (record, exception) -> {
        log.error("Exception is : {} Failed Record : {} ", exception, record);
    };

    @Bean
    public NewTopic topicBuilder() {
        return TopicBuilder.name(ORDERS_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();

    }

    @Bean
    public NewTopic storeTopicBuilder() {
        return TopicBuilder.name(STORES_TOPIC)
                .partitions(2)
                .replicas(1)
                .build();

    }
}
