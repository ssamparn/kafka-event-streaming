package com.kafka.order.consumer;

import com.coffee.order.domain.generated.CoffeeOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoffeeOrderConsumer {

    @KafkaListener(
            topics = {"coffee-orders"},
            autoStartup = "${coffeeOrdersConsumer.startup:true}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(ConsumerRecord<String, CoffeeOrder> consumerRecord) {
        log.info("ConsumerRecord key: {} , value: {} ", consumerRecord.key(), consumerRecord.value());
    }
}
