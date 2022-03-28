package com.gaian.services.engagement.apiservice.validator;

import static com.gaian.services.engagement.api.error.ApiErrorTemplates.ERROR_INVALID_API_ENGAGEMENT;
import static com.gaian.services.engagement.api.error.ApiErrorTemplates.ERROR_INVALID_API_CHANNEL;
import static com.gaian.services.engagement.api.error.ApiErrorTemplates.ERROR_AQ_UNSUPPORTED;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import com.gaian.services.engagement.model.request.Type;
import com.gaian.services.engagement.model.request.channel.ChannelType;
import com.gaian.services.engagement.apiservice.error.exception.InvalidApiRequestException;

import com.gaian.services.engagement.error.exception.InvalidChannelException;
import com.gaian.services.engagement.model.DeliveryRequest;
import com.gaian.services.engagement.model.db.EngagementDBModel;
import com.gaian.services.engagement.model.request.Destination;

import com.gaian.services.engagement.model.request.channel.MEF;


public class ValidatorsImpl implements Validators{

	@Override
	public void validateApiRequest(DeliveryRequest inputPayload) {
		
		
		if (isNull(inputPayload)) {
            throw new InvalidApiRequestException();
        }

        EngagementDBModel apiEngagement = inputPayload.getEngagement();

        if (isNull(apiEngagement)) {
            throw new InvalidApiRequestException(ERROR_INVALID_API_ENGAGEMENT);

        } else {

            Destination destination = apiEngagement.getDestination();
            if (Type.ANALYTICS.equals(destination.getType())) {
                throw new RuntimeException(format(ERROR_AQ_UNSUPPORTED, apiEngagement.getId()));
            }

            ofNullable(apiEngagement.getChannel()).map(channel -> {

                if (!(ChannelType.MEF.equals(channel.getChannelType())) || !(channel instanceof MEF)) {
                    throw new InvalidApiRequestException(ERROR_INVALID_API_CHANNEL);
                }

                channel.validate();
                return channel;

            }).orElseThrow(InvalidChannelException::new);
        }
    }
		
	}


