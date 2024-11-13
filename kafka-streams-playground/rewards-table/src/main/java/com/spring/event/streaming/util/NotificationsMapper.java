package com.spring.event.streaming.util;

import com.spring.event.streaming.generated.Notification;
import com.spring.event.streaming.generated.PosInvoice;

public class NotificationsMapper {

    private final static Double LOYALTY_FACTOR = 0.02;

    public static Notification getNotificationFrom(PosInvoice invoice) {
        Notification notification = new Notification();
        notification.setInvoiceNumber(invoice.getInvoiceNumber());
        notification.setCustomerCardNo(invoice.getCustomerCardNo());
        notification.setTotalAmount(invoice.getTotalAmount());
        notification.setEarnedLoyaltyPoints(invoice.getTotalAmount() * LOYALTY_FACTOR);
        notification.setTotalLoyaltyPoints(invoice.getTotalAmount() * LOYALTY_FACTOR);
        return notification;
    }
}