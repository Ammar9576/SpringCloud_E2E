package com.nbc.custom_reports.service.methodman;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;
import com.nbc.custom_reports.domain.methodman.ConvergenceDealData;
import com.nbc.custom_reports.domain.methodman.ConvergenceDealData.LinearDeal.Plan;
import com.nbc.custom_reports.domain.methodman.DetailsObj;
import com.nbc.custom_reports.domain.methodman.Input;
import com.nbc.custom_reports.domain.methodman.LinearDetail;
import com.nbc.custom_reports.domain.methodman.Quarters;
import com.nbc.custom_reports.domain.methodman.Summary;
import com.nbc.custom_reports.domain.methodman.TargetGroup;
import com.nbc.custom_reports.domain.olympics.LinearProposalRO;
import com.nbc.custom_reports.repository.methodman.QuartersRepository;
import com.nbc.custom_reports.service.olympics.ConvergenceReportService;
import com.nbc.custom_reports.service.olympics.DigitalService;
import com.nbc.custom_reports.util.olympics.DigitalClientException;
import com.nbc.custom_reports.validator.methodman.ConvergenceDealDataValidator;



@Service
@RefreshScope
public class SummaryService<K, V> {
	
	@Value("${linearSummaryQuarterlyURI}")
	private String linearSummaryURI;
	
	@Value("${cDealDeleteURI}")
	private String cDealDeleteURI;
	
	@Value("${LinearNDigitalDeleteURI}")
	private String LinearNDigitalDeleteURI;
		
	@Value("${digitalQuarterlyURI}")
	private String digitalQuarterlyURI;
	
	@Value("${covergenceDealURI}")
	private String covergenceDealURI;
	
	@Value("${adminDataByNameURI}")
	private String adminDataByNameURI;
	
	@Value("${convergenceArchiveSaveURI}")
	private String convergenceArchiveSaveURI;
	
	@Autowired
	ConvergenceReportService convergenceService;
	
	@Autowired
	DigitalService digitalService;
	
	
	@Autowired
    private ObjectMapper objectMapper;
	
	@Autowired
	private QuartersRepository quartersRepository;
	
	@Autowired
	private RestTemplate restTemplate;

	//private static final Logger logger = Logger.getLogger(SummaryService.class);
	
	@Autowired
	private ConvergenceDealDataValidator convergenceDealDataValidator;
	
	/*
	 * Deserialize the convergence data from Mongo DB and Validate it
	 */
	private Map<String,Object> deserializeConvergenceResponseData(Map<K,V> convergenceDealResponseKeyDetail){
		Map<String,Object> linearDigitalOrganizedDataMap=new HashMap<String,Object>();
		try{
			
			List<String> keyList=new ArrayList<String>();
			keyList.add("demo");
			keyList.add("onairTemplate");
			keyList.add("revisionType");
			keyList.add("linearDetails");
			keyList.add("digitalOrders");
			
			Map<K, V> collect = (Map<K, V>) convergenceDealResponseKeyDetail.entrySet().stream()
								 .filter(map -> keyList.contains(map.getKey()))
								 .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()!=null?p.getValue():""));
			if(collect!=null){
				String jsonStr=objectMapper.writeValueAsString(collect);
				ConvergenceDealData convergenceDealData = objectMapper.readValue( jsonStr, ConvergenceDealData.class);
				 BeanPropertyBindingResult errors=new BeanPropertyBindingResult(convergenceDealData,"convergenceDealData");
				 convergenceDealDataValidator.validate(convergenceDealData,errors);
				 if(errors.hasErrors()){
					 String errorMessage=errors.getAllErrors().stream().map(a -> ","+a.getCode()).reduce("", String::concat);
					 if(errorMessage!=null){
						 throw new RuntimeException("Missing specified convergence deal data - "+errorMessage.substring(1, errorMessage.length()));
					 }
				 }
				 LinearDetail linearDetail=new LinearDetail();
				linearDetail.setDemoId(convergenceDealData.getDemo().getId());
				linearDetail.setDemo(convergenceDealData.getDemo().getName());
				linearDetail.setOnAirTemplate(convergenceDealData.getOnairTemplate().getName());
				linearDetail.setOnAirTemplateId(convergenceDealData.getOnairTemplate().getId());
				linearDetail.setRevisionType(convergenceDealData.getRevisionType().getName());
				linearDetail.setRevisionTypeId(convergenceDealData.getRevisionType().getId());
				
				//linearDetail.setPortfolioId(convergenceDealData.getLinearDetails().getPortfolioId());
				
				List<Set<Plan>> planList=new ArrayList<Set<Plan>>();
				planList.add(convergenceDealData.getLinearDetails().getPlans());
				linearDetail.setPlans(convergenceDealData.getLinearDetails().getPlans().stream().filter(x -> x.isEnabled()).collect(Collectors.toSet()));
				Plan plan = linearDetail.getPlans().stream().findFirst().get();
				linearDetail.setPortfolioId(plan.getPortfolioId());
				
				List<LinearDetail> linearList=new ArrayList<LinearDetail>();
				linearList.add(linearDetail);
				linearDigitalOrganizedDataMap.put("linear", linearList);
				
				List<Long> digitalIds= convergenceDealData.getDigitalOrders().stream().map(e -> e.getOrderId()).collect(Collectors.toList()); 
				linearDigitalOrganizedDataMap.put("digital", digitalIds);
				
			}
			
		}catch(Exception e){
			throw new RuntimeException("Error while deserializing convergence data :"+e.getMessage());
		}	
		
		return linearDigitalOrganizedDataMap;
	}
	
	
	
	
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> getGrandSummaryDataAndDetails(Long dealId, String sso) throws Exception {
		Map<String,Object> dataMap= new LinkedHashMap<String,Object>();
		Map<String,List<Summary>> summaryMap=new HashMap<String,List<Summary>> ();
		Integer exportNo=null;
		try {
			/*if (userId == null)
				throw new RuntimeException("SSO Lookup for TAD User failed");*/
			
			Map<K,V> convergenceDealDataMap=fetchConvergenceDealData(dealId);
			
			Map<K,V> convergenceResponseKeyDetail =  (Map<K, V>) convergenceDealDataMap.get("response");
			
			String linearStr=null;
			List<Long> digitalList=null;
			
			if((Boolean) convergenceDealDataMap.get("success")){				
				Map<String,Object> cMap= deserializeConvergenceResponseData(convergenceResponseKeyDetail);
				List<LinearDetail> linearList=(List<LinearDetail>)cMap.get("linear");
				if(sso!=null){
					Map<K,V> convergenceDealArchiveMap=createConvergenceArchiveDealData(dealId,sso);
					if(Boolean.valueOf(convergenceDealArchiveMap.get("success").toString())){
						exportNo=Integer.valueOf(convergenceDealArchiveMap.get("response").toString());
						for (LinearDetail linearDetail : linearList) {
							linearDetail.setcDealId(dealId);
							linearDetail.setExportNo(exportNo);
							linearDetail.setNewArchiveData(true);
						}
					}
					else{
						throw new RuntimeException("Convergence Archive creation failed");
					}
				}
				Boolean generateSecDemos=Boolean.parseBoolean(getAdminDataByName("Generate Secondary Demos"));
				linearList.forEach(each->each.setGenerateSecDemos(generateSecDemos));
				linearStr=objectMapper.writeValueAsString(linearList);
				digitalList=(List<Long>)cMap.get("digital");
			}else{
				throw new RuntimeException("Convergence deal data is empty or not exist");
			}
			
			
			Map<String, Map> linearQuarterlySummaryMap = fetchLinearQuarterlySummaryData(linearStr);
			Map<String, Map> digitalQuarterlySummaryMap = fetchDigitalQuarterlySummaryData(digitalList,dealId,exportNo);
			
			//Object to JSON in file
			/*Map<String, Map> linearQuarterlySummaryMap = objectMapper.readValue(new File("C:/Users/206426113/Desktop/methodman_sample/linear.json"), Map.class);
			digitalList=new ArrayList<Long>();
			digitalList.add(5043090L);
			digitalList.add(5043096L);
			digitalList.add(5040038L);
			digitalList.add(5040020L);
			digitalList.add(5043089L);
			Map<String, Map> digitalQuarterlySummaryMap = fetchDigitalQuarterlySummaryData(userId, sessionId, digitalList);*/
			
			Map<K,V> demoMap =  (Map<K, V>) convergenceResponseKeyDetail.get("demo");
			String linearprimDemo=(String) demoMap.get("name");
			
			List<Quarters> quarterList=quartersRepository.findByCalendarId(-2L);
			//Map<String, Long> quarterMap = quarterList.stream().collect(Collectors.toMap(Quarters::getName, Quarters::getOrderNo));
			//quarterMap.put("Total", 9999L);
			//158428509 
			Map<String, Long> quarterMap = quarterList.stream().collect(Collectors.toMap(Quarters::getName, Quarters::getYear));
			quarterMap.put("Total", 999999L);
			//158428509 
			
			DetailsObj detailsObj=new DetailsObj();
			detailsObj.setLinearMap(linearQuarterlySummaryMap);
			detailsObj.setDigitalMap(digitalQuarterlySummaryMap);
			detailsObj.setDemoName(linearprimDemo);
			detailsObj.setQuarterMap(quarterMap);
			
			// 158365199
			detailsObj.setGuarCpmFlag(false);
			if (convergenceResponseKeyDetail.get("guaranteedCPM") != null) {
				Boolean guaranteedCPM = (Boolean)convergenceResponseKeyDetail.get("guaranteedCPM");
				detailsObj.setGuarCpmFlag(guaranteedCPM);
				//detailsObj.setGuarCpmFlag(true);
			}
			// 158365199
			
			// Digital Starts
			Map<String, Object> digitalSummaryMap=createDigitalSummaryObj(detailsObj);
			// Linear starts
			Map<String, Object> linearSummaryMap=createLinearSummaryObj(detailsObj);
			
			// Adding missed out Quarters
			List<String> linearQuarters=(List<String>)linearSummaryMap.get("linearQuarters");
			List<String> digitalQuarters= (List<String>)digitalSummaryMap.get("digitalQuarters");
			
			List<Summary> linearSummaryList=(List<Summary>)linearSummaryMap.get("linearSummaryList");
			List<Summary> digitalSummaryList= (List<Summary>)digitalSummaryMap.get("digitalSummaryList");
			
			Set<String> linearQuartersSet	 = new HashSet<String>(linearQuarters);
			linearQuartersSet.removeAll(digitalQuarters);
			
			Set<String> digitalQuarterSet = new HashSet<String>(digitalQuarters);
			digitalQuarterSet .removeAll(linearQuarters);
			
			if(linearQuartersSet!=null && linearQuartersSet.size()>0){
				linearQuartersSet.forEach(v->{
					Summary summary= blankSummaryObject(linearprimDemo);
					summary.setQuarterName(v);
					//158428509 
					//summary.setOrderId((Long)quarterMap.get(v));
					Long year = 	(Long)quarterMap.get(v);
					summary.setOrderId(new Long(year+v.split("Q")[0])); 
					//158428509 
					digitalSummaryList.add(summary); 
					
				});
			}
			
			if(digitalQuarterSet!=null && digitalQuarterSet.size()>0){
				digitalQuarterSet.forEach(v->{
					Summary summary= blankSummaryObject(linearprimDemo);
					summary.setQuarterName(v);
					//158428509 
					//summary.setOrderId((Long)quarterMap.get(v));
					Long year = 	(Long)quarterMap.get(v);
					summary.setOrderId(new Long(year+v.split("Q")[0])); 
					//158428509 
					linearSummaryList.add(summary); 
					
				});
			}
			
			// Sorting based on order number
			digitalSummaryList.sort(Comparator.comparing(Summary::getOrderId));
			linearSummaryList.sort(Comparator.comparing(Summary::getOrderId));
			
			summaryMap.put("linear", linearSummaryList);
			summaryMap.put("digital", digitalSummaryList);
			
			// Grand Summary
			linearSummaryMap.put("linearprimDemo", linearprimDemo);
			linearSummaryMap.put("quarterMap", quarterMap);
			List<Summary> grandSummaryList= createGrandSummaryResponse(linearSummaryMap,digitalSummaryMap);
			
			summaryMap.put("total", grandSummaryList);
			
			dataMap.put("quaterlySummary", summaryMap);
			
			/*System.out.println("linearSummaryMap-->"+objectMapper.writeValueAsString(linearQuarterlySummaryMap));
			System.out.println("digitalSummaryMap-->"+objectMapper.writeValueAsString(digitalQuarterlySummaryMap));
			System.out.println("grand Summary-->"+objectMapper.writeValueAsString(summaryMap));*/
			dataMap.put("digitalDetails",Boolean.parseBoolean(getAdminDataByName("Suppress Children"))?removeChildLines(digitalQuarterlySummaryMap):digitalQuarterlySummaryMap.get("orders"));
			LinearProposalRO linearProposalRO = convergenceService.fetchLinearProposalDataByPlans(linearStr);
			dataMap.put("linearDetailsHtml",linearProposalRO.getLinearHtml());
			dataMap.put("termsAndConditions",getAdminDataByName("termsAndConditions"));
			dataMap.put("cDealId",dealId);
			dataMap.put("exportNo",exportNo);
			
		} catch (Exception e) {
			try {
				if(exportNo!=null){
					rollbackDealArchives(dealId,exportNo);
				}
			} catch (Exception e2) {
				throw new RuntimeException("Actual exception: "+e.getMessage()+"\n Rollback exception: "+e2.getMessage());
			}
			
			throw new RuntimeException(e.getMessage());
		}
		return dataMap;
	}
	
	
	private Map<K, V> createConvergenceArchiveDealData(Long dealId, String sso) throws JsonParseException, JsonMappingException, IOException {
		try {
			HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ObjectMapper objMapper=new ObjectMapper();

            StringBuilder res = new StringBuilder();
            res.append(restTemplate.postForObject(convergenceArchiveSaveURI+dealId+"?sso="+sso,String.class, String.class, headers));
            Map<K,V> archiveDataMap =  objMapper.readValue(res.toString(), Map.class);	
            
            return archiveDataMap;

        } catch (HttpClientErrorException e) {
        	//JsonObject json = (new JsonParser()).parse(e.getResponseBodyAsString()).getAsJsonObject();
            throw new DigitalClientException("Error occurred posting to Archive service: " + e.getMessage());
        }
        catch (HttpServerErrorException se)
        {
        	JsonObject json = (new JsonParser()).parse(se.getResponseBodyAsString()).getAsJsonObject();
            throw new RuntimeException("Error occurred while creating CDeal archive : " + json.getAsJsonPrimitive("response").getAsString());
        }
	
	}


	private void rollbackDealArchives(Long cDealId,Integer exportNo) throws Exception{
		ResponseEntity<String> response=null;
		Map<K,V> convergenceDealDataMap = null;
		try {
			HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        Map<String, String> uriParams = new HashMap<String, String>();
	        uriParams.put("cDealId", cDealId.toString());
	        uriParams.put("exportNo", exportNo.toString());
	        	        
	        response=restTemplate.exchange(cDealDeleteURI,HttpMethod.DELETE,null,String.class,uriParams);
	        convergenceDealDataMap =  new Gson().fromJson(response.getBody().toString(), Map.class);
	        if(convergenceDealDataMap!=null&&!Boolean.valueOf(convergenceDealDataMap.get("success").toString())){
	        	throw new RuntimeException("CDeal Archives deletion for cDealId :"+cDealId+" and exportNo : "+exportNo+" Failed");
	        }
	        restTemplate.exchange(LinearNDigitalDeleteURI,HttpMethod.DELETE,null,String.class,uriParams);
	        
		}catch (HttpServerErrorException e) {
            JsonObject json = (new JsonParser()).parse(e.getResponseBodyAsString()).getAsJsonObject();
            throw new RuntimeException("CDeal Archives deletion for cDealId :"+cDealId+" and exportNo : "+exportNo+" Failed" + json.getAsJsonPrimitive("response").getAsString());
        }
	}



	private List<Map<String, Object>> removeChildLines(Map<String, Map>  digitalQuarterlySummaryMap){
		List<Map<String,Object>> orders=(List<Map<String, Object>>) digitalQuarterlySummaryMap.get("orders");
		for (Map<String, Object> order : orders) {
			List<LinkedTreeMap> digiList = (List<LinkedTreeMap>) order.get("lineItems");
			order.put("lineItems", digiList.stream().filter(each->each.get("parentLineItemId")==null).collect(Collectors.toList()));
		}
		return orders;
	}
	
	
	public String getAdminDataByName(String name){
		List<Map<String,String>> responseList=null;
		Map termsMap=fetchAdminDataByName(name);
		if(termsMap!=null&&termsMap.size()>0)
			responseList=(List<Map<String,String>>)termsMap.get("response");
		return responseList!=null&&responseList.size()>0?responseList.get(0).get("value"):"";
	}
	
	public Map<K,V> fetchConvergenceDealData(Long dealId) {
		StringBuilder res = new StringBuilder();
		Map<K,V> convergenceDealDataMap = null;
		try {
			HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        Map<String, Long> uriParams = new HashMap<String, Long>();
	        uriParams.put("dealId", dealId);
	        res.append(restTemplate.getForObject(covergenceDealURI,String.class,uriParams));
	        convergenceDealDataMap =  new Gson().fromJson(res.toString(), Map.class);
	        if(convergenceDealDataMap==null){
	        	throw new RuntimeException("Convergence deal data is empty");
	        }
		}catch (HttpServerErrorException e) {
            JsonObject json = (new JsonParser()).parse(e.getResponseBodyAsString()).getAsJsonObject();
            throw new RuntimeException("Error occurred while fetching convergence deal data : " + json.getAsJsonPrimitive("response").getAsString());
        }
        return convergenceDealDataMap;		
	}
	
	public Map<K,V> fetchAdminDataByName(String name) {
		StringBuilder res = new StringBuilder();
		Map<K,V> convergenceDealDataMap = null;
		try {
			HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        Map<String, String> uriParams = new HashMap<String, String>();
	        uriParams.put("name", name);
	        res.append(restTemplate.getForObject(adminDataByNameURI,String.class,uriParams));
	        convergenceDealDataMap =  new Gson().fromJson(res.toString(), Map.class);
	        if(convergenceDealDataMap==null){
	        	throw new RuntimeException("Admin data is empty");
	        }
		}catch (HttpServerErrorException e) {
            JsonObject json = (new JsonParser()).parse(e.getResponseBodyAsString()).getAsJsonObject();
            throw new RuntimeException("Error occurred while fetching Admin data : " + json.getAsJsonPrimitive("response").getAsString());
        }
        return convergenceDealDataMap;		
	}
	
	
	public Map<String,Map> fetchLinearQuarterlySummaryData(String jsonStr) {
		StringBuilder res = new StringBuilder();
		Map<String,Map> linearDataMap = null;
		try {
			HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);
	        HttpEntity<String> requestEntity = new HttpEntity<String>(jsonStr, headers);
	        	        
	        res.append(restTemplate.postForObject(linearSummaryURI, requestEntity, String.class));
	        linearDataMap =  new Gson().fromJson(res.toString(), Map.class);
	        if(linearDataMap==null){
	        	throw new RuntimeException("Linear quarterly summary detail is blank");
	        }
		}catch (HttpServerErrorException e) {
            JsonObject json = (new JsonParser()).parse(e.getResponseBodyAsString()).getAsJsonObject();
            throw new RuntimeException("Error occurred while fetching linear quarterly summary details: " + json.getAsJsonPrimitive("exception").getAsString());
        }
		return linearDataMap;	
	}
	
	
	public Map<String, Map> fetchDigitalQuarterlySummaryData(final List<Long> digitalDeals,Long cDealId,Integer exportNo) {
		try {
			HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String baseUri=digitalQuarterlyURI+"?planIds[]="+StringUtils.join(digitalDeals, ',');
            String uri=cDealId!=null&&exportNo!=null?baseUri+"&cDealId="+cDealId+"&exportNo="+exportNo:baseUri;
            StringBuilder res = new StringBuilder();
            res.append(restTemplate.getForObject(uri,String.class));
            Map<String, Map> digitalDataMap =  new Gson().fromJson(res.toString(), Map.class);	
            if(digitalDataMap==null){
	        	throw new RuntimeException("Digital quarterly summary detail is blank");
	        }
            return digitalDataMap;

        } catch (HttpClientErrorException e) {
        	//JsonObject json = (new JsonParser()).parse(e.getResponseBodyAsString()).getAsJsonObject();
            throw new DigitalClientException("Error occurred posting to Digital service: " + e.getMessage());
        }
        catch (HttpServerErrorException se)
        {
        	JsonObject json = (new JsonParser()).parse(se.getResponseBodyAsString()).getAsJsonObject();
            throw new RuntimeException("Error occurred while fetching digital quarterly summary details: " + json.getAsJsonPrimitive("exception").getAsString());
        }
		catch (Exception e) {
			//JsonObject json = (new JsonParser()).parse(e.getMessage()).getAsJsonObject();
            throw new RuntimeException("Error occurred while fetching digital quarterly summary details: " + e.getMessage());
		}
	}
	
	
	private Map<String, String> getDigitalQuarters(String quarter,Map<String, Long>  quarterMap) {
		
		String quarterSpilt = quarter.substring(quarter.indexOf(":")+1);
		String[] quaterArr= quarterSpilt.split("/");
		StringBuffer sb=new StringBuffer();
		sb.append(new StringBuilder(quaterArr[0]).reverse().toString());
		sb.append(quaterArr[1].substring(Math.max(quaterArr[1].length() - 2, 0)));
		
		
		Map<String, String> digiQuarterMap= new HashMap<String, String>();
		// 158428509 
		//digiQuarterMap.put("orderNo",((Long)quarterMap.get(sb.toString())).toString());
		StringBuffer sb1=new StringBuffer();
		sb1.append(new StringBuilder(quaterArr[1]).toString());
		sb1.append(quaterArr[0].substring(Math.max(quaterArr[0].length() - 1, 0)));
		digiQuarterMap.put("orderNo",sb1.toString());
		// 158428509 
				
		digiQuarterMap.put("name",sb.toString());
		quarterMap.put(sb.toString(), new Long(quaterArr[1]));
		
		return digiQuarterMap;		
	}
	
	
	private Summary formSummaryObject(Input input) {
		
		Summary summary =new Summary();
		summary.setOrderId(input.getQuarterId());
		summary.setQuarterName(input.getQuarterName());							
		summary.setGrossDollars(formatdollars(round(input.getGrossTotalDollars())));
		summary.setNetDollars(formatdollars(round(input.getNetTotalDollars())));
		
		TargetGroup priDemo =new TargetGroup();
		priDemo.addGrossCpm(input.getGrossTotalDollars(),input.getTotalImpressions()); 
		priDemo.addNetCpm(input.getNetTotalDollars(),input.getTotalImpressions()); 
		priDemo.setImpressions(formatImpressions(round(input.getTotalImpressions()/1000))); 
		priDemo.setName(input.getPriDemo());	
		summary.setPriDemo(priDemo);
		
		return summary;		
	}
	
	private Summary formTotalSummaryObject(Input input) {
		
		Summary summary =new Summary();
		summary.setQuarterName("Total");
		summary.setOrderId(999999L);
		summary.setGrossDollars(formatdollars(Long.valueOf(round(input.getGrossDollarList().values().stream().mapToDouble(Double::doubleValue).sum()))));
		summary.setNetDollars(formatdollars(Long.valueOf(round(input.getNetDollarList().values().stream().mapToDouble(Double::doubleValue).sum()))));
		
		if(input.getEquiList()!=null){
			double equivValue= new Double(input.getEquiList().stream().mapToDouble(Double::doubleValue).sum());
			summary.setEquivUnits(formatEquivUnits(String.valueOf(equivValue)));
		}
		
		TargetGroup priDemo =new TargetGroup();
		priDemo.addGrossCpm(new Double(input.getGrossDollarList().values().stream().mapToDouble(Double::doubleValue).sum()),new Double(input.getTotalImpList().values().stream().mapToDouble(Double::doubleValue).sum())); 
		priDemo.addNetCpm(new Double(input.getNetDollarList().values().stream().mapToDouble(Double::doubleValue).sum()),new Double(input.getTotalImpList().values().stream().mapToDouble(Double::doubleValue).sum()));
		priDemo.setImpressions(formatImpressions(Long.valueOf(round(input.getTotalImpList().values().stream().mapToDouble(Double::doubleValue).sum()/1000)))); 
		priDemo.setName(input.getPriDemo());
		summary.setPriDemo(priDemo);
		
		return summary;		
	}
	
	private Summary blankSummaryObject(String demoName) {
		
		Summary summary =new Summary();
		summary.setGrossDollars("$0");
		summary.setNetDollars("$0");
		summary.setEquivUnits("0.0");
		TargetGroup priDemo =new TargetGroup();
		priDemo.setGrossCpm("$00.00"); 
		priDemo.setNetCpm("$00.00");
		priDemo.setImpressions("0"); 
		priDemo.setName(demoName);
		summary.setPriDemo(priDemo);
		
		return summary;		
	}
	
	
	private String formatdollars(Long value) {
		DecimalFormat formatter = new DecimalFormat("#,###");
		return "$"+formatter.format(value);
	}
	
	private String formatImpressions(Long value) {
		DecimalFormat formatter = new DecimalFormat("#,###");
		return formatter.format(value);
	}
	private String formatEquivUnits(String value) {
		DecimalFormat formatter = new DecimalFormat("####.0");
		return formatter.format(Double.parseDouble(value));
	}
	
	private Long round(Double value) {
		Long valueRes =0L;
		/*if (value.getClass() == Integer.class) {
			valueRes= Long.valueOf(Math.round((Integer)value));
		} 
		else if (value.getClass() == String.class) {
			valueRes= Long.valueOf(Math.round(Double.parseDouble((String)value)));
		}else if (value.getClass() == Double.class) {*/
			valueRes= Long.valueOf(Math.round(value));
		/*}
		else if (value.getClass() == Long.class) {
			valueRes= (Long)value;
		}*/
		return valueRes;
	}
	
	
	private Double toDouble(Object value) {
		Double valueRes =null;
		if (value.getClass() == Integer.class) {
			valueRes= new Double((Integer)value);
		} 
		else if (value.getClass() == String.class) {
			valueRes= new Double(Double.parseDouble((String)value));
		}else if (value.getClass() == Double.class) {
			valueRes= (Double) value;
		}
		else if (value.getClass() == Long.class) {
			valueRes=  new Double((Long)value);
		}
		return valueRes;
	}
	
	private Map<String,Object> createDigitalSummaryObj(DetailsObj detailsObj){
		Map<String,Object> digitalSummaryMap=new HashMap<String,Object>();
		try{
			List<Summary> digitalSummaryList =new ArrayList<Summary>();
			List<String> digitalQuarters=new ArrayList<String>();
			Map<String,Double> digiGrossDollarMap =new HashMap<String,Double>();
			Map<String,Double> digiNetDollarMap =new HashMap<String,Double>();
			Map<String,Double> digiTotalImpMap =new HashMap<String,Double>();
			
			
			Map<String, Map> digitalMap= detailsObj.getDigitalMap();
			Map<String, Long> quarterMap = detailsObj.getQuarterMap();
			String linearprimDemo=detailsObj.getDemoName();
			
			if(digitalMap.get("categorySummaries")!=null && digitalMap.get("categorySummaries").get("quarterSummary")!=null){
				List quarterSummaryList= (List) digitalMap.get("categorySummaries").get("quarterSummary");
				if(quarterSummaryList!=null){
					Map<K,V> quarters=(Map<K, V>) quarterSummaryList.get(0);
					if(quarters!=null){
						quarters.forEach((k,v)->{
							Map<String, String> digiMap = getDigitalQuarters(k.toString(),quarterMap);
							digitalQuarters.add(digiMap.get("name"));
							Map<K,V> value=(Map<K, V>) v;
							
							Input input=new Input();
							
							Double grossTotalDollars=toDouble(value.get("netTotalDollars"))/.85;
							Double netTotalDollars= toDouble(value.get("netTotalDollars"));
							Double totalImpressions=toDouble(value.get("totalImpressions"));	
							
							input.setGrossTotalDollars(grossTotalDollars);
							input.setNetTotalDollars(netTotalDollars);
							input.setTotalImpressions(totalImpressions);
							input.setPriDemo(linearprimDemo);
							input.setQuarterId(Long.parseLong((String)(digiMap.get("orderNo"))));
							input.setQuarterName(digiMap.get("name"));
							
							digiGrossDollarMap.put(input.getQuarterName(),grossTotalDollars);
							digiNetDollarMap.put(input.getQuarterName(),netTotalDollars);
							digiTotalImpMap.put(input.getQuarterName(),totalImpressions);
							
							Summary summary = formSummaryObject(input);
							digitalSummaryList.add(summary);
							
							
						});
						
						Input input=new Input();
						input.setGrossDollarList(digiGrossDollarMap);
						input.setNetDollarList(digiNetDollarMap);
						input.setTotalImpList(digiTotalImpMap);
						input.setPriDemo(linearprimDemo);
						digitalQuarters.add("Total");
						Summary summary = formTotalSummaryObject(input);
						digitalSummaryList.add(summary);
					}
					
					digitalSummaryMap.put("digitalSummaryList", digitalSummaryList);
					digitalSummaryMap.put("digiGrossDollarMap", digiGrossDollarMap);
					digitalSummaryMap.put("digiNetDollarMap", digiNetDollarMap);
					digitalSummaryMap.put("digiTotalImpMap", digiTotalImpMap);
					digitalSummaryMap.put("digitalQuarters", digitalQuarters);
				}
			}
		}catch(Exception e){
			throw new RuntimeException("Error occured while creating digital summary object");
		}
		return digitalSummaryMap;
	}
	
	private Summary formGuarCPMSummaryObject(Input input, Double guidelinesGuarCPM, boolean flag) {

		Summary summary = new Summary();
		summary.setOrderId(input.getQuarterId());
		summary.setQuarterName(input.getQuarterName());
		summary.setGrossDollars(formatdollars(round(input.getGrossTotalDollars())));
		summary.setNetDollars(formatdollars(round(input.getNetTotalDollars())));
		
		DecimalFormat formatter = new DecimalFormat("#00.00");
		
		TargetGroup priDemo = new TargetGroup();
		priDemo.setGrossCpm("$"+(guidelinesGuarCPM!=0 ?formatter.format(guidelinesGuarCPM):"00.00"));
		priDemo.setNetCpm("$"+(guidelinesGuarCPM!=0 ?formatter.format(guidelinesGuarCPM*0.85):"00.00"));
		priDemo.setImpressions(formatImpressions(round(input.getTotalImpressions())));
		priDemo.setName(input.getPriDemo());
		summary.setPriDemo(priDemo);
		return summary;
	}
	
	private Map<String, Object> createLinearSummaryObj(DetailsObj detailsObj){
		 Map<String, Object> linearSummaryMap=new HashMap<String, Object>();
		try{
		
			Map<String, Map>  linearMap= detailsObj.getLinearMap();
			Map<String, Long> quarterMap = detailsObj.getQuarterMap();
			String linearprimDemo=detailsObj.getDemoName();
			
			List<String> linearQuarters=new ArrayList<String>();
			List<Summary> linearSummaryList =new ArrayList();
			
			Map<String, HashMap<String, String>> linearDataMap=(Map<String, HashMap<String, String>>) linearMap.get("dataMap");
			List linearSummary=(List) linearDataMap.get("summary");
			
			Map<String,Double> linearGrossDollarMap =new HashMap<String,Double>();
			Map<String,Double> linearNetDollarMap =new HashMap<String,Double>();
			Map<String,Double> linearTotalImpMap =new HashMap<String,Double>();
			
			List<Double> linearEquiList =new ArrayList();
			
			Boolean guarCpmFlag = detailsObj.isGuarCpmFlag();
			
			if(linearSummary!=null){
				linearSummary.forEach(quarters ->{
					if(quarters!=null){
							Map<K,V> value=(Map<K, V>) quarters;
							linearQuarters.add((String)value.get("gs1"));
							
							Input input=new Input();							
							Double grossTotalDollars=value.get("totalDollars")!=null?toDouble(value.get("totalDollars")):0.0;
							Double netTotalDollars= value.get("netDollars")!=null?toDouble(value.get("netDollars")):0.0;
							
							Double totalImpressions;
							Double guidelinesGuarCPM = 00.00;
							
							//158365199
							if (guarCpmFlag) {
								guidelinesGuarCPM = value.get("guidelinesGuarCPM") != null? toDouble(value.get("guidelinesGuarCPM")): 00.00;
								totalImpressions = guidelinesGuarCPM>0?(grossTotalDollars / guidelinesGuarCPM):0;
								totalImpressions = totalImpressions != null ? toDouble(totalImpressions) : 0.0;
							} else {
								totalImpressions=value.get("totalGuarImps")!=null?toDouble(value.get("totalGuarImps")):0.0;
							}//158365199
							
							input.setTotalImpressions(totalImpressions);
							
							Double totalEqvUnits=value.get("totalEqvUnits")!=null?Double.parseDouble((String)value.get("totalEqvUnits")):0.0;	
							
							input.setGrossTotalDollars(grossTotalDollars);
							input.setNetTotalDollars(netTotalDollars);
							input.setTotalImpressions(totalImpressions);
							input.setPriDemo(linearprimDemo);
							//158428509 
							//input.setQuarterId((Long)quarterMap.get((String)value.get("gs1"))); 
							Long year = 	(Long)quarterMap.get((String)value.get("gs1")) ; //==2014
							String str1 = (String)value.get("gs1");
							input.setQuarterId(new Long(year+str1.split("Q")[0])); 
							// 158428509 
							
							input.setQuarterName((String)value.get("gs1"));
							
							//158365199
							Summary summary = null;
							if (guarCpmFlag) {
								summary = formGuarCPMSummaryObject(input, guidelinesGuarCPM, guarCpmFlag);
							}else {
								summary = formSummaryObject(input);	
							}
							//158365199
							summary.setEquivUnits(formatEquivUnits((String)value.get("totalEqvUnits")));
							linearSummaryList.add(summary);
							
							linearGrossDollarMap.put(input.getQuarterName(),grossTotalDollars);
							linearNetDollarMap.put(input.getQuarterName(),netTotalDollars);
							//158365199
							if (guarCpmFlag) {
								linearTotalImpMap.put(input.getQuarterName(),totalImpressions*1000);
							}else {
								linearTotalImpMap.put(input.getQuarterName(),totalImpressions);
							}
							//158365199
							linearEquiList.add(totalEqvUnits);
					}
				});
				
				Input input=new Input();
				input.setGrossDollarList(linearGrossDollarMap);
				input.setNetDollarList(linearNetDollarMap);
				input.setTotalImpList(linearTotalImpMap);
				input.setPriDemo(linearprimDemo);
				input.setEquiList(linearEquiList);
				linearQuarters.add("Total");
				Summary summary = formTotalSummaryObject(input);
				
				linearSummaryList.add(summary);
				
				linearSummaryMap.put("linearSummaryList", linearSummaryList);
				linearSummaryMap.put("linearGrossDollarMap", linearGrossDollarMap);
				linearSummaryMap.put("linearNetDollarMap", linearNetDollarMap);
				linearSummaryMap.put("linearTotalImpMap", linearTotalImpMap);
				linearSummaryMap.put("linearQuarters", linearQuarters);
				
			}
		}catch(Exception e){
			throw new RuntimeException("Error occured while creating linear summary object");
		}
		return linearSummaryMap;
	}
	
	private List<Summary> createGrandSummaryResponse(Map<String,Object> linearSummaryMap,Map<String,Object> digitalSummaryMap){
		List<Summary> grandSummaryList =new ArrayList<Summary>();
		try{
			
			List<String> linearQuarters=(List<String>)linearSummaryMap.get("linearQuarters");
			List<String> digitalQuarters=(List<String>)digitalSummaryMap.get("digitalQuarters");
			
			
			Map<String,Double> linearGrossDollarMap =(Map<String,Double>)linearSummaryMap.get("linearGrossDollarMap");
			Map<String,Double> linearNetDollarMap = (Map<String,Double>)linearSummaryMap.get("linearNetDollarMap");
			Map<String,Double> linearTotalImpMap =(Map<String,Double>)linearSummaryMap.get("linearTotalImpMap");
			
			Map<String,Double> digiGrossDollarMap =(Map<String,Double>)digitalSummaryMap.get("digiGrossDollarMap");
			Map<String,Double> digiNetDollarMap = (Map<String,Double>)digitalSummaryMap.get("digiNetDollarMap");
			Map<String,Double> digiTotalImpMap =(Map<String,Double>)digitalSummaryMap.get("digiTotalImpMap");
			
			Map<String, Long> quarterMap = (Map<String, Long>)linearSummaryMap.get("quarterMap");
			String linearprimDemo=(String)linearSummaryMap.get("linearprimDemo");
						
			List<String> combinedQuarters = Stream.concat(linearQuarters.stream(), digitalQuarters.stream()).distinct().collect(Collectors.toList());
			combinedQuarters.remove("Total");
			
			Map<String,Double> grandGrossDollarMap =new HashMap<String,Double>();
			Map<String,Double> grandNetDollarMap =new HashMap<String,Double>();
			Map<String,Double> grandTotalImpMap =new HashMap<String,Double>();
			
			combinedQuarters.forEach(a->{
				Input input=new Input();		
				Double lineargross= linearGrossDollarMap.get(a)!=null?linearGrossDollarMap.get(a):0.0;
				Double digitalgross= digiGrossDollarMap.get(a)!=null?digiGrossDollarMap.get(a):0.0;
				
				Double linearnet= linearNetDollarMap.get(a)!=null?linearNetDollarMap.get(a):0.0;
				Double digitalnet= digiNetDollarMap.get(a)!=null?digiNetDollarMap.get(a):0.0;
				
				Double linearimp= linearTotalImpMap.get(a)!=null?linearTotalImpMap.get(a):0.0;
				Double digitalimp= digiTotalImpMap.get(a)!=null?digiTotalImpMap.get(a):0.0;
				
				Double grossTotalDollars=lineargross+digitalgross;
				Double netTotalDollars= linearnet+digitalnet;
				Double totalImpressions=linearimp+digitalimp;	
				
				input.setGrossTotalDollars(grossTotalDollars);
				input.setNetTotalDollars(netTotalDollars);
				input.setTotalImpressions(totalImpressions);
				input.setPriDemo(linearprimDemo);
				//158428509 
				//input.setQuarterId((Long)quarterMap.get(a));
				Long year = 	(Long)quarterMap.get(a) ; //==2014
				input.setQuarterId(new Long(year+a.split("Q")[0])); 
				// 158428509 
				
				input.setQuarterName(a);
				
				
				Summary summary = formSummaryObject(input);
				grandSummaryList.add(summary);
				
				grandGrossDollarMap.put(input.getQuarterName(),grossTotalDollars);
				grandNetDollarMap.put(input.getQuarterName(),netTotalDollars);
				grandTotalImpMap.put(input.getQuarterName(),totalImpressions);
			});
			
			
			Input input=new Input();
			input.setGrossDollarList(grandGrossDollarMap);
			input.setNetDollarList(grandNetDollarMap);
			input.setTotalImpList(grandTotalImpMap);
			input.setPriDemo(linearprimDemo);
			
			Summary summary = formTotalSummaryObject(input);
			
			grandSummaryList.add(summary);
			
			grandSummaryList.sort(Comparator.comparing(Summary::getOrderId));
		}catch(Exception e){
			throw new RuntimeException("Error occured while creating Grand summary object");
		}
		
		return grandSummaryList;
	}
}