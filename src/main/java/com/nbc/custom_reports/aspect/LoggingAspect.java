package com.nbc.custom_reports.aspect;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nbc.custom_reports.config.RequestWrapper;

@Aspect
@Configuration
@Component
public class LoggingAspect {
	
	@Autowired
	private Logger logger;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	public void logMessage(String logMessage) {
		logger.info(logMessage);
	}

	private void entering(JoinPoint joinPoint) {
		String str = joinPoint.getSignature().getDeclaringTypeName();
		logger.debug("Entering into " + joinPoint.getSignature().getName() + "() method of class "+ str.substring(str.lastIndexOf('.') + 1));
	}

	private void exiting(JoinPoint joinPoint) {
		String str = joinPoint.getSignature().getDeclaringTypeName();
		logger.debug("Exiting from " + joinPoint.getSignature().getName() + "() method of class "+ str.substring(str.lastIndexOf('.') + 1));
	}
	
	@Before("execution(* com.nbc.custom_reports.controller.*.*.*(..)) && !execution(* com.nbc.custom_reports.filter.CORSFilter.*(..))")
	public void beforeController(JoinPoint joinPoint) {
		entering(joinPoint);
	}
	
	@Before("execution(* com.nbc.custom_reports.repository.*.*.*(..)) && !execution(* com.nbc.custom_reports.filter.CORSFilter.*(..))")
	public void beforeRepository(JoinPoint joinPoint) {
		entering(joinPoint);
	}

	@Before("execution(* com.nbc.custom_reports.service.*.*.*(..)) && !execution(* com.nbc.custom_reports.filter.CORSFilter.*(..))")
	public void beforeService(JoinPoint joinPoint) {
		entering(joinPoint);
	}

	@AfterReturning("execution(* com.nbc.custom_reports.controller.*.*.*(..)) && !execution(* com.nbc.custom_reports.filter.CORSFilter.*(..))")
	public void afterController(JoinPoint joinPoint) {
		exiting(joinPoint);
	}

	@AfterReturning("execution(* com.nbc.custom_reports.repository.*.*.*(..)) && !execution(* com.nbc.custom_reports.filter.CORSFilter.*(..))")
	public void afterRepository(JoinPoint joinPoint) {
		exiting(joinPoint);
	}

	@AfterReturning("execution(* com.nbc.custom_reports.service.*.*.*(..)) && !execution(* com.nbc.custom_reports.filter.CORSFilter.*(..))")
	public void afterService(JoinPoint joinPoint) {
		exiting(joinPoint);
	}
	
	@AfterThrowing(value = "(execution(* com.nbc.custom_reports.repository.*.*.*(..)) || execution(* com.nbc.custom_reports.service.*.*.*(..)) || execution(* com.nbc.custom_reports.controller.*.*.*(..)))  && !execution(* com.nbc.custom_reports.filter.CORSFilter.*(..))", throwing = "exception")
	public void logAfterThrowing(JoinPoint joinPoint, Throwable  exception) {
		String str = joinPoint.getSignature().getDeclaringTypeName();
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		RequestWrapper rw=(RequestWrapper)request;
		if(StringUtils.isNotEmpty(rw.getBody())){
			logger.info("Request Body : "+rw.getBody());	
		}
		if(StringUtils.isNotEmpty(request.getQueryString())){
			logger.info("Query String : "+request.getQueryString());	
		}

	    if(exception.getStackTrace()!=null) {
			StackTraceElement[] steArr=exception.getStackTrace();
			steArr = Arrays.stream(steArr).filter(s -> s.getClassName().startsWith("com")&& ! s.getClassName().contains("CORSFilter") && ! s.getClassName().contains("$$")).toArray(StackTraceElement[]::new);
			
			
			String message=null;
			if(exception.getMessage()!=null) {
				message=StringUtils.capitalize((exception.getClass().getSimpleName()).toLowerCase().replace("exception", " exception( "+exception.getMessage()+" )"));
			}else {
				message=StringUtils.capitalize((exception.getClass().getSimpleName()).toLowerCase().replace("exception", " exception"));
			}
			if(steArr!=null && steArr.length>0){
				StackTraceElement ste=steArr[0];
				logger.error(message+" occured in the method " + joinPoint.getSignature().getName()
						+ "()  of class  " + str.substring(str.lastIndexOf('.') + 1) + " at linenumber "+ste.getLineNumber());
			}else{
				
				logger.error(message+" occured in the method " + joinPoint.getSignature().getName()
						+ "()  of class  " + str.substring(str.lastIndexOf('.') + 1));
				
			}
			
			
		}else {
			logger.error("Exception occured in the method " + joinPoint.getSignature().getName()
					+ "()  of class  " + str.substring(str.lastIndexOf('.') + 1));
		}

	}
	
}