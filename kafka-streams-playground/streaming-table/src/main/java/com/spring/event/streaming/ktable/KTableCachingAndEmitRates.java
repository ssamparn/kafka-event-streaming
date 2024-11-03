package com.spring.event.streaming.ktable;

import lombok.extern.slf4j.Slf4j;

/* *
 * In the StreamingKTable App, we saw some basics of KTable. We created an example and used two KTables in that example.
 * However, while testing that example, we realized that the output takes a while to show up on the console. Now we will try to understand the reason for the delay.
 *
 * KTable is responsible for forwarding records to downstream processors as well as to the internal state store. In the StreamingKTableApp example, the "stockTickerSourceKTable" receives a record from a Kafka topic,
 * applies the filter, and forwards filtered records to "filteredStockKTable", as well as to the internal state store. However, the records are not immediately forwarded to the downstream processor or to the state store.
 * A KTable will internally cache the records in memory and wait for a while for more records to arrive.
 *
 * Why is that? Because this is an optimization technique to reduce the number of operations. This waiting is used by the KTable to be able to perform an in-memory update and forward only the latest record to the downstream processors.
 * For example, let's assume you got your first stock tick for HDFCBANK. The "stockTickerSourceKTable" will hold it in memory, and let's assume it waits for 100 milliseconds before sending this record to the next KTable "filteredStockKTable".
 * Imgine, in that 100ms time, you got 2 more ticks for HDFCBANK. So, the "stockTickerSourceKTable" will update the older HDFCBANK ticks with the newer ticks and send only one the most recent tick to the "filteredStockKTable".
 * In this case, the number of records to the downstream node is reduced from 3 to 1. The net result is the same because old records for the same key are anyway overwritten with the newer records, right?
 * You can disable this caching. However, When there is no caching, the "filteredStockKTable" gets all the 3 records for HDFCBANK. Caching has no direct impact on the correctness of the data, but it is just an optimization technique.
 * By caching, the "stockTickerSourceKTable" reduces the number of write operations to the downstream processor "filteredStockKTable" as well as to the underlying RocksDB.
 *
 * In a real-life scenario, waiting for 200 to 300 milliseconds could reduce the number of records by many folds. However, it will also slow down the process. Hence, you have an option to tune it and set it according to your use case requirements.
 * There are 2 configurations for tuning the record caching in your Kafka Streams DSL.
 *  1. commit.interval.ms
 *  2. cache.max.bytes.buffering
 *
 * The commit interval is the maximum time for a processor to wait before emitting the records to the downstream processor or the state store.
 * The next configuration controls the number of bytes allocated for caching. But remember, this is the maximum buffer for all the threads in your application instance.
 * If you specified 10MB cache buffer and running 10 topology threads, each one will get 1 MB cache share.
 * Great! So the data is flushed to the state store and forwarded to the next downstream processor node whenever the earliest of commit interval or the cache buffer hits.
 *
 * Setting these configurations is easy.
 *
 * // set commit interval to 1 second
 * props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
 *
 * // enable record cache of size 10MB
 * props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L);
 *
 * You can disable caching by setting any of these configurations to zero. Great!
 *
 * The next most important thing here is the scope of these configurations. Both commit.interval.ms and cache.max.bytes.buffering are application-level parameters.
 * So, It is not possible to specify different parameters for individual processor nodes in your topology. That means you cannot set the commit interval of 500 ms for "stockTickerSourceKTable" and 100 ms for "filteredStockKTable".
 *
 * However, for some applications, that could be a critical requirement. You might want to control the KTable emit rate on an individual node basis rather than achieving this as a side-effect of the record caches and commit interval.
 * Kafka Streams offers you to directly impose the rate limit via the KTable suppress() operator. We can suppress the emit rate of "stockTickerSourceKTable" for 5 minutes.
 *
 * KTable.supress(Supressed.untilTimeLimit(Duration.ofMinutes(5));
 *
 * That means, the "stockTickerSourceKTable" will only flush the records to "filteredStockKTable" after five minutes. In such scenarios, you must be careful about the buffer memory.
 * In the given code snippet, note that the latest state for each key in "stockTickerSourceKTable" must be buffered in memory for those 5 minutes. Hence, it is critical to ensure that you have enough memory.
 * You can specify buffer configuration in terms of maxBytes or maxRecords. Additionally, it is also possible to choose what happens if the buffer fills up.
 *
 * KTable.supress(Supressed.untilTimeLimit(Duration.ofMinutes(5), Supressed.BufferConfig.maxBytes(100000L).emitEarlyWhenFull()));
 *
 * This example takes a relaxed approach and just emits early if needed to free up the buffer memory. Alternatively, you can choose to stop processing and shut down the application.
 * That may seem extreme, but it gives you a guarantee that the 5-minute time limit will be absolutely enforced.
 *
 * After the application shuts down, you could allocate more memory for the buffer and resume processing.
 * */
@Slf4j
public class KTableCachingAndEmitRates {

}
