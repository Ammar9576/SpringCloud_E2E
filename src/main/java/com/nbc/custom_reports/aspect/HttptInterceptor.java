package com.nbc.custom_reports.aspect;


    import javax.servlet.http.HttpServletRequest;
	import javax.servlet.http.HttpServletResponse;
	import org.apache.log4j.Logger;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.stereotype.Component;
	import org.springframework.web.servlet.ModelAndView;
	import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

	@Component
	public class HttptInterceptor extends HandlerInterceptorAdapter{

		@Autowired
		private Logger logger;
		
		public void logMessage(String logMessage) {
			logger.info(logMessage);
		}
		
		 @Override
		 public boolean preHandle(HttpServletRequest request,HttpServletResponse response, Object object) throws Exception {
			 logMessage("Request Type : " + request.getMethod()+"  Request URL : " + request.getRequestURL());
			return true;
		 }

		 @Override
		 public void postHandle(HttpServletRequest request, HttpServletResponse response, 
				Object object, ModelAndView model)
				throws Exception {
		 }

		 @Override
		 public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
				Object object, Exception arg3)
				throws Exception {
			
			 logMessage("Response Status Code : "+response.getStatus());
		 }
		
	}


