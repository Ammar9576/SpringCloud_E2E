package com.nbc.custom_reports.service.methodman;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.nbc.custom_reports.domain.olympics.LinearProposalRO;
import com.nbc.custom_reports.service.olympics.ConvergenceReportService;
import com.nbc.custom_reports.service.olympics.DigitalService;

@Service
public class DetailsService<K,V> {
	
	@Autowired
	DigitalService digitalService;
	
	@Autowired
	SummaryService summaryService;
	
	@Autowired
	ConvergenceReportService convSvc;

	@SuppressWarnings("unchecked")
	public Map<String,String> getMethodManCombinedDetails(String jsonStr, String sessionId, Long userId, JsonObject reportParams) throws Exception {
		Map<String,String> pdfInputParamMap =new HashMap<String,String>();
		try {
			if (userId == null)
				throw new RuntimeException("SSO Lookup for TAD User failed");

			HashMap<String, HashMap<String, String>> digitalDeals = digitalService.getDigitalParams(jsonStr);
			Map<String, Map<K, V>> digitalDataMap = digitalService.fetchDigitalData(userId, sessionId, digitalDeals);
			LinearProposalRO linearProposalRO = convSvc.fetchLinearProposalData(jsonStr);
			pdfInputParamMap.put("linearHeader", linearProposalRO.getLinearDeal());
			pdfInputParamMap.put("linearDetails",linearProposalRO.getLinearHtml());
			pdfInputParamMap.put("digitalDetails",digitalService.buildDigitalOrderHtml(digitalDataMap,"DigitalDetailsHeaderMethodMan",Boolean.parseBoolean(summaryService.getAdminDataByName("Suppress Children"))));
		} catch (Exception e) {
			throw e;
		}
		return pdfInputParamMap;
	}
	
}
