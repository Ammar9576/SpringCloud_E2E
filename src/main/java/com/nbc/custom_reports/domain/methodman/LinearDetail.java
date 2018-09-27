package com.nbc.custom_reports.domain.methodman;

import java.util.Set;

import com.nbc.custom_reports.domain.methodman.ConvergenceDealData.LinearDeal.Plan;

public class LinearDetail {
	
	private Set<Plan> plans;
	private Long portfolioId;
	private Long onAirTemplateId;
	private String onAirTemplate;
	private Long revisionTypeId;
	private String revisionType;
	private String demo;
	
	private Long demoId;
	private Long summaryTemplateId;
	private String summaryTemplate;
	private Long cDealId;
	private Integer exportNo;
	private Boolean newArchiveData;
	private Boolean generateSecDemos;
	
	
	/**
	 * @return the plans
	 */
	public Set<Plan> getPlans() {
		return plans;
	}
	/**
	 * @param plans the plans to set
	 */
	public void setPlans(Set<Plan> plans) {
		this.plans = plans;
	}
	/**
	 * @return the portfolioId
	 */
	public Long getPortfolioId() {
		return portfolioId;
	}
	/**
	 * @param portfolioId the portfolioId to set
	 */
	public void setPortfolioId(Long portfolioId) {
		this.portfolioId = portfolioId;
	}
	
	/**
	 * @return the onAirTemplateId
	 */
	public Long getOnAirTemplateId() {
		return onAirTemplateId;
	}
	/**
	 * @param onAirTemplateId the onAirTemplateId to set
	 */
	public void setOnAirTemplateId(Long onAirTemplateId) {
		this.onAirTemplateId = onAirTemplateId;
	}
	/**
	 * @return the onAirTemplate
	 */
	public String getOnAirTemplate() {
		return onAirTemplate;
	}
	/**
	 * @param onAirTemplate the onAirTemplate to set
	 */
	public void setOnAirTemplate(String onAirTemplate) {
		this.onAirTemplate = onAirTemplate;
	}
	/**
	 * @return the revisionTypeId
	 */
	public Long getRevisionTypeId() {
		return revisionTypeId;
	}
	/**
	 * @param revisionTypeId the revisionTypeId to set
	 */
	public void setRevisionTypeId(Long revisionTypeId) {
		this.revisionTypeId = revisionTypeId;
	}
	/**
	 * @return the revisionType
	 */
	public String getRevisionType() {
		return revisionType;
	}
	/**
	 * @param revisionType the revisionType to set
	 */
	public void setRevisionType(String revisionType) {
		this.revisionType = revisionType;
	}
	/**
	 * @return the demo
	 */
	public String getDemo() {
		return demo;
	}
	/**
	 * @param demo the demo to set
	 */
	public void setDemo(String demo) {
		this.demo = demo;
	}
	/**
	 * @return the demoId
	 */
	public Long getDemoId() {
		return demoId;
	}
	/**
	 * @param demoId the demoId to set
	 */
	public void setDemoId(Long demoId) {
		this.demoId = demoId;
	}
	/**
	 * @return the summaryTemplateId
	 */
	public Long getSummaryTemplateId() {
		return summaryTemplateId;
	}
	/**
	 * @param summaryTemplateId the summaryTemplateId to set
	 */
	public void setSummaryTemplateId(Long summaryTemplateId) {
		this.summaryTemplateId = summaryTemplateId;
	}
	/**
	 * @return the summaryTemplate
	 */
	public String getSummaryTemplate() {
		return summaryTemplate;
	}
	/**
	 * @param summaryTemplate the summaryTemplate to set
	 */
	public void setSummaryTemplate(String summaryTemplate) {
		this.summaryTemplate = summaryTemplate;
	}
	/**
	 * @return the cDealId
	 */
	public Long getcDealId() {
		return cDealId;
	}

	/**
	 * @param cDealId the cDealId to set
	 */
	public void setcDealId(Long cDealId) {
		this.cDealId = cDealId;
	}

	/**
	 * @return the exportNo
	 */
	public Integer getExportNo() {
		return exportNo;
	}

	/**
	 * @param exportNo the exportNo to set
	 */
	public void setExportNo(Integer exportNo) {
		this.exportNo = exportNo;
	}

	/**
	 * @return the newArchiveData
	 */
	public Boolean getNewArchiveData() {
		return newArchiveData;
	}

	/**
	 * @param newArchiveData the newArchiveData to set
	 */
	public void setNewArchiveData(Boolean newArchiveData) {
		this.newArchiveData = newArchiveData;
	}
	/**
	 * @return the generateSecDemos
	 */
	public Boolean getGenerateSecDemos() {
		return generateSecDemos;
	}
	/**
	 * @param generateSecDemos the generateSecDemos to set
	 */
	public void setGenerateSecDemos(Boolean generateSecDemos) {
		this.generateSecDemos = generateSecDemos;
	}

}