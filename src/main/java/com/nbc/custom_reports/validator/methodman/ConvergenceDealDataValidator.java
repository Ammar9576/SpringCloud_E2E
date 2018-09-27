package com.nbc.custom_reports.validator.methodman;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.nbc.custom_reports.domain.methodman.ConvergenceDealData;

@Component
public class ConvergenceDealDataValidator implements Validator {
	
	   public boolean supports(Class clazz) {
	        return ConvergenceDealData.class.equals(clazz);
	    }

	    public void validate(Object obj, Errors e) {
	    	ConvergenceDealData p = (ConvergenceDealData) obj;
	        if (p.getLinearDetails()==null || (p.getLinearDetails()!=null && (
	        		p.getLinearDetails().getPlans()==null ||
	        		(p.getLinearDetails().getPlans()!=null && 
	        		p.getLinearDetails().getPlans().size()<=0 )))) {
	        	e.rejectValue("linearDetails", " linear plan");
	        } 
	        
	        if (p.getDemo()==null || (p.getDemo()!=null && (StringUtils.isEmpty(p.getDemo().getId())
	        		|| StringUtils.isEmpty(p.getDemo().getName())))) {
	        	e.rejectValue("demo", "demo");
	        } 
	        
	        if (p.getOnairTemplate()==null || (p.getOnairTemplate()!=null && (StringUtils.isEmpty(p.getOnairTemplate().getId())
	        		|| StringUtils.isEmpty(p.getOnairTemplate().getName())))) {
	        	e.rejectValue("onairTemplate", "onairTemplate");
	        } 
	        
	        if (p.getRevisionType()==null || (p.getRevisionType()!=null && (StringUtils.isEmpty(p.getRevisionType().getId())
	        		|| StringUtils.isEmpty(p.getRevisionType().getName())))) {
	        	e.rejectValue("revisionType", "revisionType");
	        } 
	        
	        if (p.getDigitalOrders()==null || (p.getDigitalOrders()!=null && p.getDigitalOrders().size()<=0)) {
	        	e.rejectValue("digitalOrders", "digitalOrders");
	        } 
	    }
}
