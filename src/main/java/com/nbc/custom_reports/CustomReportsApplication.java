package com.nbc.custom_reports;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestAttributes;

@SpringBootApplication
public class CustomReportsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomReportsApplication.class, args);
	}
	
	@Bean
	public ErrorAttributes errorAttributes() {
	    return new DefaultErrorAttributes() {
	        @Override
	        public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean includeStackTrace) {
	            Map<String, Object> errorAttributes = super.getErrorAttributes(requestAttributes, includeStackTrace);
	            
	            Map<String, Object> errorAttr = new HashMap<String, Object>();
	            errorAttr.put("exception",errorAttributes.get("message"));
	            errorAttr.put("success",false);
	            // Customize the default entries in errorAttributes to suit your needs
	            return errorAttr;
	        }

	   };
	}
}
