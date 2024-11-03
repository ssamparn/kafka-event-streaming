package com.spring.event.streaming.stream;

/* *
 * We created an in-memory state store for the RewardsApp. Now you might be wondering about the volatility of the in-memory state store.
 * What happens if the application or the machine crashed? When we restart our Kafka Streams application, what happens to the state store.
 * Fault tolerance and ability to recover from failure is one of the main concerns for the in-memory state store in Kafka Streams Applications.
 * However, for persistent state store, this might not appear to be a significant problem because persistent state stores are created using a local RocksDB database.
 * However, even for persistent state store, we have another concern of relocating or redistributing states from one machine to another machine.
 * The relocation or the redistribution of a state is required in 2 scenarios.
 *
 *   1. Assume, the application machine failed, and you lost the disk or the computer altogether. When it is not feasible to restart the same machine, you may quickly start your Kafka Streams application on a different machine.
 * However, what happens to the states that you lost with your computer. Those states were local on the faulty device. How do you recover your RocksDB from the failed machine?
 *
 *   2. Let's have a look at the 2nd scenario. You have a small application, and hence you started a single instance of your Kafka Streams application. The application subscribes to a topic
 * and begins processing all the data from all the partitions of the topic. However, your business is growing, and now you have a requirement to scale up your application.
 * Hence, you decided to start 2 more instances. Kafka Streams is smart enough to redistribute the workload among 3 instances of the application automatically.
 * However, initially, all the states were maintained locally on the first machine. We also have a requirement to redistribute or relocate the corresponding states from the first machine to the new machines.
 *
 * The problem is straightforward. We must be able to relocate state stores to achieve fault tolerance and scalability.
 * Whether you create an in-memory state store or a persistent state store, you do have a requirement to be able to automatically relocate the local states from one machine to another machine.
 *
 * Kafka Streams framework takes care of migrating your local states. How? How can Kafka provide fault tolerance for the local state store?
 * Well, that is so simple. To make state stores fault-tolerant and to allow for state store migration without data loss, a state store is continuously backed up to a Kafka topic behind the scenes.
 * This arrangement enables Kafka Streams to migrate a Stateful task from one machine to another computer. The internal backup topic is referred to as state store changelog.
 * By default, this feature is enabled. However, you can disable the state store backup feature. You can disable it while creating the state store with method withLoggingDisabled().

 * Since state store changelog is a Kafka topic, you can also apply Kafka topic configuration settings to customize the changelog topic.
 * Usually, we keep a low retention period for the changelog, and instead of deleting, we apply compaction to keep the most recent values.
 *
 * One final note: Even though Kafka Streams allows you to disable changelog, you may not want to disable it without serious consideration,
 * because doing so will remove fault tolerance and rebalancing capability from your application.
 * */
public class StateStoreFaultTolerance {

}
