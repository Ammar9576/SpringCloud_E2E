package com.nbc.custom_reports.service.methodman;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nbc.custom_reports.domain.methodman.Dashboard;
import com.nbc.custom_reports.domain.methodman.Payload;
import com.nbc.custom_reports.domain.methodman.Rollup;

@Service
@RefreshScope
public class DashboardService<K,V> {
	
	@Value("${linearRollupURI}")
	private String linearRollupURI;
	
	@Value("${digitalRollupURI}")
	private String digitalRollupURI;
	
	@Value("${dashboardAdminURI}")
	private String dashboardAdminURI;
	
	@Value("${convergenceDealURI}")
	private String convergenceDealURI;
	
	@Autowired
	private RestTemplate restTemplate;
			
	@SuppressWarnings("unchecked")
	public String getDashboardDetails(String convgDealId) throws Exception {
		List<HashMap<String,String>> dashboardAdminList  = null;
		Dashboard dashboard = new Dashboard();		
		String dashBoardjson = null;
		double  linearIndex = 0, digitalIndex = 0;
		HashMap<K,V> convergenceDealMap = null;
		HashMap<K,V> linearDetailsMap = null;
		long benchmark = 0;			
		Gson gson = new Gson();
		HashMap<K,V> digitalRollupMap = null;
		List<Payload> payloadList = null;
		HashMap<K,Double> linearRollupMap = null;
		Rollup rollup = null;
		Double hvcPercent;				
		try {
			convergenceDealMap = (HashMap<K,V>)getConvergenceDeal(convgDealId);
		if(convergenceDealMap!=null) {	
			linearDetailsMap = (HashMap<K,V>)convergenceDealMap.get("linearDetails");
			List<HashMap> linearPlans = (ArrayList<HashMap>)linearDetailsMap.get("plans");
			//Integer portfolioId = (Integer)linearDetailsMap.get("portfolioId");
			List<HashMap> digitalDeals = (ArrayList<HashMap>)convergenceDealMap.get("digitalOrders");
			HashMap<K,V> revTypeMap = (HashMap<K,V>)convergenceDealMap.get("revisionType");
			StringBuilder digitalDealIds = new StringBuilder();
			for(HashMap digitalDeal : digitalDeals) {
				if(digitalDeal!=null && digitalDeal.get("orderId")!=null)
					digitalDealIds.append(((Integer)digitalDeal.get("orderId"))).append(",");
			}
			if(digitalDealIds.length()>0)
				digitalDealIds.deleteCharAt(digitalDealIds.length()-1);
			digitalRollupMap  = fetchDigitalImpsAndDollars(digitalDealIds.toString());	
			if(linearPlans!=null && linearPlans.size()>0 && revTypeMap!=null) { 
				payloadList = buildPayload(linearPlans,revTypeMap);
			    linearRollupMap = fetchLinearImpsAndDollars(gson.toJson(payloadList));
		    }
			rollup = new Rollup();
			setDollars(rollup, linearRollupMap, digitalRollupMap);
			setImps(rollup, linearRollupMap, digitalRollupMap);			
			//get admin data
			dashboardAdminList  = getDashboardAdminData();
			if(dashboardAdminList!=null) {		
				for(HashMap<String,String> adminMap:dashboardAdminList) {
					if("linearIndex".equalsIgnoreCase(adminMap.get("name").toString()))				
						linearIndex = Double.valueOf(adminMap.get("value"));
					else if("digitalIndex".equalsIgnoreCase(adminMap.get("name").toString()))
						digitalIndex = Double.valueOf(adminMap.get("value"));
					else if("moatBenchmark".equalsIgnoreCase(adminMap.get("name").toString()))
						benchmark = Long.valueOf(adminMap.get("value"));
					}	
				}		
				hvcPercent = ((digitalIndex * (rollup.getDigitalImps()/rollup.getTotalImps())) + (linearIndex * (rollup.getLinearImps()/rollup.getTotalImps()))) * 100;
				hvcPercent = hvcPercent.isNaN()?0:hvcPercent;
				hvcPercent = BigDecimal.valueOf(hvcPercent).setScale(getScale(hvcPercent),BigDecimal.ROUND_HALF_UP).stripTrailingZeros().doubleValue();
				setDashboardDetails(dashboard, convergenceDealMap,rollup,hvcPercent,benchmark);
				dashBoardjson =  gson.toJson(dashboard);		
	    }else {
	       throw new Exception("Deal does not exist!");	
	    }
		}catch(Exception e) {
			throw e;
		}
		return dashBoardjson;
	}
		
	@SuppressWarnings("unchecked")
	public HashMap<K,V> fetchDigitalImpsAndDollars(String planIds) {		
		HashMap<K,V> digitalRollupMap = new HashMap<K,V>();
		try {			
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);              

        String url = digitalRollupURI+"planIds[]="+planIds;
        digitalRollupMap =  restTemplate.getForObject(url,HashMap.class);
		}catch (HttpServerErrorException e) {
            JsonObject json = (new JsonParser()).parse(e.getResponseBodyAsString()).getAsJsonObject();
            throw new RuntimeException("Error occurred while generating digital rollup: " + json.getAsJsonPrimitive("message").getAsString());
        }
        return digitalRollupMap;		
	}	
	
	@SuppressWarnings("unchecked")
	public HashMap<K,Double> fetchLinearImpsAndDollars(String payload) {		
		HashMap<K,Double> linearRollupMap = new HashMap<K,Double>();
		try {			
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<String>(payload, headers);        

        linearRollupMap = restTemplate.postForObject(linearRollupURI, requestEntity, HashMap.class);
		}catch (HttpServerErrorException e) {
            JsonObject json = (new JsonParser()).parse(e.getResponseBodyAsString()).getAsJsonObject();
            throw new RuntimeException("Error occurred while generating linear rollup: " + json.getAsJsonPrimitive("message").getAsString());
        }
        return 	linearRollupMap;
	}	
	
	@SuppressWarnings("unchecked")
	public HashMap<K,V> getConvergenceDeal(String dealId) throws Exception {
		HashMap<K,V> convergenceDealResponseMap = null;
		HashMap<K,V> convergenceDealMap = null;
		try {
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);              

        convergenceDealResponseMap =  restTemplate.getForObject(convergenceDealURI+dealId,HashMap.class);
        convergenceDealMap = (HashMap<K,V>)convergenceDealResponseMap.get("response");
		}catch(Exception e) {
			throw e;
		}
        return convergenceDealMap;
	}

	private List<Payload> buildPayload(List<HashMap> linearPlans,HashMap<K,V> revTypeMap) {
		Payload payload = null;
		List<Payload> payloadList = new ArrayList<Payload>();
		for(HashMap plan : linearPlans) {
			if(plan!=null && (Boolean)plan.get("enabled")) {
				payload = new Payload();
				payload.setPlanId((Integer)plan.get("planId"));
				payload.setRevType((Integer)revTypeMap.get("id"));
				payloadList.add(payload);
			}
		}		
		return payloadList;
	}
	
	@SuppressWarnings("unchecked")
	public List<HashMap<String,String>> getDashboardAdminData() throws Exception{
		HashMap<K,V> dashboardAdminMap = null;
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);              

        dashboardAdminMap =  restTemplate.getForObject(dashboardAdminURI,HashMap.class);
        if(!(Boolean) dashboardAdminMap.get("success"))
        	throw new Exception((String)dashboardAdminMap.get("response"));
        return (dashboardAdminMap!=null?(List<HashMap<String,String>>)dashboardAdminMap.get("response"):null);
	}
	
	private void setDollars(Rollup rollup , HashMap<K,Double> linearRollupMap, HashMap<K,V> digitalRollupMap) {
		Double totalDollars;		
		try {			
		rollup.setLinearDollars(linearRollupMap!=null?BigDecimal.valueOf(linearRollupMap.get("dollars")).longValue():0);
		rollup.setDigitalDollars(digitalRollupMap!=null?BigDecimal.valueOf(((HashMap<K,Double>)digitalRollupMap.get("categorySummaries")).get("totalGrossDollars")).longValue():0);
		totalDollars = (rollup.getLinearDollars()+rollup.getDigitalDollars())/1000000D;
		rollup.setTotalDollars(BigDecimal.valueOf(totalDollars).setScale(getScale(totalDollars),BigDecimal.ROUND_HALF_UP).stripTrailingZeros().doubleValue());
		}catch(Exception e) {
			throw e;
		}
	}
	
	private void setImps(Rollup rollup , HashMap<K,Double> linearRollupMap, HashMap<K,V> digitalRollupMap) {
		Double linearImps,totalImps;
		Double digitalImps;		
		try {
		linearImps = linearRollupMap!=null?linearRollupMap.get("impressions")/1000000:0;
		digitalImps = digitalRollupMap!=null?((HashMap<K,Integer>)digitalRollupMap.get("categorySummaries")).get("totalImpressions")/1000000D:0.0;
		totalImps = linearImps + digitalImps;
		rollup.setLinearImps(BigDecimal.valueOf(linearImps).setScale(getScale(linearImps),BigDecimal.ROUND_HALF_UP).stripTrailingZeros().doubleValue());
		rollup.setDigitalImps(BigDecimal.valueOf(digitalImps).setScale(getScale(digitalImps),BigDecimal.ROUND_HALF_UP).stripTrailingZeros().doubleValue());
		rollup.setTotalImps(BigDecimal.valueOf(totalImps).setScale(getScale(totalImps),BigDecimal.ROUND_HALF_UP).stripTrailingZeros().doubleValue());
		}catch(Exception e) {
			throw e;
		}
	}
	
	private void setDashboardDetails(Dashboard dashboard,HashMap<K,V> convergenceDealMap,Rollup rollup,Double hvcPercent,long benchmark) {
		try {
			dashboard.setConvgDealId((Integer)convergenceDealMap.get("id"));
			dashboard.setDollarsAndImps(rollup);						
			dashboard.setHvcPercent(BigDecimal.valueOf(hvcPercent).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
			dashboard.setBenchmark(benchmark);
			HashMap<K,V> dashboardMap = ((HashMap<K,V>)convergenceDealMap.get("dashboard"));		
			if(dashboardMap!=null) {
				dashboard.setConsumerTargets(dashboardMap!=null && dashboardMap.get("consumerTargets")!=null?dashboardMap.get("consumerTargets").toString():"");
				dashboard.setDemoAverageFrequency((Double)dashboardMap.get("demoAverageFrequency"));
				dashboard.setDemoReachPercent((Double)dashboardMap.get("demoReachPercent"));
				dashboard.setConsumerAverageFrequency((Double)dashboardMap.get("consumerAverageFrequency"));
				dashboard.setConsumerReachPercent((Double)dashboardMap.get("consumerReachPercent"));
				dashboard.setMoatScore((Integer)dashboardMap.get("moatScore"));
				dashboard.setIncludeDashboard((Boolean)dashboardMap.get("includeDashboard"));
			}
		}catch(Exception e) {
			throw e;
		}
	}
	
	private static int getScale(Number value) {
		int scale=1;
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(6);
		String totalDollarsStr = df.format(value);
		if(totalDollarsStr.indexOf(".")>-1) {
		String numberStr = totalDollarsStr.substring(0,totalDollarsStr.indexOf("."));
		if(numberStr!=null && numberStr.length()>0 && Long.valueOf(numberStr)>0) {
			scale = 1;
		}else {
			String decimalStr = totalDollarsStr.substring(totalDollarsStr.indexOf(".")+1,totalDollarsStr.length());
			for (Character c : decimalStr.toCharArray()) {
				if(Integer.valueOf(c.toString())>0)				
					break;
				else
					scale++;
			}
		}
		}
		return scale;
	}
	
}
 