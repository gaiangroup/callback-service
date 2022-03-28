package com.gaian.services.engagement.apiservice.utils;

import static java.util.Collections.EMPTY_LIST;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import java.util.List;

import com.gaian.services.engagement.model.db.EngagementDBModel;
import com.gaian.services.engagement.model.request.channel.ApiUrl;
import com.gaian.services.engagement.model.request.content.ContentModel;


public class ApiUtils {
	
	public static List<ContentModel> collectAsyncContent(EngagementDBModel engagement) {


        ApiUrl api = ApiUrl.class.cast(engagement.getChannel());

		List<ContentModel> apiContents = api.getContents();

        if (isNotEmpty(apiContents)) {
            return apiContents.parallelStream().filter(ContentModel::isAsync).collect(toList());
        }

        return EMPTY_LIST;
    }

}
