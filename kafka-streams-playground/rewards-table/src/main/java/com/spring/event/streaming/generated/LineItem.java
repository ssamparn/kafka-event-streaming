
package com.spring.event.streaming.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ItemCode",
    "ItemDescription",
    "ItemPrice",
    "ItemQty",
    "TotalValue"
})
@Generated("jsonschema2pojo")
public class LineItem {

    @JsonProperty("ItemCode")
    private String itemCode;
    @JsonProperty("ItemDescription")
    private String itemDescription;
    @JsonProperty("ItemPrice")
    private Double itemPrice;
    @JsonProperty("ItemQty")
    private Integer itemQty;
    @JsonProperty("TotalValue")
    private Double totalValue;

    @JsonProperty("ItemCode")
    public String getItemCode() {
        return itemCode;
    }

    @JsonProperty("ItemCode")
    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    @JsonProperty("ItemDescription")
    public String getItemDescription() {
        return itemDescription;
    }

    @JsonProperty("ItemDescription")
    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    @JsonProperty("ItemPrice")
    public Double getItemPrice() {
        return itemPrice;
    }

    @JsonProperty("ItemPrice")
    public void setItemPrice(Double itemPrice) {
        this.itemPrice = itemPrice;
    }

    @JsonProperty("ItemQty")
    public Integer getItemQty() {
        return itemQty;
    }

    @JsonProperty("ItemQty")
    public void setItemQty(Integer itemQty) {
        this.itemQty = itemQty;
    }

    @JsonProperty("TotalValue")
    public Double getTotalValue() {
        return totalValue;
    }

    @JsonProperty("TotalValue")
    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(LineItem.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("itemCode");
        sb.append('=');
        sb.append(((this.itemCode == null)?"<null>":this.itemCode));
        sb.append(',');
        sb.append("itemDescription");
        sb.append('=');
        sb.append(((this.itemDescription == null)?"<null>":this.itemDescription));
        sb.append(',');
        sb.append("itemPrice");
        sb.append('=');
        sb.append(((this.itemPrice == null)?"<null>":this.itemPrice));
        sb.append(',');
        sb.append("itemQty");
        sb.append('=');
        sb.append(((this.itemQty == null)?"<null>":this.itemQty));
        sb.append(',');
        sb.append("totalValue");
        sb.append('=');
        sb.append(((this.totalValue == null)?"<null>":this.totalValue));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
