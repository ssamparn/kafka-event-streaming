
package com.spring.event.streaming.generated;

import javax.annotation.processing.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "totalSalary",
    "employeeCount",
    "avgSalary"
})
@Generated("jsonschema2pojo")
public class DepartmentAggregate {

    @JsonProperty("totalSalary")
    private Integer totalSalary;
    @JsonProperty("employeeCount")
    private Integer employeeCount;
    @JsonProperty("avgSalary")
    private Double avgSalary;

    @JsonProperty("totalSalary")
    public Integer getTotalSalary() {
        return totalSalary;
    }

    @JsonProperty("totalSalary")
    public void setTotalSalary(Integer totalSalary) {
        this.totalSalary = totalSalary;
    }

    public DepartmentAggregate withTotalSalary(Integer totalSalary) {
        this.totalSalary = totalSalary;
        return this;
    }

    @JsonProperty("employeeCount")
    public Integer getEmployeeCount() {
        return employeeCount;
    }

    @JsonProperty("employeeCount")
    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public DepartmentAggregate withEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
        return this;
    }

    @JsonProperty("avgSalary")
    public Double getAvgSalary() {
        return avgSalary;
    }

    @JsonProperty("avgSalary")
    public void setAvgSalary(Double avgSalary) {
        this.avgSalary = avgSalary;
    }

    public DepartmentAggregate withAvgSalary(Double avgSalary) {
        this.avgSalary = avgSalary;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(DepartmentAggregate.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("totalSalary");
        sb.append('=');
        sb.append(((this.totalSalary == null)?"<null>":this.totalSalary));
        sb.append(',');
        sb.append("employeeCount");
        sb.append('=');
        sb.append(((this.employeeCount == null)?"<null>":this.employeeCount));
        sb.append(',');
        sb.append("avgSalary");
        sb.append('=');
        sb.append(((this.avgSalary == null)?"<null>":this.avgSalary));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

}
