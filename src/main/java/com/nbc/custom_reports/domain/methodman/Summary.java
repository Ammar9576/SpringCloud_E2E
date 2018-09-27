package com.nbc.custom_reports.domain.methodman;

import java.util.Set;

public class Summary {
	private String quarterName;
	private Long orderId;
	private String grossDollars;
	private String netDollars;
	private String equivUnits;
	private TargetGroup priDemo;
	private Set<TargetGroup> secDemo;
	
	
	/**
	 * @return the orderId
	 */
	public Long getOrderId() {
		return orderId;
	}
	/**
	 * @param orderId the orderId to set
	 */
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
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
	 * @return the grossDollars
	 */
	public String getGrossDollars() {
		return grossDollars;
	}
	/**
	 * @param grossDollars the grossDollars to set
	 */
	public void setGrossDollars(String grossDollars) {
		this.grossDollars = grossDollars;
	}
	/**
	 * @return the netDollars
	 */
	public String getNetDollars() {
		return netDollars;
	}
	/**
	 * @param netDollars the netDollars to set
	 */
	public void setNetDollars(String netDollars) {
		this.netDollars = netDollars;
	}
	/**
	 * @return the priDemo
	 */
	public TargetGroup getPriDemo() {
		return priDemo;
	}
	/**
	 * @param priDemo the priDemo to set
	 */
	public void setPriDemo(TargetGroup priDemo) {
		this.priDemo = priDemo;
	}
	/**
	 * @return the secDemo
	 */
	public Set<TargetGroup> getSecDemo() {
		return secDemo;
	}
	/**
	 * @param secDemo the secDemo to set
	 */
	public void setSecDemo(Set<TargetGroup> secDemo) {
		this.secDemo = secDemo;
	}
	
	/**
	 * @return the equivUnits
	 */
	public String getEquivUnits() {
		return equivUnits;
	}
	/**
	 * @param equivUnits the equivUnits to set
	 */
	public void setEquivUnits(String equivUnits) {
		this.equivUnits = equivUnits;
	}
	
}