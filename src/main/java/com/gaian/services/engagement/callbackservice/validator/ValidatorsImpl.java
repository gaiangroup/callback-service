package com.gaian.services.engagement.callbackservice.validator;

import static com.gaian.services.engagement.callback.error.CallbackErrorTemplates.ERROR_INVALID_CALLBACK_ENGAGEMENT;
import static com.gaian.services.engagement.callback.error.CallbackErrorTemplates.ERROR_INVALID_CALLBACK_CHANNEL;
import static com.gaian.services.engagement.callback.error.CallbackErrorTemplates.ERROR_AQ_UNSUPPORTED;

import com.gaian.services.engagement.callback.error.CallbackErrorTemplates;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import org.apache.commons.lang3.StringUtils;
import com.gaian.services.engagement.model.request.Type;
import com.gaian.services.engagement.model.request.channel.ChannelType;
import com.gaian.services.engagement.callbackservice.error.exception.InvalidCallbackRequestException;

import com.gaian.services.engagement.error.exception.InvalidChannelException;
import com.gaian.services.engagement.model.DeliveryRequest;
import com.gaian.services.engagement.model.db.EngagementDBModel;
import com.gaian.services.engagement.model.request.Destination;

import com.gaian.services.engagement.model.request.channel.MEF;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.gaian.services.engagement.model.request.channel.CallbackUrl;


public class ValidatorsImpl implements Validators{

	@Override
	public void validateCallbackRequest(DeliveryRequest inputPayload) {
		
		
		if (isNull(inputPayload)) {
            throw new InvalidCallbackRequestException();
        }

        EngagementDBModel callbackEngagement = inputPayload.getEngagement();

        if (isNull(callbackEngagement)) {
            throw new InvalidCallbackRequestException(ERROR_INVALID_CALLBACK_ENGAGEMENT);

        } else {

            Destination destination = callbackEngagement.getDestination();
            if (Type.ANALYTICS.equals(destination.getType())) {
                throw new RuntimeException(format(ERROR_AQ_UNSUPPORTED, callbackEngagement.getId()));
            }

            ofNullable(callbackEngagement.getChannel()).map(channel -> {

                if (!(ChannelType.MEF.equals(channel.getChannelType())) || !(channel instanceof MEF)) {
                    throw new InvalidCallbackRequestException(ERROR_INVALID_CALLBACK_CHANNEL);
                }

                channel.validate();
                return channel;

            }).orElseThrow(InvalidChannelException::new);
        }
    }
		
	}


