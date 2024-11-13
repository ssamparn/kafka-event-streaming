package com.spring.event.streaming.serde;

import com.spring.event.streaming.generated.DepartmentAggregate;
import com.spring.event.streaming.generated.Employee;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import java.util.HashMap;
import java.util.Map;

/* *
 * Factory class for Serdes
 * */
public class AppSerdes extends Serdes {

    static final class EmployeeSerde extends WrapperSerde<Employee> {
        EmployeeSerde() {
            super(new JsonSerializer<>(), new JsonDeserializer<>());
        }
    }

    public static Serde<Employee> Employee() {
        EmployeeSerde serde = new EmployeeSerde();

        Map<String, Object> serdeConfigs = new HashMap<>();
        serdeConfigs.put(JsonDeserializer.VALUE_CLASS_NAME_CONFIG, Employee.class);
        serde.configure(serdeConfigs, false);

        return serde;
    }

    static final class DepartmentAggregateSerde extends WrapperSerde<DepartmentAggregate> {
        DepartmentAggregateSerde() {
            super(new JsonSerializer<>(), new JsonDeserializer<>());
        }
    }

    public static Serde<DepartmentAggregate> DepartmentAggregate() {
        DepartmentAggregateSerde serde = new DepartmentAggregateSerde();

        Map<String, Object> serdeConfigs = new HashMap<>();
        serdeConfigs.put(JsonDeserializer.VALUE_CLASS_NAME_CONFIG, DepartmentAggregate.class);
        serde.configure(serdeConfigs, false);

        return serde;
    }

}
