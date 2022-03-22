package com.gaian.services.engagement.callbackservice.config;

import static io.vertx.core.Vertx.vertx;
import static java.time.Duration.ofMillis;
import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;
import com.gaian.services.engagement.callbackservice.kafka.DeliveryRequestDeserializer;
import com.gaian.services.engagement.model.DeliveryRequest;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecords;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import com.gaian.services.exception.KafkaException;
import org.apache.kafka.common.serialization.StringSerializer;
import com.gaian.services.engagement.callbackservice.kafka.CallbackTaskSubscriber;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;


import org.springframework.beans.factory.annotation.Value;

import dev.snowdrop.vertx.kafka.KafkaConsumerFactory;
import static java.lang.String.format;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Data
@Slf4j
@EnableKafka
@Configuration
@ConfigurationProperties(prefix = "kafka")
@EnableAutoConfiguration(exclude = KafkaAutoConfiguration.class)
public class KafkaConfig {
	
	@Autowired
	private CallbackTaskSubscriber subscriber;
	
	@Autowired
	private KafkaConsumerFactory consumerFactory;
	
	@Value("${kafka.callbackTaskConsumer.topic}")
	private String engagementTopic;
	
	private Map<String, String> callbackTaskConsumer;
	
	private final StringSerializer stringSerializer = new StringSerializer();
	 
	/**
     * Setting up kafka consumers based on vertx
     */
	
	 @PostConstruct
	    public void setUpEngagementEventListeners() {

		 callbackTaskConsumer.put("key.deserializer", StringDeserializer.class.getName());
		 callbackTaskConsumer.put("value.deserializer", DeliveryRequestDeserializer.class.getName());

	        Integer concurrency = ofNullable(callbackTaskConsumer.get("concurrency"))
	            .map(Integer::valueOf).orElse(30);

	        range(0, concurrency).forEach(index ->

	            consumerFactory.<String, DeliveryRequest>create(callbackTaskConsumer).doOnVertxConsumer(
	                consumer -> consumer.subscribe(engagementTopic, subscription -> {

	                    if (subscription.succeeded()) {

	                        log.info("Consumer {} subscribed to topic {}", index, engagementTopic);

	                        Long minPollTime = ofNullable(callbackTaskConsumer.get("min.poll.interval.ms"))
	                            .map(Long::valueOf).orElse(10000L);

	                        vertx().setPeriodic(minPollTime, poller -> pollMessages(consumer, index));

	                    } else {

	                        throw new KafkaException(
	                            format("Error subscribing to topic '%s' ", engagementTopic), subscription.cause());
	                    }
	                })
	            ).block()
	        );
	    }


	 /**
	     * Poll the kafka topic form incoming messages
	     *
	     * @param consumer kafka consumer
	     * @param consumerId ID assigned to the consumer
	     */
	 
	 public void pollMessages(
		        KafkaConsumer<String, DeliveryRequest> consumer, int consumerId) {

		        log.debug("Trying to poll from {}", engagementTopic);
		        Long pollTimeout = ofNullable(callbackTaskConsumer.get("poll.timeout.ms"))
		                .map(Long::valueOf).orElse(1000L);

		        consumer.poll(ofMillis(pollTimeout), response -> {

		            if (response.succeeded()) {

		                KafkaConsumerRecords<String, DeliveryRequest> events = response.result();
		                if (!events.isEmpty()) {

		                    ConsumerRecords<String, DeliveryRequest> messages = events.records();

		                    log.info("{} messages polled from partitions {} by consumer {}",
		                        events.size(), messages.partitions(), consumerId);

		                    consumer.pause();
		                    log.debug("Consumer paused! ");

		                    messages.forEach(task -> {

		                        subscriber.performTask(task);
		                        commit(consumer, consumerId, messages);
		                    });

		                    consumer.resume();
		                    log.debug("Consumer resumed");
		                }
		                pollMessages(consumer, consumerId);

		            } else {
		                log.error("Failed to poll messages from topic '{}' ", engagementTopic, response.cause());
		            }
		        });
		    }

	 /**
	     * Commit all the messages that are consumed and processed
	     *
	     * @param consumer The kafka consumer
	     * @param consumerId ID assigned to the consumer
	     * @param messages messages consumed from the kafka topic
	     */
	 
	 private void commit(
		        KafkaConsumer<String, DeliveryRequest> consumer, int consumerId,
		        ConsumerRecords<String, DeliveryRequest> messages) {

		        if (!ofNullable(callbackTaskConsumer.get("enable.auto.commit"))
		            .map(Boolean::valueOf).orElse(false)) {

		            consumer.commit(commitResponse -> {
		                if (commitResponse.failed()) {
		                    log.error("Consumer {} failed to commit on partition {} of {}",
		                        consumerId, messages.partitions(),
		                        engagementTopic, commitResponse.cause());
		                } else {
		                    log.info("Consumer {} committed message on partition {}",
		                        consumerId, messages.partitions());
		                }
		            });
		        }
		    }
	 
}
