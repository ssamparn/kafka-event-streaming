package com.spring.event.streaming.stream;

import com.spring.event.streaming.generated.DepartmentAggregate;
import com.spring.event.streaming.generated.Employee;
import com.spring.event.streaming.serde.AppSerdes;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedTable;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;

import java.util.Properties;

/* *
 * While computing the department wise average salary in the kstream-aggregate module, we realized that some use cases do not fit into a KStream aggregation.
 * If your use case is an update stream, you must model your solution using a KTable aggregate. Let’s rewrite the salary average by department using KTable.
 *
 * Problem Statement:
 *  Create a Kafka topic as "employees" and send below employee messages to the topic.
 *  Below are some sample messages:
 *    101:{"id": "101", "name": "Sashank", "department": "engineering", "salary": 5000}
 *    102:{"id": "102", "name": "John", "department": "accounts", "salary": 8000}
 *    103:{"id": "103", "name": "Abdul", "department": "engineering", "salary": 9000}
 *    104:{"id": "104", "name": "Donald", "department": "mathematics", "salary": 7000}
 *    105:{"id": "105", "name": "Jimmy", "department": "support", "salary": 6000}
 *
 * We want to re-implement the department wise salary average calculation using a KTable.
 * Earlier (see kstream-aggregate module), we implemented it using KStream and realized that people may get transferred from one department to another.
 * In that case, the employee information is being updated. And hence, any aggregates that were computed on employee information should also be updated.
 * Let’s recreate the solution, which is based on the earlier example.
 * We start by consuming the employee records in a KTable. Then we groupBy() this table on the department id.
 *
 * You should notice one key point in this groupBy() method on the KTable. This is the main difference between groupBy() of a KStream and of an KTable.
 * The groupBy() on KTable returns a KeyValuepair(), whereas groupBy() on KStream returned the key and automatically preserved the same value. Just highlighting this as a difference.
 * Once the KTable is grouped, we are ready to apply the aggregate(). The aggregation on the table is different than the aggregate() on KStream.
 * The KTable aggregate() takes 4 arguments. 1. Initializer 2. Aggregator (Adder) 3. Aggregator (Subtractor) and a 4. Serializer.
 * We have already seen the initializer and aggregator in KStream. Both work for the KTable in the same manner, and the code is also identical.
 * The initializer is to define zeros to initialize the state store, right? In the adder aggregator, we increment the count, add the salary to the total salary, and re-compute the average. That’s all.
 * The subtractor aggregator is precisely opposite to the adder. That means, we decrease the count, we subtract the salary from the total salary, and finally, re-compute the average. You already know the serializer.
 *
 * Now let’s try to understand how the adder and the subtractor are going to handle the update scenario. We initially had five records. Right? When we grouped them on department-id, the groupBy() creates 5 records.
 * All these records are new, so they are passed to the adder, and we manage to compute the average. Then we received one new event for employee_id 101. This time the department name is changed from engineering to support.
 *
 * 101:{"id": "101", "name": "Sashank", "department": "support", "salary": 5000}
 * 104:{"id": "104", "name": "Donald", "department": "accounts", "salary": 7000}
 *
 * The groupBy() knows that this new record is an update because we already have a record for 101. So, the groupBy will treat it as a delete and an insert operation.
 * What I mean is delete the old record and add a new record. As a result of this approach, the groupBy() would generate two records in the group.
 * One new record for employee 101 is added to the Support department. That’s an insert. And at the same time, an old record for employee 101 is removed from the Engineering department. That’s a delete.
 * Now, the aggregate() will call the adder method with the added record and also call the subtractor method with the removed record.
 * This mechanism allows us to adjust our aggregate for new records while entering into the group as well as old records while leaving the group.
 * The interface for implementing the adder and subtractor is the same. However, in the adder, we increase the average, and in the subtractor, we decrease the average.
 * That’s all.
 * */
@Slf4j
public class KTableAggregateApp {

    private static final  String applicationID = "KTableAggregateDemo";
    private static final  String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final  String topicName = "employees";
    private static final  String stateStoreLocation = "tmp/state-store";

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationID);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(StreamsConfig.STATE_DIR_CONFIG, stateStoreLocation);
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100);

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // employee source KTable
        KTable<String, Employee> employeeSourceKTable = streamsBuilder.table(topicName, Consumed.with(AppSerdes.String(), AppSerdes.Employee()));

        // grouping by department
        KGroupedTable<String, Employee> employeeKGroupedTableBasedOnDepartment = employeeSourceKTable
                .groupBy((id, employee) -> new KeyValue<>(employee.getDepartment(), employee), Grouped.with(AppSerdes.String(), AppSerdes.Employee()));

        // aggregate the employee grouped ktable based on department
        employeeKGroupedTableBasedOnDepartment
                .aggregate(
                        // initializer
                        () -> new DepartmentAggregate().withEmployeeCount(0).withTotalSalary(0).withAvgSalary(0D),
                        // adder
                        (key, value, aggregatedValue) -> {
                            DepartmentAggregate departmentAggregate = new DepartmentAggregate();
                            departmentAggregate.setEmployeeCount(aggregatedValue.getEmployeeCount() + 1);
                            departmentAggregate.setTotalSalary(aggregatedValue.getTotalSalary() + value.getSalary());
                            departmentAggregate.setAvgSalary((aggregatedValue.getTotalSalary() + value.getSalary()) / (aggregatedValue.getEmployeeCount() + 1D));
                            return departmentAggregate;
                        },
                        // subtractor
                        (key, value, aggregatedValue) -> {
                            DepartmentAggregate departmentAggregate = new DepartmentAggregate();
                            departmentAggregate.setEmployeeCount(aggregatedValue.getEmployeeCount() - 1);
                            departmentAggregate.setTotalSalary(aggregatedValue.getTotalSalary() - value.getSalary());
                            departmentAggregate.setAvgSalary((aggregatedValue.getTotalSalary() - value.getSalary()) / (aggregatedValue.getEmployeeCount() - 1D));
                            return departmentAggregate;
                        },
                        // serializer
                        Materialized.with(AppSerdes.String(), AppSerdes.DepartmentAggregate()))
                .toStream().print(Printed.<String, DepartmentAggregate>toSysOut().withLabel("Avg. Salary based on Department"));

        KafkaStreams streams = new KafkaStreams(streamsBuilder.build(), properties);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stopping Streams");
            streams.close();
        }));
    }
}
