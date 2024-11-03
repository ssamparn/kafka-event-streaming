
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
    "CustomerCardNo",
    "TotalAmount",
    "NumberOfItem",
    "PaymentMethod",
    "TaxableAmount",
    "CGST",
    "SGCT",
    "CESS",
    "InvoiceLineItems"
})
@Generated("jsonschema2pojo")
public class Invoice {

    @JsonProperty("InvoiceNumber")
    private String invoiceNumber;
    @JsonProperty("CreatedTime")
    private Long createdTime;
    @JsonProperty("CustomerCardNo")
    private String customerCardNo;
    @JsonProperty("TotalAmount")
    private Double totalAmount;
    @JsonProperty("NumberOfItem")
    private Integer numberOfItem;
    @JsonProperty("PaymentMethod")
    private String paymentMethod;
    @JsonProperty("TaxableAmount")
    private Double taxableAmount;
    @JsonProperty("CGST")
    private Double cgst;
    @JsonProperty("SGCT")
    private Double sgct;
    @JsonProperty("CESS")
    private Double cess;
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

    @JsonProperty("NumberOfItem")
    public Integer getNumberOfItem() {
        return numberOfItem;
    }

    @JsonProperty("NumberOfItem")
    public void setNumberOfItem(Integer numberOfItem) {
        this.numberOfItem = numberOfItem;
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

    @JsonProperty("SGCT")
    public Double getSgct() {
        return sgct;
    }

    @JsonProperty("SGCT")
    public void setSgct(Double sgct) {
        this.sgct = sgct;
    }

    @JsonProperty("CESS")
    public Double getCess() {
        return cess;
    }

    @JsonProperty("CESS")
    public void setCess(Double cess) {
        this.cess = cess;
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
        sb.append(Invoice.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("invoiceNumber");
        sb.append('=');
        sb.append(((this.invoiceNumber == null)?"<null>":this.invoiceNumber));
        sb.append(',');
        sb.append("createdTime");
        sb.append('=');
        sb.append(((this.createdTime == null)?"<null>":this.createdTime));
        sb.append(',');
        sb.append("customerCardNo");
        sb.append('=');
        sb.append(((this.customerCardNo == null)?"<null>":this.customerCardNo));
        sb.append(',');
        sb.append("totalAmount");
        sb.append('=');
        sb.append(((this.totalAmount == null)?"<null>":this.totalAmount));
        sb.append(',');
        sb.append("numberOfItem");
        sb.append('=');
        sb.append(((this.numberOfItem == null)?"<null>":this.numberOfItem));
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
        sb.append("sgct");
        sb.append('=');
        sb.append(((this.sgct == null)?"<null>":this.sgct));
        sb.append(',');
        sb.append("cess");
        sb.append('=');
        sb.append(((this.cess == null)?"<null>":this.cess));
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
