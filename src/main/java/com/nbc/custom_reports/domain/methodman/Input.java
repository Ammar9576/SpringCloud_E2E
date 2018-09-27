package com.nbc.custom_reports.domain.methodman;

import java.util.List;
import java.util.Map;

public class Input {
	private String quarterName;
	private Long quarterId;
	private Double  grossTotalDollars;
	private Double netTotalDollars;
	private Double totalImpressions;
	private String priDemo;
	private Map<String,Double> grossDollarList;
	private Map<String,Double> netDollarList;
	private Map<String,Double> totalImpList;
	private List<Double> equiList;
	
	
	
	/**
	 * @return the totalImpressions
	 */
	public Double getTotalImpressions() {
		return totalImpressions;
	}
	/**
	 * @param totalImpressions the totalImpressions to set
	 */
	public void setTotalImpressions(Double totalImpressions) {
		this.totalImpressions = totalImpressions;
	}
	
	/**
	 * @return the quarterName
	 */
	public String getQuarterName() {
		return quarterName;
	}
	/**
	 * @param quarterName the quarterName to set
	 */
	public void setQuarterName(String quarterName) {
		this.quarterName = quarterName;
	}
	/**
	 * @return the quarterId
	 */
	public Long getQuarterId() {
		return quarterId;
	}
	/**
	 * @param quarterId the quarterId to set
	 */
	public void setQuarterId(Long quarterId) {
		this.quarterId = quarterId;
	}
	
	
	
	/**
	 * @return the netTotalDollars
	 */
	public Double getNetTotalDollars() {
		return netTotalDollars;
	}
	/**
	 * @param netTotalDollars the netTotalDollars to set
	 */
	public void setNetTotalDollars(Double netTotalDollars) {
		this.netTotalDollars = netTotalDollars;
	}
	/**
	 * @return the priDemo
	 */
	public String getPriDemo() {
		return priDemo;
	}
	/**
	 * @param priDemo the priDemo to set
	 */
	public void setPriDemo(String priDemo) {
		this.priDemo = priDemo;
	}
	
	
	/**
	 * @return the totalImpList
	 */
	public Map<String, Double> getTotalImpList() {
		return totalImpList;
	}
	/**
	 * @param totalImpList the totalImpList to set
	 */
	public void setTotalImpList(Map<String, Double> totalImpList) {
		this.totalImpList = totalImpList;
	}
	/**
	 * @return the equiList
	 */
	public List<Double> getEquiList() {
		return equiList;
	}
	/**
	 * @param equiList the equiList to set
	 */
	public void setEquiList(List<Double> equiList) {
		this.equiList = equiList;
	}
	/**
	 * @return the grossTotalDollars
	 */
	public Double getGrossTotalDollars() {
		return grossTotalDollars;
	}
	/**
	 * @param grossTotalDollars the grossTotalDollars to set
	 */
	public void setGrossTotalDollars(Double grossTotalDollars) {
		this.grossTotalDollars = grossTotalDollars;
	}
	/**
	 * @return the grossDollarList
	 */
	public Map<String, Double> getGrossDollarList() {
		return grossDollarList;
	}
	/**
	 * @param grossDollarList the grossDollarList to set
	 */
	public void setGrossDollarList(Map<String, Double> grossDollarList) {
		this.grossDollarList = grossDollarList;
	}
	/**
	 * @return the netDollarList
	 */
	public Map<String, Double> getNetDollarList() {
		return netDollarList;
	}
	/**
	 * @param netDollarList the netDollarList to set
	 */
	public void setNetDollarList(Map<String, Double> netDollarList) {
		this.netDollarList = netDollarList;
	}
	
	
	
	
}