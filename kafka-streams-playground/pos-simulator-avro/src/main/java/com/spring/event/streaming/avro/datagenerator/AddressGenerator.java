package com.spring.event.streaming.avro.datagenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.event.streaming.avro.generated.DeliveryAddress;

import java.io.File;
import java.util.Random;

class AddressGenerator {
    private static final AddressGenerator addressGeneratorInstance = new AddressGenerator();
    private final Random random;

    private DeliveryAddress[] addresses;

    private int getIndex() {
        return random.nextInt(100);
    }

    static AddressGenerator getInstance() {
        return addressGeneratorInstance;
    }

    private AddressGenerator() {
        final String addressData = "kafka-streams-playground/pos-simulator-avro/src/main/resources/data/address.json";
        final ObjectMapper mapper;
        random = new Random();
        mapper = new ObjectMapper();
        try {
            addresses = mapper.readValue(new File(addressData), DeliveryAddress[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* *
     * Generates a random address for the POS Invoice
     * */
    DeliveryAddress getNextAddress() {
        return addresses[getIndex()];
    }

}
