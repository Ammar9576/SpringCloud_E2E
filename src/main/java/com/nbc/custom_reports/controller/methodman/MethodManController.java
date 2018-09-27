package com.nbc.custom_reports.controller.methodman;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.nbc.custom_reports.domain.PayloadErrorLogger;
import com.nbc.custom_reports.domain.methodman.ResponseObject;
import com.nbc.custom_reports.domain.methodman.Summary;
import com.nbc.custom_reports.service.methodman.DashboardService;
import com.nbc.custom_reports.service.methodman.DetailsService;
import com.nbc.custom_reports.service.methodman.SummaryService;
import com.nbc.custom_reports.service.olympics.ConvergenceReportService;
import com.nbc.custom_reports.service.olympics.DigitalService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/methodman")
@Api(description = "Dashboard & Summary Api's", tags = {"Methodman"})
public class MethodManController {

	@Autowired
	DashboardService<String,Object> dashboardService;
	
	@Autowired
	ConvergenceReportService convergenceService;
	
	@Autowired
	DetailsService detailsSvc;
	
	@Autowired
	DigitalService digitalService;
	
	@Autowired
	SummaryService summSvc;
	
	@RequestMapping(value = "/getDashboardDetails",produces = "application/json", method = RequestMethod.GET)
	public ResponseEntity<?> getDashboardDetails(@ApiParam(value="Specify convergence deal Id",required=true) @RequestParam String convgDealId) {	
		ResponseObject res=null;
		try {	
			return new ResponseEntity<String>(dashboardService.getDashboardDetails(convgDealId),HttpStatus.OK);
		}catch (Exception e) {
			return new ResponseEntity<ResponseObject>(new ResponseObject("Exception while getting dashboard details for "+convgDealId+"--"+e.getMessage(),false), HttpStatus.BAD_REQUEST);
		}
	}
		
	/*@RequestMapping(value = "/getReport", method = RequestMethod.POST)
	public void getMethodManReport() {
		
	}*/
	@ApiIgnore
	@ResponseBody
	@RequestMapping(value = "/getLinearAndDigitalDetailsHtml", method = RequestMethod.POST)
	public ResponseEntity getMethodManCombinedDetails(@RequestBody String jsonStr) {
		PayloadErrorLogger logger = null;
		Map<String,Object> pdfInputParamMap = null;
		String errorMsg = null;
		String sessionId = null;
		Long userId=0L;
		JsonPrimitive tadUser = null;
		Map<String,String> result=null;
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
			result=detailsSvc.getMethodManCombinedDetails(jsonStr, sessionId, userId, params);
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
		return new ResponseEntity(result,HttpStatus.OK);
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/getGrandSummaryData/{dealId}", method = RequestMethod.GET)
	@ApiIgnore
	public ResponseObject getGrandSummaryData(@ApiParam(value="Specify convergence deal Id") @PathVariable Long dealId,@RequestParam(required=false) String sso, HttpServletResponse response) {
		String sessionId = null;
		Long userId=0L;
		ResponseObject res=null;
		try {
			sessionId = UUID.randomUUID().toString();
			
			userId = convergenceService.lookupTADUser();
			Map<String,List<Summary>> grandSummary=summSvc.getGrandSummaryDataAndDetails(dealId,sso);		
			res = new ResponseObject(grandSummary,true);
			
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}finally {
			try {
			
			}catch(Exception e){
				throw new RuntimeException("Error while logging input--"+e.getMessage());
			}
		}
		return res;
	}
	
	@ResponseBody
	@RequestMapping(value = "/getGrandSummaryDataAndDetails/{dealId}", method = RequestMethod.GET)
	public ResponseEntity<ResponseObject>  getGrandSummaryDataAndDetails(@ApiParam(value="Specify convergence deal Id",required=true) @PathVariable Long dealId,@ApiParam(value="Specify sso in FirstName.LastName(ssoID) format") @RequestParam(required=false) String sso, HttpServletResponse response) {
		String sessionId = null;
		Long userId=0L;
		ResponseObject res=null;
		try {
			/*sessionId = UUID.randomUUID().toString();
			
			userId = convergenceService.lookupTADUser();*/
			Map<String,List<Summary>> grandSummary=summSvc.getGrandSummaryDataAndDetails(dealId,sso);	
			if(grandSummary==null){
				return new ResponseEntity<ResponseObject>(new ResponseObject("Grand Summary details is empty",false), HttpStatus.BAD_REQUEST);
			}
			
			
			res = new ResponseObject(grandSummary,true);
		
		} catch (Exception e) {
			return new ResponseEntity<ResponseObject>(new ResponseObject(e.getMessage(),false), HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<ResponseObject>(res, HttpStatus.OK);
	}
	
}
