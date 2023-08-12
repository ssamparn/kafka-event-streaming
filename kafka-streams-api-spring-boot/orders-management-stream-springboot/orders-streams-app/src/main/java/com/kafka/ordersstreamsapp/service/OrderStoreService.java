package com.kafka.ordersstreamsapp.service;

import com.kafka.orders.streams.domain.TotalRevenue;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderStoreService {

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    public ReadOnlyKeyValueStore<String, Long> ordersCountStore(String generalOrdersCountTopicStoreName) {
        return streamsBuilderFactoryBean.getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(generalOrdersCountTopicStoreName, QueryableStoreTypes.keyValueStore()));
    }

    public ReadOnlyKeyValueStore<String, TotalRevenue> ordersRevenueStore(String generalOrdersRevenueTopicStoreName) {
        return streamsBuilderFactoryBean.getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(generalOrdersRevenueTopicStoreName, QueryableStoreTypes.keyValueStore()));
    }
}
