package com.kafka.greetings.stream.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class GreetingsStreamSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(GreetingsStreamSpringbootApplication.class, args);
	}

}
