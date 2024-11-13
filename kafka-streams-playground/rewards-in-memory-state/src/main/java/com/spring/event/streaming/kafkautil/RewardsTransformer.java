package com.spring.event.streaming.kafkautil;

import com.spring.event.streaming.generated.Notification;
import com.spring.event.streaming.generated.PosInvoice;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

public class RewardsTransformer implements ValueTransformer<PosInvoice, Notification> {

    private final static Double LOYALTY_FACTOR = 0.02;
    private final static String REWARDS_STORE = "CustomerRewardsStore";
    private KeyValueStore<String, Double> stateStore;

    @Override
    public void init(ProcessorContext processorContext) {
        this.stateStore = processorContext.getStateStore(REWARDS_STORE);
    }

    /* *
     * transform() is called for each kafka message
     * */
    @Override
    public Notification transform(PosInvoice posInvoice) {
        Notification notification = new Notification();
        notification.setInvoiceNumber(posInvoice.getInvoiceNumber());
        notification.setCustomerCardNo(posInvoice.getCustomerCardNo());
        notification.setTotalAmount(posInvoice.getTotalAmount());
        notification.setEarnedLoyaltyPoints(posInvoice.getTotalAmount() * LOYALTY_FACTOR);
        notification.setTotalLoyaltyPoints(0.0);

        Double accumulatedRewards = stateStore.get(notification.getCustomerCardNo());
        Double totalRewards;
        if (accumulatedRewards != null) {
            totalRewards = accumulatedRewards + notification.getEarnedLoyaltyPoints();
        } else {
            totalRewards = notification.getEarnedLoyaltyPoints();
        }

        stateStore.put(notification.getCustomerCardNo(), totalRewards);
        notification.setTotalLoyaltyPoints(totalRewards);
        return notification;
    }

    @Override
    public void close() {

    }
}
