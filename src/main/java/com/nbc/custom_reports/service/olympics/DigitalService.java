package com.nbc.custom_reports.service.olympics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.tomcat.util.Diagnostics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;
import com.nbc.custom_reports.util.olympics.DigitalClientException;

@Service
@RefreshScope
public class DigitalService<K, V> {
	@Value("${digitalURI}")
	private String digitalURI;
	private static final Logger logger = Logger.getLogger(DigitalService.class);
	
	@Autowired
    SpringTemplateEngine springTemplateEngine;
	
	@Autowired
	private RestTemplate restTemplate;

	
	public HashMap<String, String> fetchDigitalHtml(Map digitalDataMap, Collection<? extends String> secDemos,boolean hhExists, Map<String, Object> conditionMap) {
        HashMap<String, String> digitalResponseMap = new HashMap<String, String>();          
            if (digitalDataMap != null && digitalDataMap.size()>0) {
                digitalResponseMap = new HashMap<String, String>();
                digitalResponseMap.put("video",  buildDigitalHtml(digitalDataMap, "video",secDemos,hhExists,conditionMap));
                digitalResponseMap.put("nonvideo",  buildDigitalHtml(digitalDataMap, "nonvideo",secDemos,hhExists,conditionMap));
            }
        return digitalResponseMap;
    }
	
	private String buildDigitalHtml(Map digitalDataMap, String category, Collection<? extends String> secDemos,boolean hhExists, Map<String, Object> conditionMap) {
		String templateName;
		if(category == "video")
			templateName = "DigitalVideoSummary";
		else
			templateName = "DigitalNonVideoSummary";
		Context context = new Context();
		context.setVariable(category, ((LinkedTreeMap)digitalDataMap.get("categorySummaries")).get(category));
		context.setVariable("secDemos",secDemos);
		context.setVariable("hhExists","N");
		if(hhExists){
		context.setVariable("hhExists","Y");
		}
		context.setVariable("isAdPrdGrpRollup", conditionMap.get("isAdPrdGrpRollup"));
		String returnValue = springTemplateEngine.process(templateName, context);
		return returnValue;
	}
	
	
	public Map<String, Map> fetchDigitalData(final Long userId, final String sessionId, final HashMap<String, HashMap<String, String>> digitalDeals) {
		try {
			HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
            params.add("userId", userId.toString());
            params.add("sessionId", sessionId);
            params.add("digitalDemo", "P2+");
            
            Map deals = digitalDeals.get("deals");
            if(!CollectionUtils.isEmpty(deals)) {
            	 deals.keySet().forEach(d -> params.add("planIds[]", d));
            }
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<MultiValueMap<String, Object>>(params, headers);


            StringBuilder res = new StringBuilder();
            res.append(restTemplate.postForObject(digitalURI, requestEntity, String.class));
            Map<String, Map> digitalDataMap =  new Gson().fromJson(res.toString(), Map.class);	
            ArrayList<LinkedTreeMap> orders = (ArrayList) digitalDataMap.get("orders");
	    		for(LinkedTreeMap order : orders) {
	    			String orderId=String.valueOf(Math.round(Double.valueOf(order.get("orderId").toString())));
	    			HashMap<String, String> digitalDeal = (HashMap<String, String>)deals.get(orderId);
	    			order.putAll(digitalDeal);
	    		}
	    		HashMap header = new HashMap<String,String>();
	    		header.put("digital_ae", digitalDeals.get("digital_ae"));
	    		header.put("digital_planner", digitalDeals.get("digital_planner"));
	    		digitalDataMap.put("header", header);
            return digitalDataMap;

        } catch (HttpClientErrorException e) {
        		logger.error("Error occurred posting to this service: " + e.getResponseBodyAsString(), e);
            
            JsonObject json = (new JsonParser()).parse(e.getResponseBodyAsString()).getAsJsonObject();
            throw new DigitalClientException("Error occurred posting to Digital service: " + json.getAsJsonPrimitive("message").getAsString());
        }
        catch (HttpServerErrorException se)
        {
        		throw new DigitalClientException(se.getMessage());
        }
	}
	
	public HashMap<String, Object> getDigitalParams(String jsonStr) {
		HashMap<String, Object> digitalParams = new HashMap<String, Object>();
		JsonObject json = (new JsonParser()).parse(jsonStr).getAsJsonObject();
		JsonObject header = json.getAsJsonObject().getAsJsonObject("deal_header");
		digitalParams.put("digital_ae", header.getAsJsonPrimitive("dig_ae_app_user").getAsString());
		digitalParams.put("digital_planner", header.getAsJsonPrimitive("dig_pln_app_user").getAsString());
		JsonArray deals = json.getAsJsonObject().getAsJsonArray("deal_components");
		HashMap<String, HashMap<String,String>> dealsMap = new HashMap<String, HashMap<String,String>>();
		for (int i = 0; i < deals.size(); i++) {
			JsonObject deal = deals.get(i).getAsJsonObject();
			if (deal.get("is_digital").getAsBoolean()) {
				HashMap<String, String> dealMap = new HashMap<String, String>();
				String deal_id = deal.getAsJsonPrimitive("deal_id").getAsString();
				dealMap.put("demographic", deal.getAsJsonPrimitive("demographic").getAsString());
				dealsMap.put (deal_id, dealMap);
			}
		}
		digitalParams.put("deals", dealsMap);
		return digitalParams;
	}
	
	public String buildDigitalOrderHtml(Map<String, Map<K, V>> digitalDataMap,String template,boolean suppressChildren) {
		StringBuilder orderHtmls = new StringBuilder();
		ArrayList<LinkedTreeMap> orders = (ArrayList) digitalDataMap.get("orders");
		HashMap header = (HashMap)digitalDataMap.get("header");
		for(LinkedTreeMap order : orders) {
			String html = buildDigitalDetailHtml(order, header,template,suppressChildren);
			orderHtmls.append(html);
		}
		return orderHtmls.toString();
	}
	
	public String buildDigitalHeaderHtml(Map<String, Object> orderMap, HashMap header,String template) {
		//String templateName = "DigitalDetailsHeader";
		Context context = new Context();
		context.setVariable("order", orderMap);
		context.setVariable("header", header);
		String returnValue = springTemplateEngine.process(template, context);
		return returnValue;
	}
	
	private String replaceSplCharacter(String data) {
    	String escData=data;
    	escData=escData.replaceAll("&", "&amp;");
    	escData=escData.replaceAll("â€˜", "&lsquo;");
    	escData=escData.replaceAll("â€™", "&rsquo;");
    	escData=escData.replaceAll("â€œ", "&ldquo;");
    	escData=escData.replaceAll("â€�", "&rdquo;");
    	escData=escData.replaceAll("'", "&#39;");
    	escData=escData.replaceAll("\"", "&#34;");
    	escData=escData.replaceAll(",", "&#44;");

     return escData;

	}
	
	private String buildDigitalDetailHtml(Map<String, Object> digitalMap, HashMap header,String template,boolean hideChildren)  {

        try {

            StringBuilder strBuild = new StringBuilder();
            List<LinkedTreeMap> digiList = (List<LinkedTreeMap>) digitalMap.get("lineItems");
            digiList=hideChildren?digiList.stream().filter(each->each.get("parentLineItemId")==null).collect(Collectors.toList()):digiList;
            strBuild.append(buildDigitalHeaderHtml(digitalMap, header,template));
            
            if (digiList == null) {
            	
                return strBuild.toString();
            }

            String tdStyle = "class='headercell ' scope='col' style='vertical-align:bottom; border-bottom-width: 0px;'";
            String noneDiv = "<div style='display:table-cell; text-align: left;'><div>&nbsp;</div></div>";

            strBuild.append("<table class='pdfptable display bottom nbc-first-in-group' id='1b' style='height: auto; width: auto;table-layout: fixed;'>")
                    .append("<thead><tr class='header indent1' id='header1' >")
                    .append("<td class='headercell ' scope='col' style='vertical-align:bottom; border-bottom-width: 0px;width:220px'")/*.append(tdStyle)*/.append(" >Placement</td>")
                    .append("<td ").append(tdStyle).append(" >Start Date</td>")
                    .append("<td ").append(tdStyle).append(" >End Date</td>")
                    .append("<td ").append(tdStyle).append(" >Ad Size</td>")
                    .append("<td ").append(tdStyle).append(" >Cost Method</td>")
                    .append("<td ").append(tdStyle).append(" >Impressions</td>")
                    .append("<td ").append(tdStyle).append(" >Gross CPM</td>")
                    .append("<td ").append(tdStyle).append(" >Net CPM</td>")
                    .append("<td ").append(tdStyle).append(" >Gross Total</td>")
                    .append("<td ").append(tdStyle).append(" >Net Total</td>") 
                    .append("<td ").append(tdStyle).append(" >Notes</td>")
                    .append("<td class='headercell' style='border-bottom-width: 0px;'>&nbsp;</td></tr></thead>");
            

            DecimalFormat formatter = new DecimalFormat("#,##0.00");
            DecimalFormat wholeformatter = new DecimalFormat("#,##0");
            strBuild.append("<tbody>");
            for (LinkedTreeMap digiMap : digiList) {
                Set<String> digiKeySet = digiMap.keySet();
                String totalNet = !StringUtils.isEmpty(digiMap.get("netCost"))?""+digiMap.get("netCost"):null;
                String totalGross = !StringUtils.isEmpty(digiMap.get("grossTotal"))?""+digiMap.get("grossTotal"):null;
                String parentLineItemId=digiMap.get("parentLineItemId")!=null?((Double)digiMap.get("parentLineItemId")).toString(): null;
                
                if(parentLineItemId!=null){
	                	digiMap.put("grossCpm",null);
	                	digiMap.put("netCpm",null);
	                	digiMap.put("impressions",null);
                }
                
                Double totalImpsInt = digiMap.get("impressions") !=null ?(Double) digiMap.get("impressions"):null;                
                String grossCpm = !StringUtils.isEmpty(digiMap.get("grossCpm"))?""+ digiMap.get("grossCpm"):null;
                String netCpm = !StringUtils.isEmpty(digiMap.get("netCpm"))?""+ digiMap.get("netCpm"):null;
                
                

                totalNet = !StringUtils.isEmpty(totalNet) ? formatter.format(Double.parseDouble(totalNet)) : "0.00";
                totalGross = !StringUtils.isEmpty(totalGross) ? formatter.format(Double.parseDouble(totalGross)) : "0.00";
                String totalImps = totalImpsInt!=null?wholeformatter.format(Double.parseDouble(totalImpsInt.toString())):null;

                grossCpm = !StringUtils.isEmpty(grossCpm) ? formatter.format(Double.parseDouble(grossCpm)) : "0.00";
                netCpm = !StringUtils.isEmpty(netCpm) ? formatter.format(Double.parseDouble(netCpm)) : "0.00";
                
                int i = 0;
                StringBuilder placementName = digiMap.get("placementName") != null ? new StringBuilder((String) digiMap.get("placementName")) : null;
                StringBuilder notes = digiMap.get("notes") != null ? new StringBuilder((String) digiMap.get("notes")) : null;

                strBuild.append("<tr indent='1_' >")
                        .append("<td style='style=width:220px;vertical-align:middle;text-align: left;'> ");
                        
                
                if(parentLineItemId!=null){
                	strBuild.append(" <span>");
                	strBuild.append("<div style='float: left;width:20px;'>&nbsp;</div>");
                	strBuild.append("<div style='float: right;width:200px;vertical-align:middle;text-align: left;'>");
                }
               
                
                
                int splitCount=40;
	            	if(parentLineItemId!=null){
	            		splitCount=35;
	            	}
                if (placementName != null) {
                    while ((i = placementName.indexOf(" ", i + splitCount)) != -1) {
                        placementName.replace(i, i + 1, "<br>");
                    }

                    strBuild.append(replaceSplCharacter(placementName.toString()));
                } else {
                    strBuild.append(noneDiv);
                }
                if(parentLineItemId!=null){
                	strBuild.append("</div></span>");
                }
               
                strBuild.append("</td>")
                        .append("<td style='text-align: left; vertical-align:middle;'> ")
                        .append((digiMap.get("startDate") == null ? noneDiv : digiMap.get("startDate")))
                        .append("</td>")
                        .append("<td style='text-align: left; vertical-align:middle;'> ")
                        .append((digiMap.get("endDate") == null ? noneDiv : digiMap.get("endDate")))
                        .append("</td>")
                        .append("<td style='text-align: left; vertical-align:middle;'> ")
                        .append((digiMap.get("adSize") == null ? noneDiv : digiMap.get("adSize")))
                        .append("</td>")
                        .append("<td style='text-align: left; vertical-align:middle;'> ")
                        .append((digiMap.get("costMethod") == null ? noneDiv : digiMap.get("costMethod")))
                        .append("</td>")
                        .append("<td style='text-align: center; vertical-align:middle;'> ")
                        .append((!StringUtils.isEmpty(totalImps) ? totalImps : noneDiv))
                        .append("</td>")
                        .append("<td style='text-align: center; vertical-align:middle;'>")
                        .append((digiMap.get("grossCpm") == null ? noneDiv : grossCpm))
                        .append("</td>")
                        .append("<td style='text-align: center; vertical-align:middle;'> ")
                        .append((digiMap.get("netCpm") == null ? noneDiv : netCpm))
                        .append("</td>")
                        .append("<td style='text-align: center; vertical-align:middle;'> ")
                        .append((!StringUtils.isEmpty(totalGross) ? totalGross : noneDiv))
                        .append("</td>")
                        .append("<td style='text-align: center; vertical-align:middle;'> ")
                        .append((!StringUtils.isEmpty(totalNet) ? totalNet : noneDiv))
                        .append("</td>")
                        .append("<td style='white-space: nowrap;vertical-align:middle;text-align: left'> ");

                if (notes != null) {
                	
                    while ((i = notes.indexOf(" ", i + 40)) != -1) {
                        notes.replace(i, i + 1, "<br>");
                    }
                    strBuild.append(replaceSplCharacter(notes.toString()));
                } else {
                    strBuild.append(noneDiv);
                }
                strBuild.append("</td>")
                        .append("<td></td></tr>");
            }
            strBuild.append("</tbody></table>");


            return strBuild.toString();

        } catch (Exception e) {
        		logger.info(e);
        		e.printStackTrace();
            throw new RuntimeException("Error while building digital html response");
        }
    }
	
	
}
