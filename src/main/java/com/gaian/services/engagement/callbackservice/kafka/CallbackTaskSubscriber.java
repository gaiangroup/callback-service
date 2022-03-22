package com.gaian.services.engagement.callbackservice.kafka;

import com.gaian.services.engagement.model.DeliveryRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;


public interface CallbackTaskSubscriber {
	/**
     * Performs the task of engagement delivery
     *
     * @param message message consumed from the kafka topic
     */

	void performTask(ConsumerRecord<String, DeliveryRequest> message);
}
