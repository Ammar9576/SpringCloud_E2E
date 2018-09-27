package com.nbc.custom_reports.domain.olympics;

public class LinearProposalRO {
	private String linearDeal;
	private String linearHtml;
	   
	  public LinearProposalRO(String linearDeal, String linearHtml){
		  this.linearDeal = linearDeal;
		  this.linearHtml = linearHtml;
	  }
	public LinearProposalRO() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the linearDeal
	 */
	public String getLinearDeal() {
		return linearDeal;
	}
	/**
	 * @param linearDeal the linearDeal to set
	 */
	public void setLinearDeal(String linearDeal) {
		this.linearDeal = linearDeal;
	}
	/**
	 * @return the linearHtml
	 */
	public String getLinearHtml() {
		return linearHtml;
	}
	/**
	 * @param linearHtml the linearHtml to set
	 */
	public void setLinearHtml(String linearHtml) {
		this.linearHtml = linearHtml;
	}

   
}