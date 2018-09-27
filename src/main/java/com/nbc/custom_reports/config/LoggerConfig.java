package com.nbc.custom_reports.config;

import java.io.Serializable;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@SuppressWarnings("serial")
@Configuration
public class LoggerConfig implements Serializable {

	@Bean
	@Scope("prototype")
	public Logger log(InjectionPoint injectionPoint) {
		return Logger.getLogger(injectionPoint.getMember().getDeclaringClass());
		
	}
}