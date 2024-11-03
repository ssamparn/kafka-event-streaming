package com.spring.event.streaming.avro.producer;

import com.spring.event.streaming.avro.datagenerator.InvoiceGenerator;
import com.spring.event.streaming.avro.generated.PosInvoice;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class PosSimulatorRunnable implements Runnable {
    private final AtomicBoolean stopper = new AtomicBoolean(false);
    private KafkaProducer<String, PosInvoice> kafkaProducer;
    private String topicName;
    private InvoiceGenerator invoiceGenerator;
    private int produceSpeed;
    private int id;

    PosSimulatorRunnable(final int id,
                     final KafkaProducer<String, PosInvoice> kafkaProducer,
                     final String topicName,
                     final int produceSpeed) {
        this.id = id;
        this.kafkaProducer = kafkaProducer;
        this.topicName = topicName;
        this.produceSpeed = produceSpeed;
        this.invoiceGenerator = InvoiceGenerator.getInstance();
    }

    @Override
    public void run() {
        try {
            log.info("Starting producer thread - {}", id);
            while (!stopper.get()) {
                PosInvoice posInvoice = invoiceGenerator.getNextInvoice();
                kafkaProducer.send(new ProducerRecord<>(topicName, posInvoice.getStoreID(), posInvoice));
                Thread.sleep(produceSpeed);
            }

        } catch (Exception e) {
            log.error("Exception in Producer thread - {}", id);
            throw new RuntimeException(e);
        }

    }

    void shutdown() {
        log.info("Shutting down producer thread - {}", id);
        stopper.set(true);
    }
}
