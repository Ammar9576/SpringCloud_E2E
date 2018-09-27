package com.nbc.custom_reports.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.nbc.custom_reports.config.RequestWrapper;


@Component
public class CORSFilter implements Filter {
	
    private static final Logger logger = Logger.getLogger(CORSFilter.class);
    private FilterConfig filterConfig = null;

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with,content-type");
        req = new RequestWrapper((HttpServletRequest) req);
        chain.doFilter(req, res);
    }

    public void init(FilterConfig filterConfig) {
    	this.filterConfig = filterConfig;
    	logger.debug("CORS Filter initialized.");

    }

    public void destroy() {
    	 this.filterConfig = null;
    	 
    }

}