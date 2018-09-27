package com.nbc.custom_reports.domain.methodman;

public class Dashboard {
	Integer convgDealId;
	Rollup dollarsAndImps;
	/*
	 * private double linearIndex; private double digitalIndex;
	 */
	double hvcPercent;
	long benchmark;
	long moatScore;
	double demoReachPercent;
	double demoAverageFrequency;
	String consumerTargets;
	double consumerReachPercent;
	double consumerAverageFrequency;
	boolean includeDashboard;

	public Integer getConvgDealId() {
		return convgDealId;
	}

	public void setConvgDealId(Integer convgDealId) {
		this.convgDealId = convgDealId;
	}

	public double getHvcPercent() {
		return hvcPercent;
	}

	public void setHvcPercent(double hvcPercent) {
		this.hvcPercent = hvcPercent;
	}

	public long getBenchmark() {
		return benchmark;
	}

	public void setBenchmark(long benchmark) {
		this.benchmark = benchmark;
	}

	public Rollup getDollarsAndImps() {
		return dollarsAndImps;
	}

	public void setDollarsAndImps(Rollup dollarsAndImps) {
		this.dollarsAndImps = dollarsAndImps;
	}

	/*
	 * public double getLinearIndex() { return linearIndex; }
	 * 
	 * public void setLinearIndex(double linearIndex) { this.linearIndex =
	 * linearIndex; }
	 * 
	 * public double getDigitalIndex() { return digitalIndex; }
	 * 
	 * public void setDigitalIndex(double digitalIndex) { this.digitalIndex =
	 * digitalIndex; }
	 */

	public long getMoatScore() {
		return moatScore;
	}

	public void setMoatScore(long moatScore) {
		this.moatScore = moatScore;
	}

	public double getDemoReachPercent() {
		return demoReachPercent;
	}

	public void setDemoReachPercent(double demoReachPercent) {
		this.demoReachPercent = demoReachPercent;
	}

	public double getDemoAverageFrequency() {
		return demoAverageFrequency;
	}

	public void setDemoAverageFrequency(double demoAverageFrequency) {
		this.demoAverageFrequency = demoAverageFrequency;
	}

	public String getConsumerTargets() {
		return consumerTargets;
	}

	public void setConsumerTargets(String consumerTargets) {
		this.consumerTargets = consumerTargets;
	}

	public double getConsumerReachPercent() {
		return consumerReachPercent;
	}

	public void setConsumerReachPercent(double consumerReachPercent) {
		this.consumerReachPercent = consumerReachPercent;
	}

	public double getConsumerAverageFrequency() {
		return consumerAverageFrequency;
	}

	public void setConsumerAverageFrequency(double consumerAverageFrequency) {
		this.consumerAverageFrequency = consumerAverageFrequency;
	}

	public boolean isIncludeDashboard() {
		return includeDashboard;
	}

	public void setIncludeDashboard(boolean includeDashboard) {
		this.includeDashboard = includeDashboard;
	}
	
	

}
