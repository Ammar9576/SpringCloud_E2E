package com.nbc.custom_reports.controller.olympics;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.nbc.custom_reports.domain.PayloadErrorLogger;
import com.nbc.custom_reports.service.olympics.ConvergenceReportService;

import io.swagger.annotations.Api;
import springfox.documentation.annotations.ApiIgnore;

@RestController

@RequestMapping("/olympics")
@RefreshScope
@Api(description = "Convergence API's", tags = {"Olympics"})
@ApiIgnore
public class OlympicsController {

	private static final Logger logger = Logger.getLogger(OlympicsController.class);

	@Autowired
	ConvergenceReportService convergenceService;	
	
	@Value("${pdfRenderrerURI}")
	private String pdfRenderrerURI;

	@ResponseBody
	@RequestMapping(value = "/getOlympicsReport", method = RequestMethod.POST)
	public void getOlympicsReport(@RequestBody String jsonStr, HttpServletResponse response) {
		PayloadErrorLogger logger = null;
		Map<String,Object> pdfInputParamMap = null;
		String errorMsg = null;
		String sessionId = null;
		Long userId=0L;
		JsonPrimitive tadUser = null;
		try {
			sessionId = UUID.randomUUID().toString();
			// For passing the parameters to pdf renderer
			JsonObject json = (new JsonParser()).parse(jsonStr).getAsJsonObject();
			JsonObject header = json.getAsJsonObject().getAsJsonObject("deal_header");
			JsonArray deals = json.getAsJsonObject().getAsJsonArray("deal_components");
			JsonObject params = json.getAsJsonObject().getAsJsonObject("report_params");
	        tadUser = json.getAsJsonObject().getAsJsonPrimitive("requested_by");
			 if (header == null || deals == null || params == null) {
		            throw new IllegalArgumentException("Missing one of the required parameters: deal_header, deal_components, report_params");
		     }	
			userId = convergenceService.lookupTADUser();
			pdfInputParamMap = convergenceService.getOlympicsReport(jsonStr,sessionId,userId,params);			
			String html = (String)pdfInputParamMap.get("html");
			String linear_deal = (String)pdfInputParamMap.get("linearDetail");
			callPDFRenderService(response,header,deals,params, html, linear_deal);
		} catch (Exception e) {
			errorMsg = e.getMessage();
			throw new RuntimeException(e.getMessage());
		}finally {
			try {
			logger = new PayloadErrorLogger();
			logger.setId(sessionId);
			logger.setTadPayload(jsonStr);
			logger.setProcInput(pdfInputParamMap!=null?pdfInputParamMap.get("procParams").toString():null);
			logger.setErrorLog(errorMsg);
			logger.setRequestedBy(tadUser!=null?tadUser.toString():null);
			logger.setCreatedDate(new Date());
			convergenceService.logPayloadAndError(logger);
			}catch(Exception e){
				throw new RuntimeException("Error while logging input--"+e.getMessage());
			}
		}
	}
	
	private void callPDFRenderService(HttpServletResponse response, JsonObject header, JsonArray deals, JsonObject params, String htmlAndJS, String linear_deal) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        StringBuilder res = new StringBuilder();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> requestVariables = new LinkedMultiValueMap<String, String>();

        requestVariables.add("table", htmlAndJS);
        requestVariables.add("deal_header", header.toString());
        requestVariables.add("deal_components", deals.toString());
        requestVariables.add("report_params", params.toString());
        requestVariables.add("linear_deal", linear_deal);
        requestVariables.add("olympicFlag", "Y");

        response.setContentType("application/pdf");

        try {
            ResponseEntity<byte[]> pdf = restTemplate.exchange(pdfRenderrerURI, HttpMethod.POST, new HttpEntity<Object>(requestVariables, null), byte[].class);
            IOUtils.write(pdf.getBody(), response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ie) {
        	throw new RuntimeException("unable to post to the PDF service");
        } catch (HttpClientErrorException e) {
            JsonObject json = (new JsonParser()).parse(e.getResponseBodyAsString()).getAsJsonObject();
            throw new RuntimeException("Error occurred posting to the PDF service: " + json.getAsJsonPrimitive("message").getAsString());
        }

    }

}
