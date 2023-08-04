package com.kafka.streams.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.streams.domain.Greeting;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class GreetingSerializer implements Serializer<Greeting> {

    private final ObjectMapper objectMapper;

    public GreetingSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public byte[] serialize(String topic, Greeting greeting) {
        try {
            return objectMapper.writeValueAsBytes(greeting);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException : {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("Exception : {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


}
