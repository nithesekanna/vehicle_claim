package com.maan.veh.claim.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maan.veh.claim.dto.InsuredVehicleInfoDTO;
import com.maan.veh.claim.dto.InsuredVehicleMasterDTO;
import com.maan.veh.claim.entity.ApiTransactionLog;
import com.maan.veh.claim.entity.CoreInsuredVehicleInfo;
import com.maan.veh.claim.entity.InsuredVehicleInfo;
import com.maan.veh.claim.entity.InsuredVehicleInfoId;
import com.maan.veh.claim.external.ErrorResponse;
import com.maan.veh.claim.external.VcInuredVehicleApiReponse;
import com.maan.veh.claim.repository.ApiTransactionLogRepository;
import com.maan.veh.claim.repository.CoreInsuredVehicleInfoRepository;
import com.maan.veh.claim.repository.InsuredVehicleInfoRepository;
import com.maan.veh.claim.response.CommonResponse;
import com.maan.veh.claim.response.VcInuredVehicleApiResponseQIIC;
import com.maan.veh.claim.response.VcinsuredVehicleResponse;
import com.maan.veh.claim.response.VcinsuredVehicleResponseQIIC;
import com.maan.veh.claim.service.VcInsuredVehicleInfoService;

@Service
public class VcInsuredVehicleInfoServiceImpl implements VcInsuredVehicleInfoService {
	
	private Logger logger = LogManager.getLogger(VcInsuredVehicleInfoServiceImpl.class);
	
	@Autowired
	private InsuredVehicleInfoRepository repository;
	
	@Autowired
	private CoreInsuredVehicleInfoRepository coreRepository;
	
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ApiTransactionLogRepository apiTransactionLogRepo;
     
	@Value("${auth.username}")
	private String apiusername;

	@Value("${auth.password}")
	private String apipassword;

	@Value("${external.api.url.authenticate}")
	private String externalApiUrlAuthenticate;

	@Value("${external.api.url.vehicledetails}")
	private String Authenticate;

	
	
	@Override
	public CommonResponse saveInsuredVehicle(InsuredVehicleMasterDTO requestPayload) {
		CommonResponse response = new CommonResponse();
	    ApiTransactionLog transactionLog = new ApiTransactionLog();
	    transactionLog.setRequestTime(LocalDateTime.now());
	    transactionLog.setEntryDate(new Date());

	    try {
	        // Prepare request payload
	        InsuredVehicleInfoDTO newdata = new InsuredVehicleInfoDTO(
	            requestPayload.getPartyId(),
	            requestPayload.getCategoryId(),
	            requestPayload.getProdId()
	        );

	        // Authenticate user and get JWT token
	        String jwtToken = authenticateUserCall();

	        // Set up headers
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + jwtToken);
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        // Send request to external API
	        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(newdata), headers);
	        setupSSLContext();
	        ResponseEntity<String> apiResponse = restTemplate.postForEntity(Authenticate, entity, String.class);

	        // Parse API response
	        VcinsuredVehicleResponseQIIC externalApiResponse = objectMapper.readValue(apiResponse.getBody(), VcinsuredVehicleResponseQIIC.class);
	        List<VcInuredVehicleApiResponseQIIC> apiData = externalApiResponse.getData();
	        if (apiData == null || apiData.isEmpty()) {
	            response.setMessage("No data received from API");
	            response.setIsError(false);
	            return response;
	        }

	        // Create a set of IDs for batch fetching
	        Set<InsuredVehicleInfoId> insuredIds = apiData.stream()
	            .map(insured -> new InsuredVehicleInfoId(
	                requestPayload.getCompanyid(),
	                insured.getPolicyNo(),
	                insured.getFileNo(),
	                requestPayload.getGarageid()
	            ))
	            .collect(Collectors.toSet());

	        // ✅ Fetch existing records in ONE bulk query
	        Set<InsuredVehicleInfoId> existingIds = repository.findExistingIds(insuredIds);

	        // Process only new records
	        List<InsuredVehicleInfo> insuredInfoList = apiData.stream()
	            .filter(insured -> !existingIds.contains(new InsuredVehicleInfoId(
	                requestPayload.getCompanyid(),
	                insured.getPolicyNo(),
	                insured.getFileNo(),
	                requestPayload.getGarageid()
	            )))
	            .map(insured -> {
	                InsuredVehicleInfo insuredVehicleInfo = new InsuredVehicleInfo();
	                insuredVehicleInfo.setCompanyId(requestPayload.getCompanyid());
	                insuredVehicleInfo.setPolicyNo(insured.getPolicyNo());
	                //insuredVehicleInfo.setClaimNo(insured.getClaimNo());
	                insuredVehicleInfo.setClaimNo(insured.getFileNo());
	                insuredVehicleInfo.setGarageId(requestPayload.getGarageid());
	                insuredVehicleInfo.setVehicleMake(insured.getMake());
	                insuredVehicleInfo.setVehicleModel(insured.getModel());
	                insuredVehicleInfo.setMakeYear(insured.getYear());
	                insuredVehicleInfo.setChassisNo(insured.getChassisNo());
	                insuredVehicleInfo.setInsuredName(insured.getInsuredName());
	                insuredVehicleInfo.setType(insured.getBodyType());
	                insuredVehicleInfo.setLossLocation(insured.getLossLocation());
	                insuredVehicleInfo.setVehicleRegNo(insured.getVehRegNo());
	                insuredVehicleInfo.setEntryDate(new Date());
	                insuredVehicleInfo.setStatus("Y");
	                insuredVehicleInfo.setFnolSgsId(insured.getFnolSgsId());
	                
	                // Newly added fields
	                insuredVehicleInfo.setWorkOrderType(insured.getWorkOrderType());
	                insuredVehicleInfo.setEngineNo(insured.getEngineNo());
	                insuredVehicleInfo.setClaimantType(insured.getClaimantType());
	                insuredVehicleInfo.setLossLocationDesc(insured.getLossLocationDesc());
	                insuredVehicleInfo.setClaimStatus(insured.getClaimStatus());
	                //insuredVehicleInfo.setFileNo(insured.getFileNo());
	                insuredVehicleInfo.setFileNo(insured.getClaimNo());
	                insuredVehicleInfo.setGarageAddress(insured.getGarageAddress());
	                insuredVehicleInfo.setPlateType(insured.getPlateType());
	                
	                // Default values
	                insuredVehicleInfo.setSurveyorId("surveyor_test1");
	                insuredVehicleInfo.setDealerId("dealer_test1");
	                insuredVehicleInfo.setLpoId(insured.getLpoId());
	                insuredVehicleInfo.setVehId(insured.getVehId());
	                insuredVehicleInfo.setClcpId(insured.getClcpId());
	                insuredVehicleInfo.setProdId(insured.getProdId());
	                insuredVehicleInfo.setFnolNo(insured.getFnolNo());
	                insuredVehicleInfo.setMobileNo(insured.getMobileNo());
	                insuredVehicleInfo.setMobileCode(insured.getMobileCode());
	                
	                return insuredVehicleInfo;
	            })
	            .collect(Collectors.toList());

	        // Save all new records in bulk
	        if (!insuredInfoList.isEmpty()) {
	            repository.saveAll(insuredInfoList);
	        }

	        // Success response
	        response.setMessage("Data saved successfully");
	        response.setIsError(false);
	        response.setResponse(externalApiResponse);
	    } catch (Exception e) {
	        transactionLog.setStatus("FAILURE");
	        transactionLog.setErrorMessage(e.getMessage());
	        response.setMessage("Failed to save data");
	        response.setIsError(true);
	        response.setErrors(Collections.singletonList(new ErrorResponse("100", "API Error", e.getMessage())));
	    } finally {
	        transactionLog.setResponseTime(LocalDateTime.now());
	    }

	    return response;
	}

	public CommonResponse saveInsuredVehiclV0(InsuredVehicleMasterDTO requestPayload) {
		CommonResponse response = new CommonResponse();
	
		ApiTransactionLog transactionLog = new ApiTransactionLog();
		transactionLog.setRequestTime(LocalDateTime.now());
		transactionLog.setEntryDate(new Date());
		
		try {
			//InsuredVehicleInfo newData = new InsuredVehicleInfo();
	        InsuredVehicleInfoDTO newdatas=new InsuredVehicleInfoDTO();
			newdatas.setPartyId(requestPayload.getPartyId());
			newdatas.setCategoryId(requestPayload.getCategoryId());
			newdatas.setProdId(requestPayload.getProdId());
			try {
				// Authenticate and get JWT token
				String jwtToken = authenticateUserCall();
				
				// Create headers with JWT token
				HttpHeaders headers = new HttpHeaders();
				headers.set("Authorization", "Bearer " + jwtToken);
				headers.setContentType(MediaType.APPLICATION_JSON);
				
				// Prepare request body
				String requestBody = objectMapper.writeValueAsString(newdatas);
				HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
				
				// Set up SSL context for secure communication
				setupSSLContext();
				
				// Send request to external API with JWT token
				ResponseEntity<String> apiResponse = restTemplate.postForEntity(Authenticate, entity, String.class);
				// Parse response
//                VcinsuredVehicleResponse externalApiResponse = objectMapper.readValue(apiResponse.getBody(), VcinsuredVehicleResponse.class);
//                List<InsuredVehicleInfo> InsuredInfo = new ArrayList<>();
//				List<VcInuredVehicleApiReponse> data = externalApiResponse.getData();
//				
//				//saveInCoreTable(data,requestPayload);
//				
//				for(VcInuredVehicleApiReponse insured:data) {
//					InsuredVehicleInfo insuredVehicleInfo =new  InsuredVehicleInfo();
//					
//					 InsuredVehicleInfoId insuredId = new InsuredVehicleInfoId(
//							    requestPayload.getCompanyid(),
//					            insured.getPolicyno(),
//					            insured.getClaimno(),
//					            requestPayload.getGarageid()
//					    );
//					 Optional<InsuredVehicleInfo> optionlData = repository.findById(insuredId);
////					 if(optionlData.isPresent() && optionlData.get().getQuotationNo()!=null) {
//					 if(optionlData.isPresent()) {
//						 continue;
//					 }
//					 
//					insuredVehicleInfo.setClaimNo(insured.getClaimno());
//					insuredVehicleInfo.setCompanyId(requestPayload.getCompanyid());//
//					insuredVehicleInfo.setPolicyNo(insured.getPolicyno());
//					insuredVehicleInfo.setVehicleMake(insured.getMake());//
//					insuredVehicleInfo.setVehicleModel(insured.getModel());//
//					insuredVehicleInfo.setMakeYear(insured.getYear());//
//					insuredVehicleInfo.setChassisNo(insured.getChassisno());
//					insuredVehicleInfo.setInsuredName(insured.getInsuredname());
//					insuredVehicleInfo.setType(insured.getBodytype());
//					insuredVehicleInfo.setLossLocation(insured.getLosslocation());
//					insuredVehicleInfo.setVehicleRegNo(insured.getVehregno());
//					insuredVehicleInfo.setEntryDate(new Date());
//					insuredVehicleInfo.setStatus("Y");
//					insuredVehicleInfo.setFnolSgsId(insured.getFnolsgsid());
//					insuredVehicleInfo.setGarageId(requestPayload.getGarageid());
//					//entering default surveyor and garage
//					insuredVehicleInfo.setSurveyorId("surveyor_test1");
//					insuredVehicleInfo.setDealerId("dealer_test1");
//					insuredVehicleInfo.setLpoId(insured.getLpoId());
//					InsuredInfo.add(insuredVehicleInfo);
//					}
//				    repository.saveAll(InsuredInfo);
//				    response.setMessage("Data saved successfully");
//					response.setIsError(false);
//					response.setResponse(externalApiResponse);
				VcinsuredVehicleResponse externalApiResponse = objectMapper.readValue(apiResponse.getBody(), VcinsuredVehicleResponse.class);
				List<InsuredVehicleInfo> insuredInfoList = new ArrayList<>();
				List<VcInuredVehicleApiReponse> apiData = externalApiResponse.getData();

				// Create a set of IDs to check for existing records in bulk
				Set<InsuredVehicleInfoId> insuredIds = apiData.stream()
				    .map(insured -> new InsuredVehicleInfoId(
				        requestPayload.getCompanyid(),
				        insured.getPolicyno(),
				        insured.getClaimno(),
				        requestPayload.getGarageid()
				    ))
				    .collect(Collectors.toSet());

				// Fetch existing records in one query
				Set<InsuredVehicleInfoId> existingIds = repository.findAllById(insuredIds)
					    .stream()
					    .map(insured -> new InsuredVehicleInfoId(
					        insured.getCompanyId(),
					        insured.getPolicyNo(),
					        insured.getClaimNo(),
					        insured.getGarageId()
					    )) // 
					    .collect(Collectors.toSet());


				for (VcInuredVehicleApiReponse insured : apiData) {
				    InsuredVehicleInfoId insuredId = new InsuredVehicleInfoId(
				        requestPayload.getCompanyid(),
				        insured.getPolicyno(),
				        insured.getClaimno(),
				        requestPayload.getGarageid()
				    );

				    // Skip existing records
				    if (existingIds.contains(insuredId)) {
				        continue;
				    }

				    InsuredVehicleInfo insuredVehicleInfo = new InsuredVehicleInfo();
				    insuredVehicleInfo.setClaimNo(insured.getClaimno());
				    insuredVehicleInfo.setCompanyId(requestPayload.getCompanyid());
				    insuredVehicleInfo.setPolicyNo(insured.getPolicyno());
				    insuredVehicleInfo.setVehicleMake(insured.getMake());
				    insuredVehicleInfo.setVehicleModel(insured.getModel());
				    insuredVehicleInfo.setMakeYear(insured.getYear());
				    insuredVehicleInfo.setChassisNo(insured.getChassisno());
				    insuredVehicleInfo.setInsuredName(insured.getInsuredname());
				    insuredVehicleInfo.setType(insured.getBodytype());
				    insuredVehicleInfo.setLossLocation(insured.getLosslocation());
				    insuredVehicleInfo.setVehicleRegNo(insured.getVehregno());
				    insuredVehicleInfo.setEntryDate(new Date());
				    insuredVehicleInfo.setStatus("Y");
				    insuredVehicleInfo.setFnolSgsId(insured.getFnolsgsid());
				    insuredVehicleInfo.setGarageId(requestPayload.getGarageid());

				    // Default values
				    insuredVehicleInfo.setSurveyorId("surveyor_test1");
				    insuredVehicleInfo.setDealerId("dealer_test1");
				    insuredVehicleInfo.setLpoId(insured.getLpoId());

				    insuredInfoList.add(insuredVehicleInfo);
				}

				// Save all new records in bulk
				if (!insuredInfoList.isEmpty()) {
				    repository.saveAll(insuredInfoList);
				}

				response.setMessage("Data saved successfully");
				response.setIsError(false);
				response.setResponse(externalApiResponse);

                  } catch (Exception e) {
				   transactionLog.setStatus("FAILURE");
				   transactionLog.setErrorMessage(e.getMessage());
				   response.setMessage("Failed to save data");
				   response.setIsError(true);
				   response.setErrors(Collections.singletonList(new ErrorResponse("100", "API Error", e.getMessage()))); // API Error error
			}
		} catch (Exception e) {
			transactionLog.setStatus("FAILURE");
			transactionLog.setErrorMessage(e.getMessage());
			response.setMessage("Failed to process the request");
			response.setIsError(true);
			response.setErrors(Collections.singletonList(new ErrorResponse("101", "Processing Error", e.getMessage())));
		} finally {
			transactionLog.setResponseTime(LocalDateTime.now());

		}
		return response;  // Return the response
	}

	private void saveInCoreTable(List<VcInuredVehicleApiReponse> data, InsuredVehicleMasterDTO requestPayload) {
		try {
			List<CoreInsuredVehicleInfo> InsuredInfo = new ArrayList<>();
			for (VcInuredVehicleApiReponse insured : data) {
				CoreInsuredVehicleInfo insuredVehicleInfo = new CoreInsuredVehicleInfo();

				insuredVehicleInfo.setClaimNo(insured.getClaimno());
				insuredVehicleInfo.setCompanyId(requestPayload.getCompanyid());//
				insuredVehicleInfo.setPolicyNo(insured.getPolicyno());
				insuredVehicleInfo.setVehicleMake(insured.getMake());//
				insuredVehicleInfo.setVehicleModel(insured.getModel());//
				insuredVehicleInfo.setMakeYear(insured.getYear());//
				insuredVehicleInfo.setChassisNo(insured.getChassisno());
				insuredVehicleInfo.setInsuredName(insured.getInsuredname());
				insuredVehicleInfo.setType(insured.getBodytype());
//				System.out.println(insured.getBodytype());
				insuredVehicleInfo.setLossLocation(insured.getLosslocation());
				insuredVehicleInfo.setVehicleRegNo(insured.getVehregno());
//				System.out.println(insured.getVehregno());
				insuredVehicleInfo.setEntryDate(new Date());
				insuredVehicleInfo.setStatus(insured.getClaimstatus());
				insuredVehicleInfo.setFnolSgsId(insured.getFnolsgsid());
				insuredVehicleInfo.setGarageId(requestPayload.getGarageid());
				// entering default surveyor and garage
//				insuredVehicleInfo.setSurveyorId("surveyor_test1");
//				insuredVehicleInfo.setDealerId("dealer_test1");
				insuredVehicleInfo.setLpoId(insured.getLpoId());
				InsuredInfo.add(insuredVehicleInfo);
			}
			    coreRepository.saveAll(InsuredInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void setupSSLContext() throws Exception {
		TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
			}
		};
		
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
	}

	public String authenticateUserCall() {
		ApiTransactionLog transactionLog = new ApiTransactionLog();
		transactionLog.setRequestTime(LocalDateTime.now());
		transactionLog.setEntryDate(new Date());
		transactionLog.setEndpoint(externalApiUrlAuthenticate);

		// Prepare request payload for authentication
		Map<String, String> formattedRequest = Map.of(
			"username", apiusername,
			"password", apipassword
		);

		try {
			// Convert Map to JSON string
			String requestBody = objectMapper.writeValueAsString(formattedRequest);
			transactionLog.setRequest(requestBody);
			
			// Set headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
			
			// Set up SSL context
			setupSSLContext();
			
			// Send authentication request
			ResponseEntity<String> apiResponse = restTemplate.postForEntity(externalApiUrlAuthenticate, entity, String.class);
			transactionLog.setResponse(apiResponse.getBody());
			transactionLog.setStatus("SUCCESS");

			// Extract JWT token
			Map<String, String> responseMap = objectMapper.readValue(apiResponse.getBody(), Map.class);
			return responseMap.get("jwt");  // Return the JWT token
		} catch (Exception e) {
			transactionLog.setStatus("FAILURE");
			transactionLog.setErrorMessage(e.getMessage());
		} finally {
			transactionLog.setResponseTime(LocalDateTime.now());
			//apiTransactionLogRepo.save(transactionLog);
			logger.info(externalApiUrlAuthenticate + " ====> " +transactionLog);
		}
		return null;  // Return null if authentication fails
	}
}
