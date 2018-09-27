package com.nbc.custom_reports.domain.methodman;

import java.util.List;
import java.util.Set;

public class ConvergenceDealData {
	
	private Demo demo;
	private Template onairTemplate;
	private RevisionType revisionType;
	private LinearDeal linearDetails;
	private List<DigitalDeal> digitalOrders;

	/**
	 * @return the digitalOrders
	 */
	public List<DigitalDeal> getDigitalOrders() {
		return digitalOrders;
	}

	/**
	 * @param digitalOrders the digitalOrders to set
	 */
	public void setDigitalOrders(List<DigitalDeal> digitalOrders) {
		this.digitalOrders = digitalOrders;
	}

	/**
	 * @return the demo
	 */
	public Demo getDemo() {
		return demo;
	}

	/**
	 * @param demo the demo to set
	 */
	public void setDemo(Demo demo) {
		this.demo = demo;
	}

	

	/**
	 * @return the onairTemplate
	 */
	public Template getOnairTemplate() {
		return onairTemplate;
	}

	/**
	 * @param onairTemplate the onairTemplate to set
	 */
	public void setOnairTemplate(Template onairTemplate) {
		this.onairTemplate = onairTemplate;
	}

	/**
	 * @return the revisionType
	 */
	public RevisionType getRevisionType() {
		return revisionType;
	}

	/**
	 * @param revisionType the revisionType to set
	 */
	public void setRevisionType(RevisionType revisionType) {
		this.revisionType = revisionType;
	}

	/**
	 * @return the linearDetails
	 */
	public LinearDeal getLinearDetails() {
		return linearDetails;
	}

	/**
	 * @param linearDetails the linearDetails to set
	 */
	public void setLinearDetails(LinearDeal linearDetails) {
		this.linearDetails = linearDetails;
	}



	public class Demo{
		

	    private String name;
		private Long id;
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
		 * @return the id
		 */
		public Long getId() {
			return id;
		}
		/**
		 * @param id the id to set
		 */
		public void setId(Long id) {
			this.id = id;
		}
		
		
	}
	
	public class Template{
		
		private String name;
		private Long id;
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
		 * @return the id
		 */
		public Long getId() {
			return id;
		}
		/**
		 * @param id the id to set
		 */
		public void setId(Long id) {
			this.id = id;
		}
		
		
	}
	
	public class RevisionType{
		
		private String name;
		private Long id;
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
		 * @return the id
		 */
		public Long getId() {
			return id;
		}
		/**
		 * @param id the id to set
		 */
		public void setId(Long id) {
			this.id = id;
		}
		
		
	}
	
	public static class LinearDeal{
		
		private Set<Plan> plans;
		
		
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
		
		
		public static class Plan{
			
			public Plan() {
				super();
	        }
			
			private Long planId;
			private Long portfolioId;
			private boolean enabled;
			
			
			/**
			 * @return the enabled
			 */
			public boolean isEnabled() {
				return enabled;
			}

			/**
			 * @param enabled the enabled to set
			 */
			public void setEnabled(boolean enabled) {
				this.enabled = enabled;
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
			 * @return the id
			 */
			public Long getPlanId() {
				return planId;
			}

			/**
			 * @param id the id to set
			 */
			public void setPlanId(Long id) {
				this.planId = id;
			}
			
		}
		
	}
	
	public static class DigitalDeal{
		
		public DigitalDeal() {
			super();
        }
		
		private Long orderId;

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
		
		

		
	}
	
	

}