package com.spring.event.streaming.datagenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.event.streaming.generated.DeliveryAddress;
import com.spring.event.streaming.generated.LineItem;
import com.spring.event.streaming.generated.PosInvoice;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class InvoiceGenerator {
    private static InvoiceGenerator invoiceGeneratorInstance = new InvoiceGenerator();
    private final Random invoiceIndex;
    private final Random invoiceNumber;
    private final Random numberOfItems;
    private final PosInvoice[] invoices;

    public static InvoiceGenerator getInstance() {
        return invoiceGeneratorInstance;
    }

    private InvoiceGenerator() {
        String invoiceData = "kafka-streams-playground/pos-simulator/src/main/resources/data/Invoice.json";
        ObjectMapper mapper;
        invoiceIndex = new Random();
        invoiceNumber = new Random();
        numberOfItems = new Random();
        mapper = new ObjectMapper();
        try {
            invoices = mapper.readValue(new File(invoiceData), PosInvoice[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int getIndex() {
        return invoiceIndex.nextInt(100);
    }

    private int getNewInvoiceNumber() {
        return invoiceNumber.nextInt(99999999) + 99999;
    }

    private int getNoOfItems() {
        return numberOfItems.nextInt(4) + 1;
    }

    /* *
     * Generates random POS invoice
     * */
    public PosInvoice getNextInvoice() {
        PosInvoice invoice = invoices[getIndex()];
        invoice.setInvoiceNumber(Integer.toString(getNewInvoiceNumber()));
        invoice.setCreatedTime(System.currentTimeMillis());
        if ("HOME-DELIVERY".equalsIgnoreCase(invoice.getDeliveryType())) {
            DeliveryAddress deliveryAddress = AddressGenerator.getInstance().getNextAddress();
            invoice.setDeliveryAddress(deliveryAddress);
        }
        int itemCount = getNoOfItems();
        Double totalAmount = 0.0;
        List<LineItem> items = new ArrayList<>();
        ProductGenerator productGenerator = ProductGenerator.getInstance();
        for (int i = 0; i < itemCount; i++) {
            LineItem item = productGenerator.getNextProduct();
            totalAmount = totalAmount + item.getTotalValue();
            items.add(item);
        }
        invoice.setNumberOfItems(itemCount);
        invoice.setInvoiceLineItems(items);
        invoice.setTotalAmount(totalAmount);
        invoice.setTaxableAmount(totalAmount);
        invoice.setCgst(totalAmount * 0.025);
        invoice.setSgst(totalAmount * 0.025);
        invoice.setCess(totalAmount * 0.00125);
        log.debug(String.valueOf(invoice));
        return invoice;
    }
}
