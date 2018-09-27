package com.nbc.custom_reports.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.nbc.custom_reports.aspect.HttptInterceptor;


@Configuration
public class InterceptorRegisterConfig extends WebMvcConfigurerAdapter {

	@Autowired
	HttptInterceptor httpInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(httpInterceptor);
	}
}