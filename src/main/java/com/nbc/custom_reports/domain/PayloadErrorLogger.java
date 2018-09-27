package com.nbc.custom_reports.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="tbl_cvg_logger",schema="nbc_apps")
public class PayloadErrorLogger {

String id;
String 	tadPayload;
String 	procInput;
String 	errorLog;
Date createdDate;
String requestedBy;

@Id
@Column(name="id")
public String getId() {
	return id;
}
public void setId(String id) {
	this.id = id;
}
@Column(name="tad_payload")
public String getTadPayload() {
	return tadPayload;
}
public void setTadPayload(String tadPayload) {
	this.tadPayload = tadPayload;
}

@Column(name="proc_input")
public String getProcInput() {
	return procInput;
}
public void setProcInput(String procInput) {
	this.procInput = procInput;
}

@Column(name="error_log")
public String getErrorLog() {
	return errorLog;
}
public void setErrorLog(String errorLog) {
	this.errorLog = errorLog;
}

@Column(name="requested_by")
public String getRequestedBy() {
	return requestedBy;
}
public void setRequestedBy(String requestedBy) {
	this.requestedBy = requestedBy;
}
@Column(name="created_date")
public Date getCreatedDate() {
	return createdDate;
}
public void setCreatedDate(Date createdDate) {
	this.createdDate = createdDate;
}

}
