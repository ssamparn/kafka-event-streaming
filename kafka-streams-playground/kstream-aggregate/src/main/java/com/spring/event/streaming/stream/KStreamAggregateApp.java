package com.spring.event.streaming.stream;

import com.spring.event.streaming.generated.DepartmentAggregate;
import com.spring.event.streaming.generated.Employee;
import com.spring.event.streaming.serde.AppSerdes;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;

import java.util.Properties;

/* *
 * Problem Statement: Create a Kafka topic as "employees" and send below employee messages to the topic.
 *
 *  Below are some sample messages:
 *
 *    101:{"id": "101", "name": "Sashank", "department": "engineering", "salary": 5000}
 *    102:{"id": "102", "name": "John", "department": "accounts", "salary": 8000}
 *    103:{"id": "103", "name": "Abdul", "department": "engineering", "salary": 9000}
 *    104:{"id": "104", "name": "Donald", "department": "mathematics", "salary": 7000}
 *    105:{"id": "105", "name": "Jimmy", "department": "support", "salary": 6000}
 *
 * The number before the colon, such as 101, 102, must be sent as a key, and the rest of the text is a JSON message. These 5 records are for employees with their name, department, & salary.
 * And now we want to compute the average salary for each department. Simple. Isn’t it? You can visualize the messages as a table shown below.
 *     |  id  | name   | department  | salary |
 *     ----------------------------------------
 *     | 101 | Sashank | engineering | 5000 |
 *     | 102 | John    | accounts    | 8000 |
 *     | 103 | Abdul   | engineering | 9000 |
 *     | 104 | Donald  | mathematics | 5000 |
 *     | 105 | Jimmy   | accounts    | 4000 |
 *
 * You can compute the average salary by department using the following SQL,
 *     SELECT department, avg(salary)
 *     FROM table
 *     GROUP BY department;
 *
 * and you can expect the output, as shown in this table.
 *     | department  | avg. salary |
 *     |-------------|-------------|
 *     | engineering |   7000      |
 *     | accounts    |   6000      |
 *     | mathematics |   5000      |
 * How would you do it on a real-time stream? Let’s look at the solution. The solution is straightforward. We just need to group the stream on the department name and finally compute the average using the aggregate method.
 * We should be able to do it using the reduce() method.
 * The employee schema is straightforward. We have four fields, as specified in the problem statement.
 * The aggregate schema is a little different.
 * We need a department name and the average. The department name is going to be the key. So, we do not need it in the value. Hence, we do not have a department name in the aggregate schema.
 * Make sense? The next thing that we need is the average value. So, to calculate the average, we also need to maintain the total salary and the count.
 * We are not going to use an avg() function here as we use in SQL. The SQL function maintains totals and counts internally. In our case, we are using raw APIs, and we need to do everything ourselves.
 * So, let’s keep the total salary, the employee count and also a precomputed average value.
 *
 * Great!
 * */
@Slf4j
public class KStreamAggregateApp {

    private static final  String applicationID = "KStreamAggregateDemo";
    private static final  String bootstrapServers = "localhost:8082,localhost:8083,localhost:8084";
    private static final  String topicName = "employees";
    private static final  String stateStoreLocation = "tmp/state-store";
    
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationID);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(StreamsConfig.STATE_DIR_CONFIG, stateStoreLocation);

        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // employee source KStream
        KStream<String, Employee> employeeSourceKStream = streamsBuilder.stream(topicName, Consumed.with(AppSerdes.String(), AppSerdes.Employee()));

        // grouping by department
        KGroupedStream<String, Employee> employeeKGroupedStreamBasedOnDepartment = employeeSourceKStream.groupBy((id, employee) -> employee.getDepartment(), Grouped.with(AppSerdes.String(), AppSerdes.Employee()));

        // aggregate the employee grouped stream based on department
        employeeKGroupedStreamBasedOnDepartment
                .aggregate(
                        // initializer
                        () -> new DepartmentAggregate().withEmployeeCount(0).withTotalSalary(0).withAvgSalary(0D),
                        // aggregator
                        (key, value, aggregatedValue) ->
                            new DepartmentAggregate()
                                    .withEmployeeCount(aggregatedValue.getEmployeeCount() + 1)
                                    .withTotalSalary(aggregatedValue.getTotalSalary() + value.getSalary())
                                    .withAvgSalary((aggregatedValue.getTotalSalary() + value.getSalary()) / (aggregatedValue.getEmployeeCount() + 1D)),
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

    /* *
     * One critical consideration for real-time computing aggregates:
     * Computing accurate real-time aggregation requires careful design considerations.
     * We have looked at the 2 examples.
     *   1. Sum of loyalty point rewards by the customer.
     *   2. Average salary by the department.
     * In the first instance, both examples are reasonably good. However, they still do not cover the negative aggregation scenarios.
     * Let's try to understand it with examples. The sum() of the reward points for the customer is ever increasing as he/she continues to buy stuff from retail channels.
     * However, how would you reduce the reward value when the customer redeems his/her points? Take a pause and think about it?
     * A straightforward approach is to keep an additional field in the invoice that represents the redeemed reward points and subtract those points while computing aggregates.
     * Simple. Isn't it? However, such a straightforward solution may not apply in many other use cases.
     * The average salary by the department is an excellent example to showcase the negative aggregation problem.
     *
     * So, let`s start. You have already sent the 5 messages and computed the aggregation. But, what will happen when one employee in engineering swaps his department with another employee in support.
     * This swap generates two new events to the same Kafka topic. Now the department salary average should change, right? You can visualize the new state of employees by the department.
     * Now, if you compute the average using the same SQL, you would get the correct outcome. However, the current KStream aggregation code that we have written here, is going to produce an incorrect state.
     * If you send these 2 messages also to the current application, you will have a new state like this.
     *
     * Why did that happen? It happens due to the fundamental nature of a KStream. The KStream is not an update stream, and it assumes every message as an additional record.
     * Hence, for the KStream, there are now seven records that it received from the Kafka topic. We sent these five records earlier, and two were sent later. Right?
     * If you compute the average on this table, you will end up with the same incorrect results. The wrong result is not the outcome of the improper behavior of KStream.
     * The KStream is designed to behave in this manner. You can use KStream aggregation to produce correct results when your use case represents a pure stream.
     *
     * In other words, KStream behaves like an insert-only table where all the new records are appended. If your use case is an update stream, you must model your solution using a KTable.
     * The department-wise salary average example clearly depicts an update stream. Employee-id is the primary key, and a new record with the same employee-id must not be treated as an additional record, right?
     * Instead, the new record should update the earlier record for the same key. In such cases, you must use KTable to compute your aggregates.
     * We will fix this problem for the current example and rewrite the same example.
     * So have a clear understanding of the difference in KStream and KTable for aggregation purposes.
     *
     * KStream and KTable are 2 fundamental abstractions in Kafka Streams.
     * You must be selecting a suitable option to represent your streams.
     * You can have a simple thumb rule. If your scenario is to update records on a key, then you are talking about KTable.
     * If your situation needs an ever-growing append-only table, you are talking about a KStream.
     *
     * Great!!
     * */
}
