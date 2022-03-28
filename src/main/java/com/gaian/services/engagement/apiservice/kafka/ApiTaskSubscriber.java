package com.gaian.services.engagement.apiservice.kafka;

import com.gaian.services.engagement.model.DeliveryRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;


public interface ApiTaskSubscriber {
	/**
     * Performs the task of engagement delivery
     *
     * @param message message consumed from the kafka topic
     */

	void performTask(ConsumerRecord<String, DeliveryRequest> message);
}
