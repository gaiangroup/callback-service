package com.gaian.services.engagement.apiservice.kafka;

import java.util.Map;
import static org.springframework.kafka.support.KafkaHeaders.OFFSET;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_PARTITION_ID;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import com.gaian.services.engagement.apiservice.service.ApiProcessor;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AsyncContentReceiver {
	
	

	@Autowired
	private ApiProcessor apiUrlProcessor;
    /**
     * Kafka listener for async content response events
     *
     * @param event content response
     * @param topic kafka topic
     * @param offset message offset
     * @param partition message partition
     */
    @KafkaListener(
        topics = "${kafka.asyncContentConsumer.topic}",
        containerFactory = "asyncContentReceiverContainerFactory"
    )
    public void receive(
        @Payload Map<String, Object> event,
        @Header(RECEIVED_TOPIC) String topic,
        @Header(OFFSET) Integer offset,
        @Header(RECEIVED_PARTITION_ID) Integer partition) {

        log.info("*** New event received for async content on topic '{}' from partition {} & offset {} : {}",
            topic, offset, partition, event);

        try {
        	apiUrlProcessor.acknowledgeAsyncContentReception(event);

        } catch(Exception exception) {
            log.error("Error while processing async content event received on topic '{}' from partition {} & offset {} : {} ",
                topic, partition, offset, event, exception);
        }
    }

}
