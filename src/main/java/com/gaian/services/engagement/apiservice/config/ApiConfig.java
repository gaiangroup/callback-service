package com.gaian.services.engagement.apiservice.config;

import static org.joda.time.Period.parse;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {
	
	 @Bean
	    public Period defaultExpiryPeriod(@Value("${api.default.expiryPeriod:P7D}") String defaultExpiryPeriod) {
	        return parse(defaultExpiryPeriod);
	    }

}
