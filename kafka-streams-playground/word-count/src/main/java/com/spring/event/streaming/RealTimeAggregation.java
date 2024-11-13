package com.spring.event.streaming;

import lombok.extern.slf4j.Slf4j;

/* *
 * Computing aggregate is a 2-step process. i.e., group your data by key and then apply aggregation formula.
 * However, before we start getting deeper into aggregation, realize that the record key is the most critical element for an aggregation. So, your thought process for aggregation must start with choosing an appropriate aggregation key.
 * However, working with keys has got one more critical consideration. What is that? Data must be partitioned on the key.
 *
 * The application topology gets executed in parallel, and each task will be working on a single input partition. Right? In this case, when you are grouping your data on a key, you must ensure that all the records for the same key are stored in a single partition.
 * If that doesn't happen, then your aggregation for the same key might be computed at two different places and won't result in an accurate value.
 * For example, let's assume the first message "Hello World" goes to task-1, and the second message "Hello Kafka Streams" goes to task-2.
 * Both of these tasks will use flatMap to get the words separated. Then they will do a groupBy on these words. And finally, calculate count.
 * But in this case, you are counting the same word "Hello" at 2 different places. Right? But, is it correct? No! How do you fix it?
 *
 * We learned earlier, we can use a through() method to repartition the data. Right? But we know that a grouping operation is almost always going to change the key.
 * I mean, you might be lucky enough to have a stream that comes with the same key that you want to use in your groupBy. But in a real-life scenario, that's a rare occurrence. And Kafka creators also knew that.
 * So, they designed the Kafka Streams API to detect such needs for an aggregation and automatically repartition your stream without making an explicit through() method call.
 * That's good news, right? So, our streaming word count example is already full proof to work in the distributed mode. We do not need to add a through() method.
 *
 * Let me highlight one more thing at this stage. Most of the Kafka Streams APIs are available in 2 variants.
 *   1. Key Preserving APIs
 *   2. Key Changing APIs
 * Many APIs that we used so far including mapValues(), flatMapValues(), transformValues(), and groupByKey() are key preserving APIs.
 * These APIs are supposed to be used to work with the message value while preserving the same key over the resulting stream.
 * We often prefer to use these key preserving APIs unless we purposefully want to change the message key.
 *
 * Kafka Streams also provides an equivalent key changing APIs such as map(), flatMap(), transform(), and groupBy().
 * These APIs are meant to be used when you intentionally want to change the key in the resulting stream.
 * Changing the key of your stream shows an intention to repartition or redistribute your data on the new message key. Right?
 * Ideally, you should always add a through() method after changing your message key. However, Kafka Stream DSL automatically detects this and applies an automatic repartition for you.
 * So, whenever we use a key changing API, Kafka internally sets a Boolean flag that the new KStream instance requires repartitioning.
 * Once this Boolean flag is set, if you perform an aggregate or a join operation, the repartitioning is handled for you automatically. Amazing! Isn't it?
 * But you still need to be careful in your design. Why? Because this automatic repartition only happens in case of the two conditions.
 *   1. You are using a KeyChanging API.
 *   2. You are using aggregation or a join.
 * In every other repartitioning requirement, you must use the through() method.
 *
 * The next observation is this. You can aggregate a KStream or a KTable. So, the primary source of aggregation could be a KStream or it could be a KTable.
 * However, the outcome of aggregation is always a KTable. Great!
 *
 * Aggregation in Kafka Streams is a 2-step process.
 *   1. Group the data.
 *   2. Apply an aggregation function.
 *
 * Kafka Streams offers you only 3 functions or methods to compute an aggregate.
 *   1. count()
 *   2. reduce(), and
 *   3. aggregate().
 *
 * That is all.If you were expecting sum(), max(), avg(), and similar kind of a long list of functions, which you might have seen in SQL then let me tell you that the Kafka Streams do not have that kind of luxury.
 * Why? Because the record structure that we use in the Kafka Streams application is not as primitive as SQL types. I mean, you can apply avg on reward points, when the reward point is as simple as an integer or long.
 * However, when the reward is a complex Java object, you cannot apply a straightforward avg() function.
 * Hence, every aggregation in Kafka streams is created using a generalized framework such as reduce() and aggregate().
 * The count() is the only exception because it does not depend on the data type. All we need to do is to count the number of records. Right? All other aggregations are created using one of the following methods. reduce() & aggregate()
 * Both of these are available on a grouped table as well as on a grouped stream. I mean, you can use them to aggregate a KTable as well as a KStream.
 * Your first choice of aggregation should be the reduce() method because it is simple and easy to implement. However, the reduce method comes with some restrictions, and we will learn those things with an example.
 * If the reduce() method is not meeting your requirement, you can use aggregate() method. The aggregate() method is more generalized, and we will learn the details when we create an example.
 *
 * Great!
 * */
@Slf4j
public class RealTimeAggregation {


}
