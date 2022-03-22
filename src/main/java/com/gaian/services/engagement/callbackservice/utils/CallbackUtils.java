package com.gaian.services.engagement.callbackservice.utils;

import static java.util.Collections.EMPTY_LIST;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import java.util.List;

import com.gaian.services.engagement.model.db.EngagementDBModel;
import com.gaian.services.engagement.model.request.channel.CallbackUrl;
import com.gaian.services.engagement.model.request.content.ContentModel;


public class CallbackUtils {
	
	public static List<ContentModel> collectAsyncContent(EngagementDBModel engagement) {


		CallbackUrl callback = CallbackUrl.class.cast(engagement.getChannel());

		List<ContentModel> mefContents = callback.getContents();

        if (isNotEmpty(mefContents)) {
            return mefContents.parallelStream().filter(ContentModel::isAsync).collect(toList());
        }

        return EMPTY_LIST;
    }

}
