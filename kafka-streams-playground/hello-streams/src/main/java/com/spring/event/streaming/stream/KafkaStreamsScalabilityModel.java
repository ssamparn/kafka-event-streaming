package com.spring.event.streaming.stream;

/* *
 * Kafka Streams Architecture:
 * Kafka Streams is built on top of Kafka Client APIs. So, the Streams API leverages the native capabilities of Apache Kafka to offer
 * data parallelism, distributed coordination, and fault tolerance.
 *
 * To understand Kafka Stream's parallelism, you must have a clear understanding of the following concepts.
 *  1. Multithreading - Vertical Scaling.
 *  2. Multiple Instances - Horizontal Scaling.
 *  3. Streams Topology
 *  4. Streams Task
 * Let's try to understand these ideas independently.
 *
 * A typical Streams application runs as a single-threaded application by default. However, you can create a multithreaded streams application quite easily.
 * All you have to do is to set a configuration property StreamsConfig.NUM_STREAM_THREADS_CONFIG.
 * With this config added, the same application starts running three 3 threads.
 * Kafka Streams allows the user to configure the number of threads. The library can use the configuration to parallelize the processing within an application instance automatically.
 * When running multiple threads, each thread executes one or more Stream Task. If you configure the number of threads as 10, your single instance of the application would start running 10 threads.
 * Each thread will execute one or more Streams Task, and your degree of parallelism would be 10 because you have 10 threads.
 *
 * An increasing number of threads is one way of vertically scaling your application. And configuring the number of threads is as simple as setting a property as shown here.
 *
 * Let's come to the horizontal scaling. You can scale your application horizontally by starting multiple Instances of your Stream Application.
 * As mentioned above, we can easily configure multiple threads for our application to increase the degree of parallelism. However, all those threads would be running within a single application.
 * The number of threads that you can start is limited by the available resources on a single machine. You can overcome this limitation by launching multiple instances of the same application on a different computer.
 * For example, you can start one application with five threads on one machine and start another instance with three threads on a different computer.
 * In that case, you will be able to achieve eight parallel processes. The point is straight. Scaling your stream processing application with Kafka Streams is easy.
 * You merely need to start new threads or instances or a combination of both, and Kafka Streams takes care of creating Streams Tasks that run in parallel.
 *
 * What is a Stream Task & How work is shared by parallel running Stream Tasks?
 * The secret of Streams Task is hidden behind the Topology.
 * So, let's try to understand the Topology and how it helps Kafka to create a Task.
 * When we write a Kafka Streams application, we define the computational logic using processor APIs which gets bundled into a Topology as explained earlier.
 * At runtime, The framework would create a set of logical stream task. Each of these tasks would create a copy of the Topology object and execute it in parallel.
 *
 * What does that mean? That means, starting more stream threads or more instances of the application are extremely simple for the Kafka Streams Framework.
 * All it needs to do is to get a new instance of the Topology and let it run by an independent Stream Task.
 *
 * Not clear yet. Let me walk you through an example.
 * Imagine a Kafka Streams application that consumes from two topics, T1 and T2, with each having 3 partitions.
 * The framework will look at these details, and it will create a fixed number of logical Tasks. The number of tasks is equal to the number of partitions in the input topic of the Topology.
 * In the case of multiple input topics, the number of tasks is equal to the largest number of partitions among the input topics. So, in our example, the framework will create 3 tasks because the maximum number of partitions across the input topics T1 and T2 are 3.
 * Now, these tasks will make their own copy of processor Topology and becomes ready to apply the processing logic.
 *
 * The next step is to assign partitions to these tasks. And Kafka would allocate the partitions evenly, i.e., one partition from each Topic to each Task.
 * At the end of this assignment, every Task will have two partitions to process, one from each Topic. This initial assignment of partitions to the tasks never changes, hence the number of Tasks is a fixed, and it is the maximum possible degree of parallelism for the application.
 * But remember, this is all logical, and nothing is yet running, right?
 * However, at this stage, we know that we can execute three parallel threads of this application. The maximum degree of parallelism is 3 because we have only three partitions in the Topic.
 * At this stage, these 3 tasks are ready to be assigned to application threads or instances. Let us assume we started the application with two threads.
 * The framework will assign one task to each thread. One task is still remaining. So, the third Task will go to one of these threads because we do not have any other thread or instance.
 * In this case, the task distribution is not even. The thread running two tasks might run slow. However, all tasks would be running, and ultimately, all the data gets processed.
 *
 * Now imagine that we want to scale out this application. We decide to start another instance with a single thread on a different machine.
 * A new thread T3 will be created, and one task would automatically migrate to the new thread. You won't realize it, but under the hood, Kafka performs an automatic re-balancing for the migration.
 * What if we wanted to add even more instances of the same application? Well, you can, but those instances would remain idle.
 * Why? Because we do not have any task to assign them. Great! Now you understand the parallelism in Kafka Streams.
 *
 * The next critical part is fault tolerance. What happens if an active instance dies or goes down?
 * Well, the fault tolerance would be straightforward. If a task runs on a machine that fails, Kafka Streams automatically reassign that Task to one of the remaining running instances.
 * Right? So, increasing the number of threads may give you higher parallelism but to get fault tolerance, you must add more instances running on different machines.
 * That arrangement gives you fault tolerance because when one computer fails, your threads will move to another computer.
 * And this failure handling is entirely transparent to the end-user and is taken care of by the framework itself.
 *
 * Great!
 * */
public class KafkaStreamsScalabilityModel {


}
