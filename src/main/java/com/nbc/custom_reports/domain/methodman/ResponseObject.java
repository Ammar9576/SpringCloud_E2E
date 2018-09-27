package com.nbc.custom_reports.domain.methodman;

import java.util.Map;

public class ResponseObject { 
	private Map<String,? extends Object> dataMap;
	private String exception;
	private Boolean status;
	
	   
	  public ResponseObject(Map<String,? extends Object> dataMap,Boolean status){
		  this.dataMap = dataMap;	
		  this.status = status;	
	  }
	  
	  public ResponseObject(String exception,Boolean status){
		  this.exception = exception;	
		  this.status = status;	
	  }
	  
	  
	  

	/**
	 * @return the status
	 */
	public Boolean getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Boolean status) {
		this.status = status;
	}

	/**
	 * @return the exception
	 */
	public String getException() {
		return exception;
	}

	/**
	 * @param exception the exception to set
	 */
	public void setException(String exception) {
		this.exception = exception;
	}

	public Map<String, ? extends Object> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<String, ? extends Object> dataMap) {
		this.dataMap = dataMap;
	} 
	

}