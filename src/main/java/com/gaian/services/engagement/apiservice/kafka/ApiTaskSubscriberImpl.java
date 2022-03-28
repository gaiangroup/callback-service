package com.gaian.services.engagement.apiservice.kafka;

import static java.lang.String.format;
import static com.gaian.services.engagement.error.ErrorTemplates.ERROR_NULL_KAFKA_MESSAGE;
import static java.util.Objects.isNull;



import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.joda.time.DateTime;
import org.joda.time.Period;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gaian.services.engagement.apiservice.service.ApiProcessor;
import com.gaian.services.engagement.apiservice.validator.Validators;
import com.gaian.services.engagement.model.DeliveryRequest;
import com.gaian.services.exception.KafkaConsumptionException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ApiTaskSubscriberImpl implements ApiTaskSubscriber {
	
	@Autowired
    private Validators validators;
	
	@Autowired
	private ApiProcessor apiUrlProcessor;
	
	@Override
    public void performTask(ConsumerRecord<String, DeliveryRequest> message) {

        DeliveryRequest task = message.value();
        //DateTime startTime = now();
        DateTime startTime = DateTime.now();
        

        log.info("*** Received new engagement task from {} from partition {} & offset {} : {}",
            message.topic(), message.partition(), message.offset(), task);

        try {
            if (isNull(task)) {
                throw new KafkaConsumptionException(
                    format(ERROR_NULL_KAFKA_MESSAGE, message.topic()));
            }

            validators.validateApiRequest(task);
            apiUrlProcessor.processEngagement(task);

            log.info("Completed engagement task in {} millis from {} from partition {} & offset {} : {}",
            		
            		new Period(startTime, DateTime.now()).getMillis(), message.topic(), message.partition(), message.offset(), task);	
              //  new Period(startTime, now()).getMillis(), message.topic(), message.partition(), message.offset(), task);

        } catch (Exception exception) {
            log.error("Error while executing an engagement task in {} millis : {} ",
            		new Period(startTime, DateTime.now()).getMillis(), task, exception);
            		
              //  new Period(startTime, now()).getMillis(), task, exception);
        }
    }

}
