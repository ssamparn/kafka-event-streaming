package com.kafka.ordersstreamsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@SpringBootApplication
public class OrdersStreamsAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrdersStreamsAppApplication.class, args);
	}

}
