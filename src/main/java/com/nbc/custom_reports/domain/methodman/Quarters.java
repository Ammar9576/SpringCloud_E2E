package com.nbc.custom_reports.domain.methodman;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "onair", name = "calendar_period")
public class Quarters  {

	private Long	orderNo;
	private String	name;
	private Long calendarId;
	private Long year;
	
	
	/**
	 * @return the year
	 */
	@Column(name = "YEAR")
	public Long getYear() {
		return year;
	}
	/**
	 * @param year the year to set
	 */
	public void setYear(Long year) {
		this.year = year;
	}
	/**
	 * @return the orderNo
	 */
	@Id
	@Column(name = "ORDER_NO")
	public Long getOrderNo() {
		return orderNo;
	}
	/**
	 * @param orderNo the orderNo to set
	 */
	public void setOrderNo(Long orderNo) {
		this.orderNo = orderNo;
	}
	/**
	 * @return the name
	 */
	@Column(name = "name")
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
	 * @return the calendarId
	 */
	@Column(name = "CALENDAR_ID")
	public Long getCalendarId() {
		return calendarId;
	}
	/**
	 * @param calendarId the calendarId to set
	 */
	public void setCalendarId(Long calendarId) {
		this.calendarId = calendarId;
	}
}