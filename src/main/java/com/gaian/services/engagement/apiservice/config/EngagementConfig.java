package com.gaian.services.engagement.apiservice.config;

import java.util.EnumMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static com.gaian.services.engagement.model.request.content.ContentType.ANIMATION;
import static com.gaian.services.engagement.model.request.content.ContentType.BA;
import static com.gaian.services.engagement.model.request.content.ContentType.CHART;
import static com.gaian.services.engagement.model.request.content.ContentType.PLACEHOLDER;
import static com.gaian.services.engagement.model.request.content.ContentType.PNG_ANIMATION;
import static com.gaian.services.engagement.model.request.content.ContentType.STATIC_TEXT;
import static com.gaian.services.engagement.model.request.content.ContentType.STATIC_URL;
import static com.gaian.services.engagement.model.request.content.ContentType.TEMPLATED_TEXT;

import com.gaian.services.engagement.content.processor.AnimationProcessor;
import com.gaian.services.engagement.content.processor.BAProcessor;
import com.gaian.services.engagement.content.processor.ChartProcessor;
import com.gaian.services.engagement.content.processor.ContentProcessor;
import com.gaian.services.engagement.content.processor.PlaceholderProcessor;
import com.gaian.services.engagement.content.processor.PngAnimationProcessor;
import com.gaian.services.engagement.content.processor.StaticTextProcessor;
import com.gaian.services.engagement.content.processor.StaticUrlProcessor;
import com.gaian.services.engagement.content.processor.TemplatedTextProcessor;
import com.gaian.services.engagement.model.request.content.ContentType;


@Configuration
public class EngagementConfig {
	/**
     * Creates a single bean with processors of all content types
     *
     * @param animationProcessor Animation processor bean
     * @param staticTextProcessor Static text processor bean
     * @param templatedTextProcessor Templated text processor bean
     * @param chartProcessor Chart processor bean
     * @param staticUrlProcessor Static Url processor
     * @param baProcessor BA processor bean
     * @param placeholderProcessor Placeholder processor bean
     * @return map with processors of all types
     */
	
	 @Bean(name = "content_processors")
	    public Map<ContentType, ContentProcessor> contentProcessors(
	        AnimationProcessor animationProcessor, StaticTextProcessor staticTextProcessor,
	        TemplatedTextProcessor templatedTextProcessor, ChartProcessor chartProcessor,
	        StaticUrlProcessor staticUrlProcessor, BAProcessor baProcessor,
	        PlaceholderProcessor placeholderProcessor, PngAnimationProcessor pngAnimationProcessor) {

	        EnumMap<ContentType, ContentProcessor> processors = new EnumMap<>(ContentType.class);

	        processors.put(BA, baProcessor);
	        processors.put(CHART, chartProcessor);
	        processors.put(ANIMATION, animationProcessor);
	        processors.put(STATIC_URL, staticUrlProcessor);
	        processors.put(STATIC_TEXT, staticTextProcessor);
	        processors.put(PLACEHOLDER, placeholderProcessor);
	        processors.put(PNG_ANIMATION, pngAnimationProcessor);
	        processors.put(TEMPLATED_TEXT, templatedTextProcessor);

	        return processors;
}
}

