package com.gaian.services.engagement.apiservice.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

import java.util.HashMap;
import static java.util.Optional.ofNullable;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka")
public class AsyncContentReceiverConfig {
	private Map<String, String> asyncContentConsumer;

    private final StringDeserializer keyDeserializer = new StringDeserializer();
    private final JsonDeserializer<Map<String, Object>> eventDeserializer = new JsonDeserializer<>(Map.class);
    @Bean(name = "asyncContentReceiverContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> asyncContentReceiverContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> factory
            = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(
            new HashMap<>(asyncContentConsumer), keyDeserializer, eventDeserializer));
        factory.setConcurrency(ofNullable(asyncContentConsumer.get("concurrency")).map(Integer::parseInt).orElse(1));
        return factory;
    }

}
