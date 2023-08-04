package com.kafka.order.producer;

import com.coffee.order.domain.generated.CoffeeOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class CoffeeOrderProducer {

    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    private KafkaTemplate<String, CoffeeOrder> kafkaTemplate;

    public CoffeeOrderProducer(KafkaTemplate<String, CoffeeOrder> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(CoffeeOrder coffeeOrderAvroMessage) {
        ProducerRecord<String, CoffeeOrder> producerRecord = new ProducerRecord<>(topicName, coffeeOrderAvroMessage.getId().toString(), coffeeOrderAvroMessage);

        CompletableFuture<SendResult<String, CoffeeOrder>> completableFuture = kafkaTemplate.send(producerRecord);
        completableFuture.whenComplete((successResult, exception) -> {
            if (exception == null) {
                handleSuccess(successResult, coffeeOrderAvroMessage);
            } else {
                handleFailure(exception, coffeeOrderAvroMessage);
            }
        });
    }

    private void handleFailure(Throwable ex, CoffeeOrder coffeeOrder) {
        log.error("Error while sending the Message for {} and the exception is {}", coffeeOrder,ex.getMessage(), ex);
        try {
            throw ex;
        } catch (Throwable throwable) {
            log.error("Error in OnFailure: {}", throwable.getMessage());
        }
    }

    private void handleSuccess(SendResult<String, CoffeeOrder> result, CoffeeOrder coffeeOrder) {
        log.info("Message Sent SuccessFully for the key : {} and the value is {} , partition is {}", coffeeOrder.getId(), coffeeOrder, result.getRecordMetadata().partition());
    }
}
