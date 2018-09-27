package com.nbc.custom_reports.domain.olympics;

public class TotalsModel {
 private String type;
 private long grossDollars;
 private long netDollars;
 private double grossCpm;
 private double netCpm;
 private long imps;
 
public String getType() {
	return type;
}
public void setType(String type) {
	this.type = type;
}
public double getGrossDollars() {
	return grossDollars;
}
public void addGrossDollars(double grossDollars) {
	this.grossDollars += grossDollars;
}
public double getNetDollars() {
	return netDollars;
}
public void addNetDollars(double netDollars) {
	this.netDollars += netDollars;
}
public double getGrossCpm() {
	return grossCpm;
}
public void addGrossCpm(double grossCpm) {
	this.grossCpm += grossCpm;
}
public double getNetCpm() {
	return netCpm;
}
public void addNetCpm(double netCpm) {
	this.netCpm += netCpm;
}
public long getImps() {
	return imps;
}
public void addImps(long imps) {
	this.imps += imps;
}
public void setGrossDollars(long grossDollars) {
	this.grossDollars = grossDollars;
}
public void setNetDollars(long netDollars) {
	this.netDollars = netDollars;
}
public void setGrossCpm(double grossCpm) {
	this.grossCpm = grossCpm;
}
public void setNetCpm(double netCpm) {
	this.netCpm = netCpm;
}
public void setImps(long imps) {
	this.imps = imps;
}
 

 
}
