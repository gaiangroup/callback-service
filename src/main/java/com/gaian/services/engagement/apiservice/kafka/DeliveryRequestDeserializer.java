package com.gaian.services.engagement.apiservice.kafka;

import com.gaian.services.engagement.model.DeliveryRequest;
import org.springframework.kafka.support.serializer.JsonDeserializer;

public class DeliveryRequestDeserializer extends JsonDeserializer<DeliveryRequest>{

}
