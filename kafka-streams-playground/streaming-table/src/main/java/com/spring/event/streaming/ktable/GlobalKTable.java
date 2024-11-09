package com.spring.event.streaming.ktable;

import lombok.extern.slf4j.Slf4j;

/* *
 * Global KTable:
 * We understood the table capability of Kafka Streams API, the KTables. However, KTables are local.
 * What it means is simple. Each streams task would have their own local copy of the KTable and hence a local copy of the state store where the KTable data is persisted.
 * These local tables are great. Because they allow you to work on a partition of the data in parallel. In this scenario, each stream task can work independently without relying on other tasks.
 *
 * However, in some cases, you may need a global table. A global table is something that is available to all streams tasks. It is a common data set which anyone can read.
 * Any changes to this global table should be available to all stream tasks even if they are running on different machines. Kafka Streams offers this capability as GlobalKTable.
 * Like a KTable, a GlobalKTable is also an abstraction of an updated stream, where each data record represents an update or insert.
 * However, there is one fundamental difference between these two structures. A standard KTable is local in nature, whereas a GlobalKTable is global in nature.
 *
 * Let's try to understand what it means by being local and how it differs from being a global table.
 *
 * Assume you have a topic T5 with 5 partitions. You want to read this topic into a table. We already know that partitions are the main idea behind the parallel processing in Kafka.
 * You can run 5 instances of your application on topic T5 to achieve a maximum degree of parallelism. Assume you started 5 instances of the same application, and all of them subscribed to T5 and reading the data into a table.
 * In this scenario, each instance of the application will be assigned one partition, and the local KTable will be able to process data only from one assigned partition. This scenario is perfectly fine for the parallel processing of data.
 * Each instance is processing one partition, and all 5 instances together process all the data.
 * However, if you read the topic into GlobalKTable, each instance of the application will be assigned all the 5 partitions, and the GlobalKTable at each instance will read data from all the partitions,
 * and hence, all of them will possess all the data. This scenario is problematic for parallel processing because it causes duplicate processing.
 * However, GlobalKTable makes a perfect sense for broadcast and lookup tables.
 * GlobalKTable is mainly used in star-joins, foreign key lookups, and broadcast information to all running instances of your application.
 * However, you must be careful in using GlobalKTable as they require local storage on each instance of the application, and they also increase the network traffic and broker workload because all instances read the entire data.
 * GlobalKtable is excellent for a small set of information that you want to be available to all your instances.
 * Great!
 * */
@Slf4j
public class GlobalKTable {
}
