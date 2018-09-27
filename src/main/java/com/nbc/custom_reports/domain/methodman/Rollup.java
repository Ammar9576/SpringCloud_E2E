package com.nbc.custom_reports.domain.methodman;

public class Rollup {
	private Long linearDollars;
	private Long digitalDollars;
	private Double totalDollars;
	private Double linearImps;
	private Double digitalImps;
	private Double totalImps;

	public Long getLinearDollars() {
		return linearDollars;
	}

	public void setLinearDollars(Long linearDollars) {
		this.linearDollars = linearDollars;
	}

	public Long getDigitalDollars() {
		return digitalDollars;
	}

	public void setDigitalDollars(Long digitalDollars) {
		this.digitalDollars = digitalDollars;
	}

	public Double getTotalDollars() {
		return totalDollars;
	}

	public void setTotalDollars(Double totalDollars) {
		this.totalDollars = totalDollars;
	}

	public Double getLinearImps() {
		return linearImps;
	}

	public void setLinearImps(Double linearImps) {
		this.linearImps = linearImps;
	}

	public Double getDigitalImps() {
		return digitalImps;
	}

	public void setDigitalImps(Double digitalImps) {
		this.digitalImps = digitalImps;
	}

	public Double getTotalImps() {
		return totalImps;
	}

	public void setTotalImps(Double totalImps) {
		this.totalImps = totalImps;
	}

}
