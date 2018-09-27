package com.nbc.custom_reports.service.olympics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nbc.custom_reports.domain.PayloadErrorLogger;
import com.nbc.custom_reports.domain.olympics.LinearProposalRO;
import com.nbc.custom_reports.domain.olympics.TotalsModel;
import com.nbc.custom_reports.repository.olympics.ConvergenceRepository;
import com.nbc.custom_reports.repository.olympics.LoggerRepository;




@Service
@RefreshScope
public class ConvergenceReportService<K, V> {
	
	@Autowired
    SpringTemplateEngine springTemplateEngine;
	
	@Value("${linearSummaryURI}")
	private String linearSummaryURI;
	
	@Value("${standardHeaders1}")
	private String standardHeaders1;
	
	@Value("${standardHeaders2}")
	private String standardHeaders2;
	
	@Value("${standardHeaders3}")
	private String standardHeaders3;
	
	@Autowired
	DigitalService digitalService;

	@Autowired
	ConvergenceRepository convergenceRepo;	
	
	@Autowired
    private ObjectMapper objectMapper;
	
	@Autowired
	private LoggerRepository loggerRepo;
	
	@Value("${linearProposalSummaryURI}")
	private String linearProposalSummaryURI;
	
	@Autowired
	private RestTemplate restTemplate;

	private static final Logger logger = Logger.getLogger(ConvergenceReportService.class);
	
	@SuppressWarnings("unchecked")
	public Map<String,String> getOlympicsReport(String jsonStr, String sessionId, Long userId, JsonObject reportParams) throws Exception {
		Map<String,String> pdfInputParamMap =new HashMap<String,String>();
		Map<String,Object> conditionMap =new HashMap<String,Object>();
		try {
			List<TotalsModel> totalsList = new ArrayList<TotalsModel>();
			ObjectMapper mapper = new ObjectMapper();
			String finalHtml=null;					
			Map<String,TotalsModel> totalsMap = new HashMap<String, TotalsModel>();
			if (userId == null)
				throw new RuntimeException("SSO Lookup for TAD User failed");

			HashMap<String, HashMap<String, String>> digitalDeals = digitalService.getDigitalParams(jsonStr);
			String linearData = fetchLinearSummaryData(jsonStr);
			Map<K,V> linearSummaryMap = mapper.readValue(linearData, Map.class);			
			Map<String, Map<K, V>> digitalDataMap = digitalService.fetchDigitalData(userId, sessionId, digitalDeals);
			LinearProposalRO linearProposalRO = fetchLinearProposalData(jsonStr);
			Map<K,V> linearDetailsMap  = mapper.readValue(linearProposalRO.getLinearDeal(),Map.class);
			populateConditions(conditionMap,linearDetailsMap,digitalDataMap,reportParams);
			calcTotals(totalsList,digitalDataMap.get("categorySummaries"),linearSummaryMap,totalsMap);
			
			finalHtml = buildHtml(digitalDataMap,linearSummaryMap,totalsMap,conditionMap);
			
			
			StringBuffer combinedHtml = new StringBuffer();
			combinedHtml.append(finalHtml);
			
			combinedHtml.append(linearProposalRO.getLinearHtml());
			combinedHtml.append(digitalService.buildDigitalOrderHtml(digitalDataMap,"DigitalDetailsHeader",false));
			pdfInputParamMap.put("html", combinedHtml.toString());
			pdfInputParamMap.put("linearDetail", linearProposalRO.getLinearDeal());
			pdfInputParamMap.put("procParams",((Map<K,V>)linearSummaryMap.get("dataMap")).get("paramsList").toString());
			//pdfInputParamMap.put("digitalDetail", buildDigitalOrderHtml(digitalData));
		} catch (Exception e) {
			throw e;
		}
		return pdfInputParamMap;
	}

	/**
	 * 
	 * @param conditionMap
	 * @param linearDetailsMap
	 * @param digitalDataMap
	 * All conditions would go in here
	 * @param reportParams 
	 */
	private void populateConditions(Map<String, Object> conditionMap, Map<K, V> linearDetailsMap, Map<String, Map<K, V>> digitalDataMap, JsonObject reportParams) {
		String ratingStream = null;
		conditionMap.put("isMultipleRatingStream","N");
		conditionMap.put("isAdPrdGrpRollup", reportParams.getAsJsonPrimitive("is_adPrdGrp_rollup").getAsBoolean());
		if(linearDetailsMap!=null && linearDetailsMap.get("ratingStream")!=null) {
			ratingStream = linearDetailsMap.get("ratingStream").toString();
		if(ratingStream.indexOf(",")!=-1)
			conditionMap.put("isMultipleRatingStream","Y");
		}
		if(digitalDataMap!=null && digitalDataMap.get("categorySummaries")!=null) {
			if(digitalDataMap.get("categorySummaries").get("video")!=null) 				
				conditionMap.put("videoCostMethod", ((Map<K,V>)digitalDataMap.get("categorySummaries").get("video")).get("costMethod")!=null?((Map<K,V>)digitalDataMap.get("categorySummaries").get("video")).get("costMethod").toString():null);
			if(digitalDataMap.get("categorySummaries").get("nonvideo")!=null) 				
				conditionMap.put("nonVideoCostMethod", ((Map<K,V>)digitalDataMap.get("categorySummaries").get("nonvideo")).get("costMethod")!=null?((Map<K,V>)digitalDataMap.get("categorySummaries").get("nonvideo")).get("costMethod").toString():null);
		}
	}

	public String buildHtml(Map<String, Map<K, V>> digitalDataMap, Map<K, V> linearSummaryMap, Map<String, TotalsModel> totalsMap, Map<String, Object> conditionMap) {
		StringBuilder sb = new StringBuilder();
		try {				
		//sb.append("<html><body>");
		sb.append("  <table group_table_number=\"0\" id=\"1b\" class=\"pdfptable display bottom nbc-first-in-group\" style=\"height: auto; width: auto;\"> ");
		sb.append(buildGrandSummaryHtml(digitalDataMap, linearSummaryMap,totalsMap,conditionMap));
		sb.append("</table>");
		//build details html
		
		//sb.append("</body></html>");
		}catch(Exception e) {
			throw e;
		}
		return sb.toString();
	}
	
	
	
	public String buildGrandSummaryHtml(Map<String, Map<K, V>> digitalDataMap,
			Map<K, V> linearSummaryMap, Map<String, TotalsModel> totalsMap, Map<String, Object> conditionMap) {
		Map<String,String> digitalSummaryHtmlMap = null;
		Map<String,String> linearSummaryHtmlMap = null;
		Map<String,String> totalsHtmlMap = null;
		StringBuilder sb = new StringBuilder();
		String headerHtml = null;
		String spacerHtml = null;
		String tbodyStartTag="<tbody>";
		String tbodyEndTag="</tbody>";
		
		try {
			Collection<? extends String> secDemos = 	(Collection<? extends String>) ((Map<K,V>)linearSummaryMap.get("dataMap")).get("secDemoHeaders");
			boolean hhExists = ((ArrayList)((Map<K,V>)linearSummaryMap.get("dataMap")).get("priDemos")).stream().anyMatch(t -> t.equals("HH"));
			
			digitalSummaryHtmlMap =  digitalService.fetchDigitalHtml(digitalDataMap, secDemos,hhExists,conditionMap);
			linearSummaryHtmlMap = buildLinearSummaryHtml(linearSummaryMap,hhExists);
			totalsHtmlMap = buildTotalsHtml(totalsMap,secDemos,hhExists,conditionMap);
			headerHtml = buildHeaderHtml(linearSummaryMap,hhExists);
			spacerHtml = buildSpacerHtml(linearSummaryMap,hhExists);
			boolean linearGuarSecExists=isLinearSectionIncluded(linearSummaryMap,"guaranteedLinear");
			boolean linearNonGuarSecExists=isLinearSectionIncluded(linearSummaryMap,"nonguaranteedLinear");
			boolean digVideoSecExists=isDigitalSectionIncluded(digitalDataMap.get("categorySummaries"),"video");
			boolean digNonVideoSecExists=isDigitalSectionIncluded(digitalDataMap.get("categorySummaries"),"nonvideo");
			
			if(linearGuarSecExists||digVideoSecExists){
				sb.append(headerHtml);
				sb.append(tbodyStartTag);
			}
			if(linearGuarSecExists){
				sb.append(linearSummaryHtmlMap.get("guaranteedLinearHtml"));
				sb.append(spacerHtml);
			}
			if(digVideoSecExists)
				sb.append(digitalSummaryHtmlMap.get("video"));
			
			if(linearGuarSecExists||digVideoSecExists){
				sb.append(totalsHtmlMap.get("guarVideoTotal"));
				sb.append(tbodyEndTag);
				sb.append(tbodyStartTag);
				sb.append(spacerHtml);
				sb.append(tbodyEndTag);
			}
			if(linearNonGuarSecExists){
				sb.append(headerHtml);
				sb.append(tbodyStartTag);
				sb.append(linearSummaryHtmlMap.get("nonGuaranteedLinearHtml"));	
				sb.append(totalsHtmlMap.get("nonGuarVideoTotal"));
				sb.append(tbodyEndTag);
				sb.append(tbodyStartTag);
				sb.append(spacerHtml);
				sb.append(tbodyEndTag);
			}
			if(digNonVideoSecExists){
				sb.append(headerHtml);
				sb.append(digitalSummaryHtmlMap.get("nonvideo"));
			}
			sb.append(tbodyStartTag);
			sb.append(spacerHtml);
			sb.append(tbodyEndTag);
			sb.append(headerHtml);
			sb.append(tbodyStartTag);
			sb.append(totalsHtmlMap.get("grandTotal"));
			sb.append(tbodyEndTag);
		}catch(Exception e) {
			throw e;
		}
		return sb.toString();
	}

	public Long lookupTADUser() {
		return convergenceRepo.lookupTADUser();
	}
	
	private boolean isLinearSectionIncluded(Map<K, V> linearSummaryMap,String secName){
		ObjectMapper mapper = new ObjectMapper();
		List<TotalsModel> totalsList=new ArrayList<>();
		List<Map<K,V>> totMapList = mapper.convertValue((List<TotalsModel>) ((Map<K,V>)linearSummaryMap.get("dataMap")).get("Totals"), List.class);
		for (Map<K,V> map : totMapList) {
			totalsList.add(mapper.convertValue(map,TotalsModel.class));
		}
		List<TotalsModel> sections= totalsList.stream()
			.filter(p -> p.getType().equalsIgnoreCase(secName)).collect(Collectors.toList());
		TotalsModel section=sections.get(0);
		if(section.getGrossDollars()>0||section.getNetDollars()>0||section.getGrossCpm()>0||section.getNetCpm()>0||section.getImps()>0)
			return true;
		else
			return false;	
	}
	
	private  boolean isDigitalSectionIncluded(Map<K,V> digitalMap,String secName){
		TotalsModel section = null;
		for(K key:digitalMap.keySet()) {
			if(key.toString().equalsIgnoreCase(secName)){
				section = new TotalsModel();
				section.setType(key.toString());
				section.addGrossDollars((Double) ((Map<K,V>)digitalMap.get(key)).get("grossTotal"));
				section.addNetDollars((Double) ((Map<K,V>)digitalMap.get(key)).get("netTotal"));
				section.addGrossCpm((Double) ((Map<K,V>)digitalMap.get(key)).get("grossCpm"));
				section.addNetCpm((Double) ((Map<K,V>)digitalMap.get(key)).get("netCpm"));
				section.addImps(Math.round(Double.valueOf((Double) ((Map<K,V>)digitalMap.get(key)).get("impressions"))));
			}
		}
		if(section.getGrossDollars()>0||section.getNetDollars()>0||section.getGrossCpm()>0||section.getNetCpm()>0||section.getImps()>0)
			return true;
		else
			return false;	
		
	}
	

	@SuppressWarnings("unchecked")
	public void calcTotals(List<TotalsModel> totalsList, Map<K,V> digitalMap, Map<K, V> linearSummaryMap, Map<String, TotalsModel> totalsMap) {
		try{
			ObjectMapper mapper = new ObjectMapper();
			List<Map<K,V>> totMapList = mapper.convertValue((List<TotalsModel>) ((Map<K,V>)linearSummaryMap.get("dataMap")).get("Totals"), List.class);			
			for (Map<K,V> map : totMapList) {
				totalsList.add(mapper.convertValue(map,TotalsModel.class));
			}			
			addDigitalTotals(totalsList,digitalMap);
			calculateGrandTotals("NON GUAR VIDEO TOTALS", totalsList.stream()
					.filter(p -> p.getType().equalsIgnoreCase("nonguaranteedLinear")).collect(Collectors.toList()),totalsMap);
			calculateGrandTotals("GUAR VIDEO TOTALS",totalsList.stream()
					.filter(p -> p.getType().equalsIgnoreCase("guaranteedLinear") || p.getType().equalsIgnoreCase("video")).collect(Collectors.toList()),totalsMap);
			calculateGrandTotals("GRAND TOTAL",
					totalsList.stream().filter(p ->  p.getType().equalsIgnoreCase("guaranteedLinear") || p.getType().equalsIgnoreCase("video") 
							                       		|| p.getType().equalsIgnoreCase("nonguaranteedLinear") || p.getType().equalsIgnoreCase("nonvideo")).collect(Collectors.toList()),totalsMap);
		}catch(Exception e){
			throw new RuntimeException("Error occured while calculating summary totals");
		}
	}

	@SuppressWarnings("unchecked")
	private void addDigitalTotals(List<TotalsModel> totalModels, Map<K,V> digitalMap) {
		try{
		TotalsModel totalsModel = null;
				for(K key:digitalMap.keySet()) {
					totalsModel = new TotalsModel();
					totalsModel.setType(key.toString());
					totalsModel.addGrossDollars((Double) ((Map<K,V>)digitalMap.get(key)).get("grossTotal"));
					totalsModel.addNetDollars((Double) ((Map<K,V>)digitalMap.get(key)).get("netTotal"));
					totalsModel.addGrossCpm((Double) ((Map<K,V>)digitalMap.get(key)).get("grossCpm"));
					totalsModel.addNetCpm((Double) ((Map<K,V>)digitalMap.get(key)).get("netCpm"));
					//totalsModel.addImps((long) ((Map<K,V>)digitalMap.get(key)).get("impressions"));
					if(((Map<K,V>)digitalMap.get(key)).get("groups")!=null) {
						for(Map<K,V> groupsMap:(List<Map<K,V>>)((Map<K,V>)digitalMap.get(key)).get("groups")) {
							if(groupsMap!=null)
								totalsModel.addImps(((Double)groupsMap.get("rawImpressions")).longValue());
						}
					}
					totalModels.add(totalsModel);
				}	
		}catch(Exception e){
			throw e;
		}
	}
	
	public void calculateGrandTotals(String type, List<TotalsModel> totalModels, Map<String,TotalsModel> totalsMap) {
		try{
			TotalsModel totalsModel = new TotalsModel();
			for (TotalsModel totModel : totalModels) {
				totalsModel.setType(type);
				totalsModel.addGrossDollars(totModel.getGrossDollars());
				totalsModel.addNetDollars(totModel.getNetDollars());
				totalsModel.addGrossCpm(totModel.getGrossCpm());
				totalsModel.addNetCpm(totModel.getNetCpm());
				totalsModel.addImps(totModel.getImps()); //raw imps
			}		
			if(type!=null && type.equalsIgnoreCase("GUAR VIDEO TOTALS") && totalsModel.getImps()>0) {
				totalsModel.setGrossCpm(totalsModel.getGrossDollars()/totalsModel.getImps()*1000);
				totalsModel.setNetCpm((totalsModel.getNetDollars()/totalsModel.getImps())*1000);
			}
			totalsModel.setImps(totalsModel.getImps()/1000);
			totalsMap.put(type,totalsModel);
		}catch(Exception e){
			throw e;
		}
	}
	
	public String fetchLinearSummaryData(String jsonStr) {
		StringBuilder res = new StringBuilder();
		try {
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();         
        //params.add("jsonStr", jsonStr);       
        //HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(params, headers);
        HttpEntity<String> requestEntity = new HttpEntity<String>(jsonStr, headers);

        res.append(restTemplate.postForObject(linearSummaryURI, requestEntity, String.class));
		}catch (HttpServerErrorException e) {
            JsonObject json = (new JsonParser()).parse(e.getResponseBodyAsString()).getAsJsonObject();
            throw new RuntimeException("Error occurred while generating linear grand summary details: " + json.getAsJsonPrimitive("message").getAsString());
        }
        return res.toString();		
	}
	/*
	 * This method will fetch the linear proposal details by connecting CIR service
	 */
	public LinearProposalRO fetchLinearProposalDataByPlans(String jsonStr) throws Exception {
		StringBuilder res = new StringBuilder();
		LinearProposalRO linearDetailRO =null;
		try {
			HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        /*MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
	        params.add("jsonStr",jsonStr);  */
	        HttpEntity<String> requestEntity = new HttpEntity<String>(jsonStr, headers);
	        RestTemplate restTemplate = new RestTemplate();        
	        res.append(restTemplate.postForObject(linearProposalSummaryURI, requestEntity, String.class));
	        linearDetailRO = objectMapper.readValue(res.toString(), LinearProposalRO.class);
	    }catch (HttpClientErrorException e) {
            JsonObject json = (new JsonParser()).parse(e.getResponseBodyAsString()).getAsJsonObject();
            if(json.getAsJsonPrimitive("exception")!=null){
            	String errorMsg=json.getAsJsonPrimitive("exception").getAsString().substring(json.getAsJsonPrimitive("exception").getAsString().indexOf(":")+1, json.getAsJsonPrimitive("exception").getAsString().length());
            	throw new RuntimeException("Error occurred while generating linear details: " + errorMsg);
            }
            
        }
        return linearDetailRO;		
	}
	
	public LinearProposalRO fetchLinearProposalData(String jsonStr) throws Exception {
		StringBuilder res = new StringBuilder();
		LinearProposalRO linearDetailRO =null;
		try {
			HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
	        params.add("jsonStr",jsonStr);  
	        RestTemplate restTemplate = new RestTemplate();        
	        res.append(restTemplate.postForObject(linearProposalSummaryURI, jsonStr, String.class));
	        linearDetailRO = objectMapper.readValue(res.toString(), LinearProposalRO.class);
	    }catch (HttpClientErrorException e) {
            JsonObject json = (new JsonParser()).parse(e.getResponseBodyAsString()).getAsJsonObject();
            if(json.getAsJsonPrimitive("exception")!=null){
            	String errorMsg=json.getAsJsonPrimitive("exception").getAsString().substring(json.getAsJsonPrimitive("exception").getAsString().indexOf(":")+1, json.getAsJsonPrimitive("exception").getAsString().length());
            	throw new RuntimeException("Error occurred while generating linear details: " + errorMsg);
            }
            
        }
        return linearDetailRO;		
	}
	
	
	public String buildHeaderHtml(Map<K, V> linearSummaryMap,boolean hhExists) {
		List<String> headers = null;
		Context context = new Context();
		try {
			
			headers=(Arrays.stream(standardHeaders1.split(",")).collect(Collectors.toList()));
			headers.addAll((Collection<? extends String>) ((Map<K,V>)linearSummaryMap.get("dataMap")).get("priDemoHeaders"));
			headers.addAll(Arrays.stream(standardHeaders2.split(",")).collect(Collectors.toList()));
			if(!hhExists){
				headers.addAll(Arrays.stream(standardHeaders3.split(",")).collect(Collectors.toList()));
			}
			headers.addAll((Collection<? extends String>) ((Map<K,V>)linearSummaryMap.get("dataMap")).get("secDemoHeaders"));
			headers.replaceAll(x -> x.replaceAll(" ", "<br/>"));
			context.setVariable("headers", headers);
		return springTemplateEngine.process("Header", context);
		}catch(Exception e) {
			throw e;
		}
	}
	
	public String buildSpacerHtml(Map<K, V> linearSummaryMap,boolean hhExists) {
		List<String> headers = null;
		Context context = new Context();
		try {
			headers=(Arrays.stream(standardHeaders1.split(",")).collect(Collectors.toList()));
			headers.addAll((Collection<? extends String>) ((Map<K,V>)linearSummaryMap.get("dataMap")).get("priDemoHeaders"));
			headers.addAll(Arrays.stream(standardHeaders2.split(",")).collect(Collectors.toList()));
			if(!hhExists){
				headers.addAll(Arrays.stream(standardHeaders3.split(",")).collect(Collectors.toList()));
			}
		headers.addAll((Collection<? extends String>) ((Map<K,V>)linearSummaryMap.get("dataMap")).get("secDemoHeaders"));
		headers.replaceAll(x -> x.replaceAll(" ", "<br/>"));
		context.setVariable("headers", headers);
		return springTemplateEngine.process("Spacer", context);
		}catch(Exception e) {
			throw e;
		}
	}			
	
	public Map<String, String> buildLinearSummaryHtml(Map<K, V> linearSummaryMap,boolean hhExists) {
		Map<String,String> linearSummaryHtmlMap = new HashMap<String,String>();
		//List<String> headers = null;
		try {
			Context context = new Context();	
			context.setVariable("secDemos", (Collection<? extends String>) ((Map<K,V>)linearSummaryMap.get("dataMap")).get("secDemoHeaders"));
			context.setVariable("guaranteedLinear",((Map<K,V>)linearSummaryMap.get("dataMap")).get("guaranteed"));
			context.setVariable("nonGuaranteedLinear",((Map<K,V>)linearSummaryMap.get("dataMap")).get("nonguaranteed"));
			context.setVariable("hhExists","N");
			if(hhExists){
				context.setVariable("hhExists","Y");
			}
			
			linearSummaryHtmlMap.put("guaranteedLinearHtml", springTemplateEngine.process("GuaranteedLinear", context).replaceAll("<span>", "").replaceAll("</span>", ""));
			linearSummaryHtmlMap.put("nonGuaranteedLinearHtml", springTemplateEngine.process("NonGuaranteedLinear", context).replaceAll("<span>", "").replaceAll("</span>", ""));			
		}catch(Exception e) {
			throw e;
		}
		return linearSummaryHtmlMap;
	}
	
	public Map<String,String> buildTotalsHtml(Map<String,TotalsModel> totalsMap, Collection<? extends String> secDemos,boolean hhExists, Map<String, Object> conditionMap){
		Map<String,String> totalsHtml = new HashMap<String,String>();
		try {
			Context context = new Context();
			context.setVariable("hhExists","N");
			if(hhExists){
				context.setVariable("hhExists","Y");
			}
			context.setVariable("isMultipleRatingStream", conditionMap.get("isMultipleRatingStream"));
			context.setVariable("secDemos",secDemos);
			context.setVariable("costMethod",conditionMap.get("videoCostMethod"));
			context.setVariable("totalsModel", totalsMap.get("GUAR VIDEO TOTALS"));
			totalsHtml.put("guarVideoTotal", springTemplateEngine.process("Totals", context));
			context.setVariable("totalsModel", totalsMap.get("NON GUAR VIDEO TOTALS"));
			context.setVariable("grossCpm", "N");
			context.setVariable("netCpm", "N");
			totalsHtml.put("nonGuarVideoTotal", springTemplateEngine.process("Totals", context));			
			context.setVariable("totalsModel", totalsMap.get("GRAND TOTAL"));
			context.setVariable("grossCpm", "N");
			context.setVariable("netCpm", "N");
			context.setVariable("imps", "N");
			totalsHtml.put("grandTotal", springTemplateEngine.process("Totals", context));
			
		}catch(Exception e){
			throw e;
		}
		return totalsHtml;
	}
	
	@Transactional
	public void logPayloadAndError(PayloadErrorLogger logger) {
		loggerRepo.save(logger);
	}
	
}