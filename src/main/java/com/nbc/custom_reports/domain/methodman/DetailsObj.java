package com.nbc.custom_reports.domain.methodman;

import java.util.Map;

public class DetailsObj {
	@SuppressWarnings("rawtypes")
	private Map<String, Map> linearMap;
	@SuppressWarnings("rawtypes")
	private Map<String, Map> digitalMap;
	private Map<String, Long> quarterMap;
	private String demoName;
	
	//158365199
	private boolean guarCpmFlag;	
	public boolean isGuarCpmFlag() {
		return guarCpmFlag;
	}
	public void setGuarCpmFlag(boolean guarCpmFlag) {
		this.guarCpmFlag = guarCpmFlag;
	}
	//158365199
	
	
	/**
	 * @return the linearMap
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Map> getLinearMap() {
		return linearMap;
	}
	/**
	 * @param linearMap the linearMap to set
	 */
	@SuppressWarnings("rawtypes")
	public void setLinearMap(Map<String, Map> linearMap) {
		this.linearMap = linearMap;
	}
	/**
	 * @return the digitalMap
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Map> getDigitalMap() {
		return digitalMap;
	}
	/**
	 * @param digitalMap the digitalMap to set
	 */
	@SuppressWarnings("rawtypes")
	public void setDigitalMap(Map<String, Map> digitalMap) {
		this.digitalMap = digitalMap;
	}
	/**
	 * @return the quarterMap
	 */
	public Map<String, Long> getQuarterMap() {
		return quarterMap;
	}
	/**
	 * @param quarterMap the quarterMap to set
	 */
	public void setQuarterMap(Map<String, Long> quarterMap) {
		this.quarterMap = quarterMap;
	}
	/**
	 * @return the demoName
	 */
	public String getDemoName() {
		return demoName;
	}
	/**
	 * @param demoName the demoName to set
	 */
	public void setDemoName(String demoName) {
		this.demoName = demoName;
	}
	
	
	
	
}