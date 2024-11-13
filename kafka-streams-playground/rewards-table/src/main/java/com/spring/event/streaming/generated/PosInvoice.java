
package com.spring.event.streaming.generated;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "InvoiceNumber",
    "CreatedTime",
    "StoreID",
    "PosID",
    "CashierID",
    "CustomerType",
    "CustomerCardNo",
    "TotalAmount",
    "NumberOfItems",
    "PaymentMethod",
    "TaxableAmount",
    "CGST",
    "SGST",
    "CESS",
    "DeliveryType",
    "DeliveryAddress",
    "InvoiceLineItems"
})
@Generated("jsonschema2pojo")
public class PosInvoice {

    @JsonProperty("InvoiceNumber")
    private String invoiceNumber;
    @JsonProperty("CreatedTime")
    private Long createdTime;
    @JsonProperty("StoreID")
    private String storeID;
    @JsonProperty("PosID")
    private String posID;
    @JsonProperty("CashierID")
    private String cashierID;
    @JsonProperty("CustomerType")
    private String customerType;
    @JsonProperty("CustomerCardNo")
    private String customerCardNo;
    @JsonProperty("TotalAmount")
    private Double totalAmount;
    @JsonProperty("NumberOfItems")
    private Integer numberOfItems;
    @JsonProperty("PaymentMethod")
    private String paymentMethod;
    @JsonProperty("TaxableAmount")
    private Double taxableAmount;
    @JsonProperty("CGST")
    private Double cgst;
    @JsonProperty("SGST")
    private Double sgst;
    @JsonProperty("CESS")
    private Double cess;
    @JsonProperty("DeliveryType")
    private String deliveryType;
    @JsonProperty("DeliveryAddress")
    private DeliveryAddress deliveryAddress;
    @JsonProperty("InvoiceLineItems")
    private List<LineItem> invoiceLineItems = new ArrayList<LineItem>();

    @JsonProperty("InvoiceNumber")
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    @JsonProperty("InvoiceNumber")
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    @JsonProperty("CreatedTime")
    public Long getCreatedTime() {
        return createdTime;
    }

    @JsonProperty("CreatedTime")
    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    @JsonProperty("StoreID")
    public String getStoreID() {
        return storeID;
    }

    @JsonProperty("StoreID")
    public void setStoreID(String storeID) {
        this.storeID = storeID;
    }

    @JsonProperty("PosID")
    public String getPosID() {
        return posID;
    }

    @JsonProperty("PosID")
    public void setPosID(String posID) {
        this.posID = posID;
    }

    @JsonProperty("CashierID")
    public String getCashierID() {
        return cashierID;
    }

    @JsonProperty("CashierID")
    public void setCashierID(String cashierID) {
        this.cashierID = cashierID;
    }

    @JsonProperty("CustomerType")
    public String getCustomerType() {
        return customerType;
    }

    @JsonProperty("CustomerType")
    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    @JsonProperty("CustomerCardNo")
    public String getCustomerCardNo() {
        return customerCardNo;
    }

    @JsonProperty("CustomerCardNo")
    public void setCustomerCardNo(String customerCardNo) {
        this.customerCardNo = customerCardNo;
    }

    @JsonProperty("TotalAmount")
    public Double getTotalAmount() {
        return totalAmount;
    }

    @JsonProperty("TotalAmount")
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @JsonProperty("NumberOfItems")
    public Integer getNumberOfItems() {
        return numberOfItems;
    }

    @JsonProperty("NumberOfItems")
    public void setNumberOfItems(Integer numberOfItems) {
        this.numberOfItems = numberOfItems;
    }

    @JsonProperty("PaymentMethod")
    public String getPaymentMethod() {
        return paymentMethod;
    }

    @JsonProperty("PaymentMethod")
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @JsonProperty("TaxableAmount")
    public Double getTaxableAmount() {
        return taxableAmount;
    }

    @JsonProperty("TaxableAmount")
    public void setTaxableAmount(Double taxableAmount) {
        this.taxableAmount = taxableAmount;
    }

    @JsonProperty("CGST")
    public Double getCgst() {
        return cgst;
    }

    @JsonProperty("CGST")
    public void setCgst(Double cgst) {
        this.cgst = cgst;
    }

    @JsonProperty("SGST")
    public Double getSgst() {
        return sgst;
    }

    @JsonProperty("SGST")
    public void setSgst(Double sgst) {
        this.sgst = sgst;
    }

    @JsonProperty("CESS")
    public Double getCess() {
        return cess;
    }

    @JsonProperty("CESS")
    public void setCess(Double cess) {
        this.cess = cess;
    }

    @JsonProperty("DeliveryType")
    public String getDeliveryType() {
        return deliveryType;
    }

    @JsonProperty("DeliveryType")
    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    @JsonProperty("DeliveryAddress")
    public DeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    @JsonProperty("DeliveryAddress")
    public void setDeliveryAddress(DeliveryAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    @JsonProperty("InvoiceLineItems")
    public List<LineItem> getInvoiceLineItems() {
        return invoiceLineItems;
    }

    @JsonProperty("InvoiceLineItems")
    public void setInvoiceLineItems(List<LineItem> invoiceLineItems) {
        this.invoiceLineItems = invoiceLineItems;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(PosInvoice.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("invoiceNumber");
        sb.append('=');
        sb.append(((this.invoiceNumber == null)?"<null>":this.invoiceNumber));
        sb.append(',');
        sb.append("createdTime");
        sb.append('=');
        sb.append(((this.createdTime == null)?"<null>":this.createdTime));
        sb.append(',');
        sb.append("storeID");
        sb.append('=');
        sb.append(((this.storeID == null)?"<null>":this.storeID));
        sb.append(',');
        sb.append("posID");
        sb.append('=');
        sb.append(((this.posID == null)?"<null>":this.posID));
        sb.append(',');
        sb.append("cashierID");
        sb.append('=');
        sb.append(((this.cashierID == null)?"<null>":this.cashierID));
        sb.append(',');
        sb.append("customerType");
        sb.append('=');
        sb.append(((this.customerType == null)?"<null>":this.customerType));
        sb.append(',');
        sb.append("customerCardNo");
        sb.append('=');
        sb.append(((this.customerCardNo == null)?"<null>":this.customerCardNo));
        sb.append(',');
        sb.append("totalAmount");
        sb.append('=');
        sb.append(((this.totalAmount == null)?"<null>":this.totalAmount));
        sb.append(',');
        sb.append("numberOfItems");
        sb.append('=');
        sb.append(((this.numberOfItems == null)?"<null>":this.numberOfItems));
        sb.append(',');
        sb.append("paymentMethod");
        sb.append('=');
        sb.append(((this.paymentMethod == null)?"<null>":this.paymentMethod));
        sb.append(',');
        sb.append("taxableAmount");
        sb.append('=');
        sb.append(((this.taxableAmount == null)?"<null>":this.taxableAmount));
        sb.append(',');
        sb.append("cgst");
        sb.append('=');
        sb.append(((this.cgst == null)?"<null>":this.cgst));
        sb.append(',');
        sb.append("sgst");
        sb.append('=');
        sb.append(((this.sgst == null)?"<null>":this.sgst));
        sb.append(',');
        sb.append("cess");
        sb.append('=');
        sb.append(((this.cess == null)?"<null>":this.cess));
        sb.append(',');
        sb.append("deliveryType");
        sb.append('=');
        sb.append(((this.deliveryType == null)?"<null>":this.deliveryType));
        sb.append(',');
        sb.append("deliveryAddress");
        sb.append('=');
        sb.append(((this.deliveryAddress == null)?"<null>":this.deliveryAddress));
        sb.append(',');
        sb.append("invoiceLineItems");
        sb.append('=');
        sb.append(((this.invoiceLineItems == null)?"<null>":this.invoiceLineItems));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
