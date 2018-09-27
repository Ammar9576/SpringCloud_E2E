package com.nbc.custom_reports.domain.methodman;

import java.text.DecimalFormat;

public class TargetGroup {
	private String name;
	private String grossCpm;
	private String netCpm;
	private String impressions;
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * @return the grossCpm
	 */
	public String getGrossCpm() {
		return grossCpm;
	}
	/**
	 * @param grossCpm the grossCpm to set
	 */
	public void setGrossCpm(String grossCpm) {
		this.grossCpm = grossCpm;
	}
	/**
	 * @return the netCpm
	 */
	public String getNetCpm() {
		return netCpm;
	}
	/**
	 * @param netCpm the netCpm to set
	 */
	public void setNetCpm(String netCpm) {
		this.netCpm = netCpm;
	}
	/**
	 * @return the impressions
	 */
	public String getImpressions() {
		return impressions;
	}
	/**
	 * @param impressions the impressions to set
	 */
	public void setImpressions(String impressions) {
		this.impressions = impressions;
	}
	public void addGrossCpm(Double grossDollars, Double impression) {
		DecimalFormat formatter = new DecimalFormat("#00.00");
		String res= grossDollars!=0 && impression!=0 ?formatter.format(((double)grossDollars/impression)*1000):"00.00";
		this.grossCpm = "$"+res;
	}  
	
	public void addNetCpm(Double netDollars, Double impression) {
		DecimalFormat formatter = new DecimalFormat("#00.00");
		String res= netDollars!=0  && impression!=0 ?formatter.format(((double)netDollars/impression)*1000):"00.00";
		this.netCpm = "$"+res;
	} 
	
}
