package com.spring.event.streaming.datagenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.event.streaming.generated.LineItem;

import java.io.File;
import java.util.Random;

class ProductGenerator {
    private static final ProductGenerator productGeneratorInstance = new ProductGenerator();
    private final Random random;
    private final Random qty;
    private final LineItem[] products;

    static ProductGenerator getInstance() {
        return productGeneratorInstance;
    }

    private ProductGenerator() {
        String productData = "kafka-streams-playground/pos-simulator/src/main/resources/data/products.json";
        ObjectMapper mapper = new ObjectMapper();
        random = new Random();
        qty = new Random();
        try {
            products = mapper.readValue(new File(productData), LineItem[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getIndex() {
        return random.nextInt(100);
    }

    private int getQuantity() {
        return qty.nextInt(2) + 1;
    }

    /* *
     * Generates a random product for the POS Invoice
     * */
    LineItem getNextProduct() {
        LineItem lineItem = products[getIndex()];
        lineItem.setItemQty(getQuantity());
        lineItem.setTotalValue(lineItem.getItemPrice() * lineItem.getItemQty());
        return lineItem;
    }
}
