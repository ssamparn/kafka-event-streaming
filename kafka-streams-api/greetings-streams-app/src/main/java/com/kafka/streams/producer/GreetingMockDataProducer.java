package com.kafka.streams.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kafka.streams.domain.Greeting;
import com.kafka.streams.util.GreetingsStreamUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

import static com.kafka.streams.util.GreetingsStreamUtil.publishMessageSync;


@Slf4j
public class GreetingMockDataProducer {

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        englishGreetings(objectMapper);
//        erroneousEnglishGreetings(objectMapper);
        spanishGreetings(objectMapper);
    }

    private static void englishGreetings(ObjectMapper objectMapper) {
        var englishGreetings = List.of(
            new Greeting("Hello, Good Morning!", LocalDateTime.now()),
                new Greeting("Hello, Good Evening!", LocalDateTime.now()),
                new Greeting("Hello, Good Night!", LocalDateTime.now())
        );
        englishGreetings
                .forEach(greeting -> {
                    try {
                        var greetingJSON = objectMapper.writeValueAsString(greeting);
                        var recordMetaData = publishMessageSync(GreetingsStreamUtil.SOURCE_TOPIC, null, greetingJSON);
                        log.info("Published the alphabet message : {} ", recordMetaData);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static void spanishGreetings(ObjectMapper objectMapper) {
        var spanishGreetings = List.of(
                new Greeting("¡Hola buenos dias!", LocalDateTime.now()),
                new Greeting("¡Hola buenas tardes!", LocalDateTime.now()),
                new Greeting("¡Hola, buenas noches!", LocalDateTime.now())
        );
        spanishGreetings
                .forEach(greeting -> {
                    try {
                        var greetingJSON = objectMapper.writeValueAsString(greeting);
                        var recordMetaData = publishMessageSync(GreetingsStreamUtil.SOURCE_TOPIC_SPANISH, null, greetingJSON);
                        log.info("Published the alphabet message : {} ", recordMetaData);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static void erroneousEnglishGreetings(ObjectMapper objectMapper) {
        var englishGreetings = List.of(
                new Greeting("Hello, Good Morning!", LocalDateTime.now()),
                new Greeting("Transient Error", LocalDateTime.now()),
                new Greeting("Hello, Good Night!", LocalDateTime.now())
        );
        englishGreetings
                .forEach(greeting -> {
                    try {
                        var greetingJSON = objectMapper.writeValueAsString(greeting);
                        var recordMetaData = publishMessageSync(GreetingsStreamUtil.SOURCE_TOPIC, null, greetingJSON);
                        log.info("Published the alphabet message : {} ", recordMetaData);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}

