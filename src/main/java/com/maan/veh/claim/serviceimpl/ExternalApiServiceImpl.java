package com.maan.veh.claim.serviceimpl;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maan.veh.claim.dto.ClaimIntimationDTOAttachmentDetails;
import com.maan.veh.claim.dto.ClaimIntimationDTODocumentDetails;
import com.maan.veh.claim.dto.ClaimIntimationDTODriver;
import com.maan.veh.claim.dto.ClaimIntimationDTORequestMetaData;
import com.maan.veh.claim.dto.ClaimIntimationDTOThirdPartyInfo;
import com.maan.veh.claim.dto.ClaimTransactionRequestDTO;
import com.maan.veh.claim.dto.ClaimTransactionRequestDTOMetaData;
import com.maan.veh.claim.dto.ClaimentCoverageRequestDTO;
import com.maan.veh.claim.dto.ClaimentCoverageResponseDTO;
import com.maan.veh.claim.dto.ClaimsDetailsRequestDto;
import com.maan.veh.claim.dto.ClaimsDetailsResponseDto;
import com.maan.veh.claim.dto.ClaimsDetailsResponseDto.ClaimDetails;
import com.maan.veh.claim.dto.FnolRequestDTO;
import com.maan.veh.claim.dto.FnolRequestDTOMetaData;
import com.maan.veh.claim.dto.GarageClaimListDto;
import com.maan.veh.claim.dto.GarageClaimListResponseDTO;
import com.maan.veh.claim.dto.SaveClaimRequestDTO;
import com.maan.veh.claim.dto.SaveSparePartsDTO;
import com.maan.veh.claim.entity.ApiIntegMaster;
import com.maan.veh.claim.entity.ApiTransactionLog;
import com.maan.veh.claim.entity.ClaimIntimationDetails;
import com.maan.veh.claim.entity.ClaimIntimationDetailsId;
import com.maan.veh.claim.entity.DamageSectionDetails;
import com.maan.veh.claim.entity.GarageWorkOrder;
import com.maan.veh.claim.entity.InsuredVehicleInfo;
import com.maan.veh.claim.entity.InsuredVehicleInfoId;
import com.maan.veh.claim.entity.LoginMaster;
import com.maan.veh.claim.entity.SparePartsSaveDetails;
import com.maan.veh.claim.entity.VcClaimStatus;
import com.maan.veh.claim.entity.VcDocumentUploadDetails;
import com.maan.veh.claim.external.ErrorDetail;
import com.maan.veh.claim.external.ErrorResponse;
import com.maan.veh.claim.external.ExternalApiResponse;
import com.maan.veh.claim.file.BASE64DecodedMultipartFile;
import com.maan.veh.claim.file.DocumentUploadDetailsReqRes;
import com.maan.veh.claim.file.MultipartInputStreamFileResource;
import com.maan.veh.claim.qiic.dto.CreateWorkBasketRequestDto;
import com.maan.veh.claim.qiic.dto.DownloadDocumentRequest;
import com.maan.veh.claim.qiic.dto.DownloadDocumentRequestDto;
import com.maan.veh.claim.qiic.dto.FileUploadRequestDto;
import com.maan.veh.claim.qiic.dto.GarageSettlementListRequestDto;
import com.maan.veh.claim.qiic.dto.GarageSettlementListResponseDto;
import com.maan.veh.claim.qiic.dto.GarageSettlementResponseDto;
import com.maan.veh.claim.qiic.dto.GetGarageWorkOrderRequestDto;
import com.maan.veh.claim.qiic.dto.LpoApprovalSyncReqDto;
import com.maan.veh.claim.qiic.dto.UploadedDocumentListResponseDto;
import com.maan.veh.claim.qiic.dto.WorkOrderDetailResponseDto;
import com.maan.veh.claim.qiic.dto.WorkOrderPendingResponseDto;
import com.maan.veh.claim.qiic.request.GarageSettlementListRequest;
import com.maan.veh.claim.qiic.request.GetGarageWorkOrderRequest;
import com.maan.veh.claim.qiic.request.LpoApprovalSyncRequest;
import com.maan.veh.claim.qiic.response.LpoApprovalSyncDamageResponse;
import com.maan.veh.claim.qiic.response.LpoApprovalSyncResponse;
import com.maan.veh.claim.repository.ApiIntegMasterRepository;
import com.maan.veh.claim.repository.ApiTransactionLogRepository;
import com.maan.veh.claim.repository.ClaimIntimationDetailsRepository;
import com.maan.veh.claim.repository.DamageSectionDetailsRepository;
import com.maan.veh.claim.repository.GarageWorkOrderRepository;
import com.maan.veh.claim.repository.InsuredVehicleInfoRepository;
import com.maan.veh.claim.repository.LoginMasterRepository;
import com.maan.veh.claim.repository.SparePartsSaveDetailsRepository;
import com.maan.veh.claim.repository.VcClaimStatusRepository;
import com.maan.veh.claim.repository.VcDocumentUploadDetailsRepository;
import com.maan.veh.claim.request.CheckClaimStatusRequest;
import com.maan.veh.claim.request.ClaimIntimationDocumentDetails;
import com.maan.veh.claim.request.ClaimIntimationRequestMetaData;
import com.maan.veh.claim.request.ClaimIntimationThirdPartyInfo;
import com.maan.veh.claim.request.ClaimListRequest;
import com.maan.veh.claim.request.ClaimListRequestDTO;
import com.maan.veh.claim.request.ClaimTransactionRequest;
import com.maan.veh.claim.request.ClaimentCoverageRequest;
import com.maan.veh.claim.request.ExternalVehicleGarageViewRequest;
import com.maan.veh.claim.request.FnolRequest;
import com.maan.veh.claim.request.GarageSectionDetailsSaveReq;
import com.maan.veh.claim.request.GetClaimRequest;
import com.maan.veh.claim.request.LoginRequest;
import com.maan.veh.claim.request.SaveClaimRequest;
import com.maan.veh.claim.request.SaveSparePartsRequest;
import com.maan.veh.claim.request.SaveSparePartsRequestMetaData;
import com.maan.veh.claim.request.VehicleDamageDetailRequest;
import com.maan.veh.claim.response.CheckClaimStatusResponse;
import com.maan.veh.claim.response.ClaimIntimationResponse;
import com.maan.veh.claim.response.ClaimListResponse;
import com.maan.veh.claim.response.ClaimentCoverageResponse;
import com.maan.veh.claim.response.CommonResponse;
import com.maan.veh.claim.response.DropDownRes;
import com.maan.veh.claim.response.ErrorList;
import com.maan.veh.claim.response.GetAllQuoteResponse;
import com.maan.veh.claim.service.ExternalApiService;

@Service
public class ExternalApiServiceImpl implements ExternalApiService {
	
	private static final Logger logger = Logger.getLogger(ExternalApiServiceImpl.class.getName());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ClaimIntimationDetailsRepository claimIntimationDetailsRepository;

    @Autowired
    private ApiTransactionLogRepository apiTransactionLogRepo;
    
    @Autowired
    private GarageWorkOrderRepository garageWorkOrderRepo;
    
    @Autowired
    private DamageSectionDetailsRepository damageSectionDetailsRepo;
    
    @Autowired
    private VcClaimStatusRepository vcClaimStatusRepo;
    
    @Autowired
    private LoginMasterRepository loginMasterRepo;
    
    @Autowired
    private ApiIntegMasterRepository apiIntegMasterRepository;
    
    @Value("${external.api.url.createfnol}")  
    private String externalApiUrlCreatefnol;
    
    @Value("${external.api.url.claimlisting}")  
    private String externalApiUrlClaimListing;
    
    @Value("${external.api.url.savespareparts}")  
    private String externalApiUrlSaveSpareparts;
    
    @Value("${external.api.url.garagelist}")  
    private String externalApiUrlGarageList;
    
    @Value("${external.api.url.getfnol}")  
    private String externalApiUrlGetfnol;
    
    @Value("${external.api.url.getfnolstatus}")
    private String externalApiUrlGetFnolStatus;
    
    @Value("${external.api.url.getClaimsDetailsByClaimNo}")
    private String checkClaimStatusApi;
    
    @Value("${external.api.url.listClaimantCoverages}")
    private String listClaimantCoverages;
    
    @Value("${external.api.url.authenticate}")
    private String externalApiUrlAuthenticate;
    
    @Value("${external.api.url.uploaddoc}")
    private String externalApiUrlUploadDoc;
    
    @Value("${auth.username}")
    private String apiusername;

    @Value("${auth.password}")
    private String apipassword;
    
    @Value("${common.path}")
	private Path rootLocation;
    
    @Autowired
    private InputValidationUtil validation;
    
    @Autowired
    private SparePartsSaveDetailsRepository SparePartsSaveDetailsRepo;
    
    @Autowired
	private LoginMasterRepository loginRepo;
    
    @Autowired
    private DropDownServiceImpl dropDownServiceImpl;
    
    @Autowired
    private InsuredVehicleInfoRepository repository;
    
	@Autowired
	private VcDocumentUploadDetailsRepository documentUploadDetailsRepo;

    @Override
    public CommonResponse createFnol(SaveClaimRequest requestPayload) {
        CommonResponse response = new CommonResponse();
        ApiTransactionLog log = new ApiTransactionLog();
        log.setRequestTime(LocalDateTime.now());
        log.setEntryDate(new Date());
        log.setEndpoint(externalApiUrlCreatefnol);

        // Validate requestPayload
        List<ErrorList> validationErrors = validation.validateClaimIntemationDetails(requestPayload);
        if (!validationErrors.isEmpty()) {
            response.setErrors(validationErrors);
            response.setMessage("Validation failed");
            response.setResponse(Collections.emptyMap());
            response.setIsError(true);
            return response;
        }
        try {
			ClaimIntimationDetails newData = new ClaimIntimationDetails();
			//Optional<ClaimIntimationDetails> optional = claimIntimationDetailsRepository.findByPolicyNo(requestPayload.getPolicyNo());
			Optional<ClaimIntimationDetails> optional = claimIntimationDetailsRepository.findByPolicyNoAndPoliceReportNo(requestPayload.getPolicyNo(),requestPayload.getPoliceReportNo());
			if(optional.isPresent()){
				newData = optional.get();
			}
    // Set fields from requestPayload into newData
			newData.setPolicyNo(requestPayload.getPolicyNo());
			newData.setRequestOrigin("API");
			newData.setCurrentBranch(requestPayload.getRequestMetaData().getCurrentBranch());
			newData.setOriginBranch(requestPayload.getRequestMetaData().getOriginBranch());
			newData.setUserName(requestPayload.getRequestMetaData().getUserName());
			newData.setIpAddress(requestPayload.getRequestMetaData().getIpAddress());
			newData.setRequestGeneratedDateTime(new Date()); // Assuming this is the current date and time
			newData.setConsumerTrackingId(requestPayload.getRequestMetaData().getConsumerTrackingID());
			newData.setLanguageCode(requestPayload.getLanguageCode());
			newData.setInsuredId(requestPayload.getInsuredId());
			newData.setLossDate(requestPayload.getLossDate());
			newData.setIntimatedDate(requestPayload.getIntimatedDate());
			newData.setLossLocation(requestPayload.getLossLocation());
			newData.setNatureOfLoss(requestPayload.getNatureOfLoss());
			newData.setPoliceStation(requestPayload.getPoliceStation());
			newData.setPoliceReportNo(requestPayload.getPoliceReportNo());
			newData.setLossDescription(requestPayload.getLossDescription());
			newData.setAtFault(requestPayload.getAtFault());
			claimIntimationDetailsRepository.save(newData);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Error while saving data");
		}
        try {
            // Extract JWT token from request
            String jwtToken = authenticateUserCall();
            
            // Create headers with JWT token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Convert requestPayload to JSON and add headers
            SaveClaimRequestDTO dto = mapToSaveClaimRequestDTO(requestPayload);
            
            String requestBody = objectMapper.writeValueAsString(dto);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            log.setRequest(requestBody);
            logger.info(requestBody);
            // Configure SSL Trust Managers (if necessary)
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            // Send request to external API with JWT in Authorization header
            ResponseEntity<String> apiResponse = restTemplate.postForEntity(log.getEndpoint(), entity, String.class);
            log.setResponse(apiResponse.getBody());
            log.setStatus("SUCCESS");
            
            // Parse the raw response into ExternalApiResponse object
            ExternalApiResponse externalApiResponse = objectMapper.readValue(apiResponse.getBody(), ExternalApiResponse.class);
            
          //saving fnol number
            try{
            	ClaimIntimationDetails oldData = new ClaimIntimationDetails();
    			
            	Optional<ClaimIntimationDetails> optional = claimIntimationDetailsRepository.findByPolicyNoAndPoliceReportNo(requestPayload.getPolicyNo(),requestPayload.getPoliceReportNo());
    			if(optional.isPresent()){
    				oldData = optional.get();
    				oldData.setFnolNo(externalApiResponse.getData().getFnolNo());
    				oldData.setClaimStatusCode(externalApiResponse.getData().getClaimStatusCode());
    				oldData.setClaimType(externalApiResponse.getData().getClaimType());
    				oldData.setFnolSgsId(externalApiResponse.getData().getFnolSgsId());
    				oldData.setClaimPartyId(externalApiResponse.getData().getClaimPartyId());
    				claimIntimationDetailsRepository.save(oldData);
    			}
    			
            }catch (Exception e) {
    			// TODO Auto-generated catch block
    			System.out.println("Error while saving data");
    		}

            if (externalApiResponse.isHasError()) {
                // Create custom error response
                List<ErrorResponse> errorList = new ArrayList<>();
                for (ErrorDetail error : externalApiResponse.getData().getErrorDetailsList()) {
                    errorList.add(new ErrorResponse("Azentio API "+externalApiUrlCreatefnol, error.getErrorField(), error.getErrorDescription()));
                }
                response.setErrors(errorList);
                response.setMessage(externalApiResponse.getMessage());
                response.setResponse(Collections.emptyMap());
                response.setIsError(true);
            } else {
                response.setMessage("Data saved successfully");
                response.setIsError(false);
                response.setResponse(externalApiResponse);
            }

        } catch (Exception e) {
            log.setStatus("FAILURE");
            log.setErrorMessage(e.getMessage());
            response.setMessage("Failed to save data");
            response.setIsError(true);
            response.setErrors(Collections.singletonList(new ErrorResponse("100", "API Error", e.getMessage()))); // API Error error
        } finally {
            log.setResponseTime(LocalDateTime.now());
            //apiTransactionLogRepo.save(log);
            logger.info(externalApiUrlCreatefnol +" ==> "+ log);
        }

        return response;
    }


	@Override
	public CommonResponse findFNOL(FnolRequest requestPayload) {
		CommonResponse response = new CommonResponse();
        ApiTransactionLog log = new ApiTransactionLog();
        log.setRequestTime(LocalDateTime.now());
        log.setEntryDate(new Date());
        log.setEndpoint(externalApiUrlGetfnol);

        try {
            // Validate request payload
            List<ErrorList> errors = validation.validateFnolRequest(requestPayload);
            if (!errors.isEmpty()) {
                response.setErrors(errors);
                response.setMessage("Failed");
                response.setResponse(Collections.emptyMap());
                response.setIsError(true);
                return response;
            }
            
            // Extract JWT token from request
            String jwtToken = authenticateUserCall();
            
            // Create headers with JWT token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Convert Map to JSON string
            FnolRequestDTO dto = mapToFnolRequestDTO(requestPayload);
            String requestBody = objectMapper.writeValueAsString(dto);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            log.setRequest(requestBody);
            logger.info(requestBody);
            // Send request to external API with JWT in Authorization header
            ResponseEntity<String> apiResponse = restTemplate.postForEntity(log.getEndpoint(), entity, String.class);
            log.setResponse(apiResponse.getBody());
            log.setStatus("SUCCESS");

            // Parse API response to Map
            response.setMessage("Data saved successfully");
            response.setIsError(false);
            response.setResponse(apiResponse.getBody());

        } catch (JsonProcessingException e) {
            log.setStatus("FAILURE");
            log.setErrorMessage(e.getMessage());
            response.setMessage("Failed to process JSON");
            response.setIsError(true);
        } catch (Exception e) {
            log.setStatus("FAILURE");
            log.setErrorMessage(e.getMessage());
            response.setMessage("Failed to save data");
            response.setIsError(true);
        } finally {
            log.setResponseTime(LocalDateTime.now());
            //apiTransactionLogRepo.save(log);
            logger.info(externalApiUrlGetfnol +" ==> "+ log);
        }

        return response;
	}

	@Override
    public CommonResponse getFnolStatus(ClaimTransactionRequest request) {
        CommonResponse response = new CommonResponse();
        ApiTransactionLog log = new ApiTransactionLog();
        log.setRequestTime(LocalDateTime.now());
        log.setEntryDate(new Date());
        log.setEndpoint(externalApiUrlGetFnolStatus);

        try {
            // Validate request
            List<ErrorList> errors = validation.validateClaimTransactionRequest(request);
            if (!errors.isEmpty()) {
                response.setErrors(errors);
                response.setMessage("Failed");
                response.setResponse(Collections.emptyMap());
                response.setIsError(true);
                return response;
            }
         // Extract JWT token from request
            String jwtToken = authenticateUserCall();
            
            // Create headers with JWT token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
                 
            // Convert Map to JSON string
            ClaimTransactionRequestDTO dto = mapToClaimTransactionRequestDTO(request);
            String requestBody = objectMapper.writeValueAsString(dto);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            log.setRequest(requestBody);
            logger.info(requestBody);
            // Send request to external API with JWT in Authorization header
            ResponseEntity<String> apiResponse = restTemplate.postForEntity(log.getEndpoint(), entity, String.class);
            log.setResponse(apiResponse.getBody());
            log.setStatus("SUCCESS");

            // Parse API response to Map
            response.setMessage("Data retrieved successfully");
            response.setIsError(false);
            response.setResponse(apiResponse.getBody());

        } catch (JsonProcessingException e) {
            log.setStatus("FAILURE");
            log.setErrorMessage(e.getMessage());
            response.setMessage("Failed to process JSON");
            response.setIsError(true);
        } catch (Exception e) {
            log.setStatus("FAILURE");
            log.setErrorMessage(e.getMessage());
            response.setMessage("Failed to retrieve data");
            response.setIsError(true);
        } finally {
            log.setResponseTime(LocalDateTime.now());
            //apiTransactionLogRepo.save(log);
            logger.info(externalApiUrlGetFnolStatus +" ==> "+ log);
        }

        return response;
    }

	@Override
	public CommonResponse authenticateUser(LoginRequest request) {
	    CommonResponse response = new CommonResponse();
	    ApiTransactionLog log = new ApiTransactionLog();
	    log.setRequestTime(LocalDateTime.now());
	    log.setEntryDate(new Date());
	    log.setEndpoint(externalApiUrlAuthenticate);

	    // Validate LoginRequest
	    List<ErrorList> errors = validation.validateLoginRequest(request);
	    if (!errors.isEmpty()) {
	        response.setErrors(errors);
	        response.setMessage("Validation failed");
	        response.setResponse(Collections.emptyMap());
	        response.setIsError(true);
	        return response;
	    }

	    try {
	        // Manually map the LoginRequest fields to the required format
	        Map<String, String> formattedRequest = Map.of(
	            "username", request.getLoginId(),
	            "password", request.getPassword()
	        );
	        
	        // Convert Map to JSON string
	        String requestBody = objectMapper.writeValueAsString(formattedRequest);
	        log.setRequest(requestBody);
	        logger.info(requestBody);
	        // Set the headers, including Content-Type as application/json
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);  // Fix the Content-Type to application/json

	        // Create HttpEntity containing headers and the request body
	        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

	        // Send request to external authentication API using RestTemplate
	        ResponseEntity<String> apiResponse = restTemplate.postForEntity(log.getEndpoint(), entity, String.class);
	        
	        // Log the response
	        log.setResponse(apiResponse.getBody());
	        log.setStatus("SUCCESS");

	        // Parse API response and prepare CommonResponse
	        response.setMessage("Authentication successful");
	        response.setIsError(false);
	        response.setResponse(apiResponse.getBody());

	    } catch (HttpClientErrorException e) {
	        // Handle 4xx errors, possibly including 415
	        log.setStatus("FAILURE");
	        log.setErrorMessage(e.getMessage());
	        response.setMessage("Authentication failed: " + e.getStatusCode());
	        response.setIsError(true);
	        response.setResponse(e.getResponseBodyAsString());
	    } catch (Exception e) {
	        log.setStatus("FAILURE");
	        log.setErrorMessage(e.getMessage());
	        response.setMessage("Authentication failed");
	        response.setIsError(true);
	        response.setResponse(e.getMessage());
	    } finally {
	        log.setResponseTime(LocalDateTime.now());
	        ////apiTransactionLogRepo.save(log);
	        logger.info(externalApiUrlAuthenticate +" ==> "+ log);
	    }

	    return response;
	}

	
	public SaveClaimRequestDTO mapToSaveClaimRequestDTO(SaveClaimRequest request) {
	    if (request == null) {
	        return null;
	    }
	    
	    SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    
	    SaveClaimRequestDTO dto = new SaveClaimRequestDTO();
	    
	    // Map simple fields
	    dto.setLanguageCode(request.getLanguageCode());
	    dto.setPolicyNo(request.getPolicyNo());
	    dto.setInsuredId(request.getInsuredId());
	    dto.setLossDate(isoDateFormat.format(request.getLossDate()));
	    dto.setIntimatedDate(isoDateFormat.format(request.getIntimatedDate()));
	    dto.setNatureOfLoss(request.getNatureOfLoss());
	    dto.setLossLocation(request.getLossLocation());
	    dto.setPoliceStation(request.getPoliceStation());
	    dto.setPoliceReportNo(request.getPoliceReportNo());
	    dto.setLossDescription(request.getLossDescription());
	    dto.setAtFault(request.getAtFault());
	    dto.setPolicyPeriod(request.getPolicyPeriod());
	    dto.setContactPersonPhoneNo(request.getContactPersonPhoneNo());
	    dto.setContactPersonPhoneCode(request.getContactPersonPhoneCode());
	    dto.setPolicyReferenceNo(request.getPolicyReferenceNo());
	    dto.setPolicyICReferenceNo(request.getPolicyICReferenceNo());
	    dto.setClaimRequestReference(request.getClaimRequestReference());
	    dto.setClaimCategory(request.getClaimCategory());
	    dto.setCreatedUser(request.getCreatedUser());
	    dto.setClaimType(request.getClaimType());
	    dto.setAccidentNumber(request.getAccidentNumber());
	    dto.setIsThirdPartyInvolved(request.getIsThirdPartyInvolved());

	    // Map complex fields
	    if (request.getRequestMetaData() != null) {
	        ClaimIntimationDTORequestMetaData metaData = new ClaimIntimationDTORequestMetaData();
	        metaData.setConsumerTrackingID(request.getRequestMetaData().getConsumerTrackingID());
	        metaData.setCurrentBranch(request.getRequestMetaData().getCurrentBranch());
	        metaData.setIpAddress(request.getRequestMetaData().getIpAddress());
	        metaData.setOriginBranch(request.getRequestMetaData().getOriginBranch());
	        metaData.setRequestData(request.getRequestMetaData().getRequestData());
	        metaData.setRequestGeneratedDateTime(isoDateFormat.format(new Date()));
	        metaData.setRequestId(request.getRequestMetaData().getRequestId());
	        metaData.setRequestOrigin(request.getRequestMetaData().getRequestOrigin());
	        metaData.setRequestReference(request.getRequestMetaData().getRequestReference());
	        metaData.setRequestedService(request.getRequestMetaData().getRequestedService());
	        metaData.setResponseData(request.getRequestMetaData().getResponseData());
	        metaData.setSourceCode(request.getRequestMetaData().getSourceCode());
	        metaData.setUserName(request.getRequestMetaData().getUserName());
	        dto.setRequestMetaData(metaData);
	    }

	    if (request.getDriver() != null) {
	    	ClaimIntimationDTODriver driverDTO = new ClaimIntimationDTODriver();
	        driverDTO.setEmiratesId(request.getDriver().getEmiratesId());
	        driverDTO.setLicenseNumber(request.getDriver().getLicenseNumber());
	        driverDTO.setDob(request.getDriver().getDob());
	        dto.setDriver(driverDTO);
	    }

	    if (request.getAttachmentDetails() != null) {
	    	ClaimIntimationDTOAttachmentDetails attachmentDetailsDTO = new ClaimIntimationDTOAttachmentDetails();
	        List<ClaimIntimationDTODocumentDetails> documentDetailsDTOList = new ArrayList<>();

	        for (ClaimIntimationDocumentDetails document : request.getAttachmentDetails().getDocumentDetails()) {
	        	ClaimIntimationDTODocumentDetails documentDetailsDTO = new ClaimIntimationDTODocumentDetails();
	            documentDetailsDTO.setDocumentData(document.getDocumentData());
	            documentDetailsDTO.setDocumentFormat(document.getDocumentFormat());
	            documentDetailsDTO.setDocumentId(document.getDocumentId());
	            documentDetailsDTO.setDocumentName(document.getDocumentName());
	            documentDetailsDTO.setDocumentRefNo(document.getDocumentRefNo());
	            documentDetailsDTO.setDocumentType(document.getDocumentType());
	            documentDetailsDTO.setDocumentURL(document.getDocumentURL());
	            
	            documentDetailsDTOList.add(documentDetailsDTO);
	        }

	        attachmentDetailsDTO.setDocumentDetails(documentDetailsDTOList);
	        dto.setAttachmentDetails(attachmentDetailsDTO);
	    }


	    if (request.getThirdPartyInfo() != null) {
	        List<ClaimIntimationDTOThirdPartyInfo> thirdPartyInfoList = new ArrayList<>();
	        for (ClaimIntimationThirdPartyInfo thirdParty : request.getThirdPartyInfo()) {
	        	ClaimIntimationDTOThirdPartyInfo thirdPartyInfoDTO = new ClaimIntimationDTOThirdPartyInfo();
	            thirdPartyInfoDTO.setTpDriverLiability(thirdParty.getTpDriverLiability());
	            thirdPartyInfoDTO.setTpDriverLicenceNo(thirdParty.getTpDriverLicenceNo());
	            thirdPartyInfoDTO.setTpDriverName(thirdParty.getTpDriverName());
	            thirdPartyInfoDTO.setTpDriverNationalityCode(thirdParty.getTpDriverNationalityCode());
	            thirdPartyInfoDTO.setTpDriverTrafficNo(thirdParty.getTpDriverTrafficNo());
	            thirdPartyInfoDTO.setTpMobileNumber(thirdParty.getTpMobileNumber());
	            thirdPartyInfoDTO.setTpVehicleCurrentInsurer(thirdParty.getTpVehicleCurrentInsurer());
	            thirdPartyInfoDTO.setTpVehicleMake(thirdParty.getTpVehicleMake());
	            thirdPartyInfoDTO.setTpVehicleMakeCode(thirdParty.getTpVehicleMakeCode());
	            thirdPartyInfoDTO.setTpVehicleModel(thirdParty.getTpVehicleModel());
	            thirdPartyInfoDTO.setTpVehicleModelCode(thirdParty.getTpVehicleModelCode());
	            thirdPartyInfoDTO.setTpVehiclePlateCode(thirdParty.getTpVehiclePlateCode());
	            thirdPartyInfoDTO.setTpVehiclePlateNo(thirdParty.getTpVehiclePlateNo());
	            thirdPartyInfoDTO.setTpVehiclePlateTypeCode(thirdParty.getTpVehiclePlateTypeCode());
	            thirdPartyInfoDTO.setThirdPartyReference(thirdParty.getThirdPartyReference());
	            thirdPartyInfoDTO.setThirdPartyType(thirdParty.getThirdPartyType());
	            thirdPartyInfoList.add(thirdPartyInfoDTO);
	        }
	        dto.setThirdPartyInfo(thirdPartyInfoList);
	    }

	    return dto;
	}
	
	public FnolRequestDTO mapToFnolRequestDTO(FnolRequest request) {
	    FnolRequestDTO dto = new FnolRequestDTO();

	    // Map simple fields
	    dto.setCustomerId(request.getCustomerId());
	    dto.setPolicyNo(request.getPolicyNo());
	    dto.setFnolNo(request.getFnolNo());
	    dto.setLossDate(request.getLossDate());

	    // Map RequestMetaData
	    if (request.getRequestMetaData() != null) {
	        FnolRequestDTOMetaData requestMetaDataDTO = new FnolRequestDTOMetaData();
	        requestMetaDataDTO.setConsumerTrackingID(request.getRequestMetaData().getConsumerTrackingID());
	        requestMetaDataDTO.setCurrentBranch(request.getRequestMetaData().getCurrentBranch());
	        requestMetaDataDTO.setIpAddress(request.getRequestMetaData().getIpAddress());
	        requestMetaDataDTO.setOriginBranch(request.getRequestMetaData().getOriginBranch());
	        requestMetaDataDTO.setRequestData(request.getRequestMetaData().getRequestData());
	        requestMetaDataDTO.setRequestGeneratedDateTime(request.getRequestMetaData().getRequestGeneratedDateTime());
	        requestMetaDataDTO.setRequestId(request.getRequestMetaData().getRequestId());
	        requestMetaDataDTO.setRequestOrigin(request.getRequestMetaData().getRequestOrigin());
	        requestMetaDataDTO.setRequestReference(request.getRequestMetaData().getRequestReference());
	        requestMetaDataDTO.setRequestedService(request.getRequestMetaData().getRequestedService());
	        requestMetaDataDTO.setResponseData(request.getRequestMetaData().getResponseData());
	        requestMetaDataDTO.setSourceCode(request.getRequestMetaData().getSourceCode());
	        requestMetaDataDTO.setUserName(request.getRequestMetaData().getUserName());

	        dto.setRequestMetaData(requestMetaDataDTO);
	    }

	    return dto;
	}

	public ClaimTransactionRequestDTO mapToClaimTransactionRequestDTO(ClaimTransactionRequest request) {
	    if (request == null) {
	        return null;
	    }
	    
	    ClaimTransactionRequestDTO dto = new ClaimTransactionRequestDTO();
	    
	    dto.setClaimsTpReferenceNo(request.getClaimsTpReferenceNo());
	    dto.setFnolNo(request.getFnolNo());
	    dto.setTpPolicyReferenceNo(request.getTpPolicyReferenceNo());
	    dto.setTransactionRefNo(request.getTransactionRefNo());

	    // Map RequestMetaData
	    if (request.getRequestMetaData() != null) {
	        ClaimTransactionRequestDTOMetaData metaDataDTO = new ClaimTransactionRequestDTOMetaData();
	        metaDataDTO.setConsumerTrackingID(request.getRequestMetaData().getConsumerTrackingID());
	        metaDataDTO.setCurrentBranch(request.getRequestMetaData().getCurrentBranch());
	        metaDataDTO.setIpAddress(request.getRequestMetaData().getIpAddress());
	        metaDataDTO.setOriginBranch(request.getRequestMetaData().getOriginBranch());
	        metaDataDTO.setRequestData(request.getRequestMetaData().getRequestData());
	        metaDataDTO.setRequestGeneratedDateTime(request.getRequestMetaData().getRequestGeneratedDateTime());
	        metaDataDTO.setRequestId(request.getRequestMetaData().getRequestId());
	        metaDataDTO.setRequestOrigin(request.getRequestMetaData().getRequestOrigin());
	        metaDataDTO.setRequestReference(request.getRequestMetaData().getRequestReference());
	        metaDataDTO.setRequestedService(request.getRequestMetaData().getRequestedService());
	        metaDataDTO.setResponseData(request.getRequestMetaData().getResponseData());
	        metaDataDTO.setSourceCode(request.getRequestMetaData().getSourceCode());
	        metaDataDTO.setUserName(request.getRequestMetaData().getUserName());
	        
	        dto.setRequestMetaData(metaDataDTO);
	    }
	    
	    return dto;
	}
	

	public String authenticateUserCall() {
	    ApiTransactionLog log = new ApiTransactionLog();
	    log.setRequestTime(LocalDateTime.now());
	    log.setEntryDate(new Date());
	    log.setEndpoint(externalApiUrlAuthenticate);

	    // Load username and password from properties
	    String username = apiusername;
	    String password = apipassword;

	    // Create the request map
	    Map<String, String> formattedRequest = Map.of(
	        "username", username,
	        "password", password
	    );

	    try {
	        // Convert Map to JSON string
	        String requestBody = objectMapper.writeValueAsString(formattedRequest);
	        log.setRequest(requestBody);
	        logger.info(requestBody);
	        // Set the headers, including Content-Type as application/json
	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);  // Fix the Content-Type to application/json

	        // Create HttpEntity containing headers and the request body
	        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
	        
	     // Configure SSL Trust Managers (if necessary)
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
	        
	        
	        // Send request to external authentication API
	        ResponseEntity<String> apiResponse = restTemplate.postForEntity(log.getEndpoint(), entity, String.class);
	        log.setResponse(apiResponse.getBody());
	        log.setStatus("SUCCESS");

	        // Extract JWT token from response
	        Map<String, String> responseMap = objectMapper.readValue(apiResponse.getBody(), Map.class);
	        String jwtToken = responseMap.get("jwt");

	        return jwtToken; // Return the JWT token

	    } catch (Exception e) {
	        log.setStatus("FAILURE");
	        log.setErrorMessage(e.getMessage());
	    } finally {
	        log.setResponseTime(LocalDateTime.now());
	        //apiTransactionLogRepo.save(log);
	        logger.info(externalApiUrlAuthenticate +" ==> "+ log);
	    }

	    return null; // Return null if authentication fails
	}
	
	public String saveOrUpdateClaimIntimation(SaveClaimRequest saveClaimRequestDTO) {
        try {
            // Check if a record already exists for the given policyNo
            Optional<ClaimIntimationDetails> existingRecord = claimIntimationDetailsRepository.findById(
                    new ClaimIntimationDetailsId(saveClaimRequestDTO.getPolicyNo(),saveClaimRequestDTO.getPoliceReportNo())
            );

            ClaimIntimationDetails claimIntimationDetails;

            if (existingRecord.isPresent()) {
                // Update the existing record
                claimIntimationDetails = existingRecord.get();
                logger.info("Updating existing record for policyNo: " + saveClaimRequestDTO.getPolicyNo());
            } else {
                // Insert new data
                claimIntimationDetails = new ClaimIntimationDetails();
                claimIntimationDetails.setPolicyNo(saveClaimRequestDTO.getPolicyNo());
                logger.info("Inserting new record for policyNo: " + saveClaimRequestDTO.getPolicyNo());
            }

            // Map the fields from SaveClaimRequestDTO to ClaimIntimationDetails entity
            claimIntimationDetails.setRequestOrigin(saveClaimRequestDTO.getRequestMetaData().getRequestOrigin());
            claimIntimationDetails.setCurrentBranch(saveClaimRequestDTO.getRequestMetaData().getCurrentBranch());
            claimIntimationDetails.setOriginBranch(saveClaimRequestDTO.getRequestMetaData().getOriginBranch());
            claimIntimationDetails.setUserName(saveClaimRequestDTO.getRequestMetaData().getUserName());
            claimIntimationDetails.setIpAddress(saveClaimRequestDTO.getRequestMetaData().getIpAddress());

            claimIntimationDetails.setRequestGeneratedDateTime(saveClaimRequestDTO.getRequestMetaData().getRequestGeneratedDateTime());
  
            claimIntimationDetails.setConsumerTrackingId(saveClaimRequestDTO.getRequestMetaData().getConsumerTrackingID());
            claimIntimationDetails.setLanguageCode(saveClaimRequestDTO.getLanguageCode());
            claimIntimationDetails.setInsuredId(saveClaimRequestDTO.getInsuredId());

            claimIntimationDetails.setLossDate(saveClaimRequestDTO.getLossDate());
            claimIntimationDetails.setIntimatedDate(saveClaimRequestDTO.getIntimatedDate());

            claimIntimationDetails.setLossLocation(saveClaimRequestDTO.getLossLocation());
            claimIntimationDetails.setNatureOfLoss(saveClaimRequestDTO.getNatureOfLoss());
            claimIntimationDetails.setPoliceStation(saveClaimRequestDTO.getPoliceStation());
            claimIntimationDetails.setPoliceReportNo(saveClaimRequestDTO.getPoliceReportNo());
            claimIntimationDetails.setLossDescription(saveClaimRequestDTO.getLossDescription());
            claimIntimationDetails.setAtFault(saveClaimRequestDTO.getAtFault());

            // Save or update the record in the database
            claimIntimationDetailsRepository.save(claimIntimationDetails);
            return "Success: Data saved/updated for policyNo: " + saveClaimRequestDTO.getPolicyNo();

        } catch (Exception e) {
            logger.severe("Error saving/updating data for policyNo: " + saveClaimRequestDTO.getPolicyNo() + " - " + e.getMessage());
            return "Error: Unable to save/update data for policyNo: " + saveClaimRequestDTO.getPolicyNo();
        }
    }

	@Override
	public CommonResponse getClaimByPolicy(GetClaimRequest req) {
	    CommonResponse response = new CommonResponse();
	    try {
	        // Fetch data by policy number
	        Optional<ClaimIntimationDetails> dataOptional = claimIntimationDetailsRepository.findByPolicyNoAndPoliceReportNo(req.getPolicyNo(),req.getPoliceReportNo());
	        
	        if (dataOptional.isPresent()) {
	            ClaimIntimationDetails data = dataOptional.get();
	            ClaimIntimationResponse formattedResponse = mapToClaimIntimationResponse(data);
	            
	            response.setResponse(formattedResponse);
	            response.setMessage("Data fetched successfully");
	            response.setIsError(false);
	        } else {
	            response.setMessage("No data found for policy number: " + req.getPolicyNo());
	            response.setIsError(true);
	        }
	    } catch (Exception e) {
	        response.setMessage("Failed to fetch data");
	        response.setIsError(true);
	    }
	    return response;
	}

	@Override
	public CommonResponse getAllClaims() {
	    CommonResponse response = new CommonResponse();
	    try {
	        List<ClaimIntimationDetails> dataList = claimIntimationDetailsRepository.findAllByOrderByRequestGeneratedDateTimeDesc();
	        List<ClaimIntimationResponse> responseList = dataList.stream()
	                .map(this::mapToClaimIntimationResponse)
	                .collect(Collectors.toList());
	        
	        response.setResponse(responseList);
	        response.setMessage("Data fetched successfully");
	        response.setIsError(false);
	    } catch (Exception e) {
	        response.setMessage("Failed to fetch data");
	        response.setIsError(true);
	    }
	    return response;
	}

	private ClaimIntimationResponse mapToClaimIntimationResponse(ClaimIntimationDetails data) {
	    ClaimIntimationResponse response = new ClaimIntimationResponse();

	    try {
	        // Set fields from ClaimIntimationDetails entity to ClaimIntimationResponse
	        response.setLanguageCode(data.getLanguageCode());
	        response.setPolicyNo(data.getPolicyNo());
	        response.setFnolNo(data.getFnolNo());
	        response.setInsuredId(data.getInsuredId());

			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

			// Convert and set the Loss Date
			Date lossDate = data.getLossDate();
			if (lossDate != null) {
				response.setLossDate(dateFormat.format(lossDate));
			}

			// Convert and set the Intimated Date
			Date intimatedDate = data.getIntimatedDate();
			if (intimatedDate != null) {
				response.setIntimatedDate(dateFormat.format(intimatedDate));
			}
//	        response.setLossDate(data.getLossDate());
//	        response.setIntimatedDate(data.getIntimatedDate());
	        response.setNatureOfLoss(data.getNatureOfLoss());
	        response.setLossLocation(data.getLossLocation());
	        response.setPoliceStation(data.getPoliceStation());
	        response.setPoliceReportNo(data.getPoliceReportNo());
	        response.setLossDescription(data.getLossDescription());
	        response.setAtFault(data.getAtFault());
	        response.setPolicyPeriod("Not available");  // Map other fields as needed

	        // Set nested objects like requestMetaData, driver, attachmentDetails
	        response.setRequestMetaData(mapToRequestMetaData(data));
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return response;
	}

	// Example of mapping nested objects (you need to complete as per your structure)
	private ClaimIntimationRequestMetaData mapToRequestMetaData(ClaimIntimationDetails data) {
	    ClaimIntimationRequestMetaData metaData = new ClaimIntimationRequestMetaData();

	    try {
	        // Map fields from ClaimIntimationDetails to ClaimIntimationRequestMetaData
	        metaData.setConsumerTrackingID(data.getConsumerTrackingId());
	        metaData.setCurrentBranch(data.getCurrentBranch());
	        metaData.setIpAddress(data.getIpAddress());
	        metaData.setOriginBranch(data.getOriginBranch()); 

	        // Format requestGeneratedDateTime similarly
	        if (data.getRequestGeneratedDateTime() != null) {
	            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	            metaData.setRequestGeneratedDateTime(data.getRequestGeneratedDateTime());
	        }
	        
	        metaData.setRequestOrigin(data.getRequestOrigin());
	    } catch (Exception e) {
	        e.printStackTrace();
	    } 

	    return metaData;
	}


	@Override
	public CommonResponse getClaimListing(ClaimListRequest requestPayload) {
		CommonResponse response = new CommonResponse();
        ApiTransactionLog log = new ApiTransactionLog();
        log.setRequestTime(LocalDateTime.now());
        log.setEntryDate(new Date());
        log.setEndpoint(externalApiUrlClaimListing);

        try {
            // Extract JWT token from request
            String jwtToken = authenticateUserCall();
            
            // Create headers with JWT token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Convert requestPayload to JSON and add headers
            ClaimListRequestDTO dto = mapToClaimListingDTO(requestPayload);
            
            String requestBody = objectMapper.writeValueAsString(dto);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            log.setRequest(requestBody);
            logger.info(requestBody);
            // Configure SSL Trust Managers (if necessary)
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            // Send request to external API with JWT in Authorization header
            ResponseEntity<String> apiResponse = restTemplate.postForEntity(log.getEndpoint(), entity, String.class);
            log.setResponse(apiResponse.getBody());
            log.setStatus("SUCCESS");

            // Parse the raw response into ExternalApiResponse object
            ClaimListResponse externalApiResponse = objectMapper.readValue(apiResponse.getBody(), ClaimListResponse.class);

            if ("true".equalsIgnoreCase(externalApiResponse.getHasError())) {
                // Create custom error response
                List<ErrorResponse> errorList = new ArrayList<>();
//                for (ErrorDetail error : externalApiResponse.getData().getErrorDetailsList()) {
//                    errorList.add(new ErrorResponse(error.getErrorCode(), error.getErrorField(), error.getErrorDescription()));
//                }
                response.setErrors(errorList);
                response.setMessage(externalApiResponse.getMessage());
                response.setResponse(externalApiResponse);
                response.setIsError(true);
            } else {
                response.setMessage("Data saved successfully");
                response.setIsError(false);
                response.setResponse(externalApiResponse);
            }

        } catch (Exception e) {
            log.setStatus("FAILURE");
            log.setErrorMessage(e.getMessage());
            response.setMessage("Failed to save data");
            response.setIsError(true);
            response.setErrors(Collections.singletonList(new ErrorResponse("100", "API Error", e.getMessage()))); // API Error error
        } finally {
            log.setResponseTime(LocalDateTime.now());
            //apiTransactionLogRepo.save(log);
            logger.info(externalApiUrlClaimListing +" ==> "+ log);
        }

        return response;
	}


	private ClaimListRequestDTO mapToClaimListingDTO(ClaimListRequest requestPayload) {
	    if (requestPayload == null) {
	        return null;
	    }

	    ClaimIntimationDTORequestMetaData dtoRequestMetaData = new ClaimIntimationDTORequestMetaData();

	    try {
	        ClaimIntimationRequestMetaData requestMetaData = requestPayload.getRequestMetaData();

	        if (requestMetaData != null) {
	            try {
	                dtoRequestMetaData.setConsumerTrackingID(requestMetaData.getConsumerTrackingID());
	                dtoRequestMetaData.setCurrentBranch(requestMetaData.getCurrentBranch());
	                dtoRequestMetaData.setIpAddress(requestMetaData.getIpAddress());
	                dtoRequestMetaData.setOriginBranch(requestMetaData.getOriginBranch());
	                dtoRequestMetaData.setRequestData(requestMetaData.getRequestData());
	                
	                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	                dtoRequestMetaData.setRequestGeneratedDateTime(
	                    requestMetaData.getRequestGeneratedDateTime() != null 
	                        ? dateFormat.format(requestMetaData.getRequestGeneratedDateTime()) 
	                        : null
	                );
	                
	                dtoRequestMetaData.setRequestId(requestMetaData.getRequestId());
	                dtoRequestMetaData.setRequestOrigin(requestMetaData.getRequestOrigin());
	                dtoRequestMetaData.setRequestReference(requestMetaData.getRequestReference());
	                dtoRequestMetaData.setRequestedService(requestMetaData.getRequestedService());
	                dtoRequestMetaData.setResponseData(requestMetaData.getResponseData());
	                dtoRequestMetaData.setSourceCode(requestMetaData.getSourceCode());
	                dtoRequestMetaData.setUserName(requestMetaData.getUserName());
	            } catch (Exception e) {
	                System.err.println("Error mapping requestMetaData: " + e.getMessage());
	                e.printStackTrace();
	            }
	        }
	    } catch (Exception e) {
	        System.err.println("Error accessing requestMetaData from requestPayload: " + e.getMessage());
	        e.printStackTrace();
	    }

	    String claimNotificationFromDate = null;
	    String claimNotificationToDate = null;
	    String claimLossFromDate = null;
	    String claimLossToDate = null;

	    try {
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	        claimNotificationFromDate = requestPayload.getClaimNotificationFromDate() != null 
	            ? dateFormat.format(requestPayload.getClaimNotificationFromDate()) : "";

	        claimNotificationToDate = requestPayload.getClaimNotificationToDate() != null 
	            ? dateFormat.format(requestPayload.getClaimNotificationToDate()) : "";

	        claimLossFromDate = requestPayload.getClaimLossFromDate() != null 
	            ? dateFormat.format(requestPayload.getClaimLossFromDate()) : "";

	        claimLossToDate = requestPayload.getClaimLossToDate() != null 
	            ? dateFormat.format(requestPayload.getClaimLossToDate()) : "";

	    } catch (Exception e) {
	        System.err.println("Error converting date fields: " + e.getMessage());
	        e.printStackTrace();
	    }

	    return new ClaimListRequestDTO(
	        requestPayload.getLobCode(),
	        requestPayload.getProdCode(),
	        requestPayload.getPolicyNumber(),
	        requestPayload.getClaimNotificationNumber(),
	        requestPayload.getCreatedBy(),
	        claimNotificationFromDate,
	        claimNotificationToDate,
	        claimLossFromDate,
	        claimLossToDate,
	        dtoRequestMetaData
	    );
	}


	@Override
	public CommonResponse saveSpareParts(SaveSparePartsDTO request) {

	    CommonResponse response = new CommonResponse();
	    ApiTransactionLog log = new ApiTransactionLog();
	    log.setRequestTime(LocalDateTime.now());
	    log.setEntryDate(new Date());
	    log.setEndpoint(externalApiUrlSaveSpareparts);

	    try {
	        // Retrieve data from repositories
	    	GarageWorkOrder workOrder = garageWorkOrderRepo.findByClaimNoAndQuotationNo(request.getClaimNo(),request.getQuotationNo());
	    	LoginMaster loginMaster = loginRepo.findByLoginId(workOrder.getGarageId());
	        List<DamageSectionDetails> damageDetails = damageSectionDetailsRepo.findByClaimNoAndQuotationNo(request.getClaimNo(),request.getQuotationNo());
	        SparePartsSaveDetails partsSaveDetails = SparePartsSaveDetailsRepo.findByClaimNoAndGarageCode(request.getClaimNo(),loginMaster.getCoreAppCode());
	        if (workOrder == null || damageDetails.isEmpty()) {
	            response.setMessage("No data found for the provided claim or work order number");
	            response.setIsError(true);
	            response.setErrors(Collections.singletonList(new ErrorResponse("404", "Data Not Found", "Data not found for the provided claim or work order number")));
	            return response;
	        }else if("Y".equalsIgnoreCase(partsSaveDetails.getSavedStatus())||"ESB".equalsIgnoreCase(partsSaveDetails.getSavedStatus())) {
	        	//response.setMessage("No data found for the provided claim or work order number");
	        	System.out.println("saved  status ==>"+partsSaveDetails.getSavedStatus());
	            response.setIsError(true);
	            ClaimListResponse externalRes = new ClaimListResponse();
	            externalRes.setMessage("spareparts details already saved");
	            response.setResponse(externalRes);
	            //response.setErrors(Collections.singletonList(new ErrorResponse("404", "Data Not Found", "Data not found for the provided claim or work order number")));
	            return response;
	        }

	        // Map entities to request DTO
	        //SaveSparePartsRequest dto = mapToSaveSparePartsRequest(workOrder, damageDetails);
	        SaveSparePartsRequest dto = mapToSaveSparePartsRequestV1(partsSaveDetails, damageDetails);
	        
	        // Authenticate and retrieve JWT token
	        String jwtToken = authenticateUserCall();

	        // Create headers and add JWT token
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + jwtToken);
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        // Convert DTO to JSON for request body and add headers
	        String requestBody = objectMapper.writeValueAsString(dto);
	        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
	        log.setRequest(requestBody);
	        logger.info(requestBody);
	     // Configure SSL Trust Managers (if necessary)
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
	        
	        // Send request to external API
	        ResponseEntity<String> apiResponse = restTemplate.postForEntity(log.getEndpoint(), entity, String.class);
	        log.setResponse(apiResponse.getBody());
	        log.setStatus("SUCCESS");

	        // Parse response into ExternalApiResponse object
	        ClaimListResponse externalApiResponse = objectMapper.readValue(apiResponse.getBody(), ClaimListResponse.class);

	        // Process response based on external API success status
	        if ("true".equalsIgnoreCase(externalApiResponse.getHasError())) {
	            response.setMessage(externalApiResponse.getMessage());
	            //response.setErrors(externalApiResponse.getErrors());
	            response.setResponse(externalApiResponse);
	            response.setIsError(true);
	            partsSaveDetails.setSavedStatus("GPC");
	            SparePartsSaveDetailsRepo.save(partsSaveDetails);
	        } else {
	            response.setMessage("Data saved successfully");
	            response.setIsError(false);
	            response.setResponse(externalApiResponse);
	            partsSaveDetails.setSavedStatus("ESB");
	            partsSaveDetails.setClgwSgsId(externalApiResponse.getClgwSgsId());
	            SparePartsSaveDetailsRepo.save(partsSaveDetails);
	        }

	    } catch (Exception e) {
	        log.setStatus("FAILURE");
	        log.setErrorMessage(e.getMessage());
	        response.setMessage("Failed to save data");
	        response.setIsError(true);
	        response.setErrors(Collections.singletonList(new ErrorResponse("100", "API Error", e.getMessage())));
	        //partsSaveDetails.setSavedStatus("N");
	    } finally {
	        log.setResponseTime(LocalDateTime.now());
	        if(StringUtils.isNotBlank(log.getRequest())){
	        	//apiTransactionLogRepo.save(log);
	        	logger.info(externalApiUrlSaveSpareparts +" ==> "+ log);
	        }
	    }

	    return response;
	}
	
	
	private SaveSparePartsRequest mapToSaveSparePartsRequestV1(SparePartsSaveDetails partsSaveDetails,
			List<DamageSectionDetails> damageDetails) {
		SaveSparePartsRequest request = new SaveSparePartsRequest();

	    try {
	    	SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    	
			// Map fields from GarageWorkOrder to SaveSparePartsRequest
			request.setWorkOrderType(partsSaveDetails.getWorkOrderType());
			request.setWorkOrderNo(partsSaveDetails.getWorkOrderNo());
			request.setWorkOrderDate(isoDateFormat.format(partsSaveDetails.getWorkOrderDate())); 
			request.setAccForSettlementType(partsSaveDetails.getAccountSettlementType());
//			request.setAccForSettlement(partsSaveDetails.getAccountSettlementName());
			request.setAccForSettlement("");
			request.setSparePartsDealer(partsSaveDetails.getSparePartsDealer());
			request.setGarageCode(partsSaveDetails.getGarageCode());
//			request.setGarageQuotationNo(partsSaveDetails.getQuotationNo());
			request.setGarageQuotationNo(partsSaveDetails.getWorkOrderNo());
			request.setDeliveryDate(isoDateFormat.format(partsSaveDetails.getDeliveryDate()));
			request.setDeliveredTo(partsSaveDetails.getDeliveredTo());
			request.setDeliveredId(partsSaveDetails.getGarageCode());
			request.setSubrogation(partsSaveDetails.getSubrogation());
			request.setJointOrder(partsSaveDetails.getJointOrder());
			//request.setTotalLoss(partsSaveDetails.getTotalLoss().toString());
			request.setTotalLoss("N");
			//request.setTotalLossType(partsSaveDetails.getTotalLossType());
			request.setTotalLossType("");
			request.setRemarks(partsSaveDetails.getRemarks());
//			request.setClaimNo(partsSaveDetails.getClaimNo());
			request.setClaimNo(partsSaveDetails.getFileNo());
			request.setLpoId(partsSaveDetails.getLpoId());
			request.setVehId(partsSaveDetails.getVehId());
			request.setClcpId(partsSaveDetails.getClcpId());

			List<VehicleDamageDetailRequest> vehicleDamageDetails = new ArrayList<>();

			for (DamageSectionDetails detail : damageDetails) {
			    VehicleDamageDetailRequest damageRequest = new VehicleDamageDetailRequest();
			    String directionCode = dropDownServiceImpl.getItemCodeByItemValue(detail.getDamageDirection(),"DAMAGE_DIRECTION");
			    String partCode = dropDownServiceImpl.getbodyPartCodeByValue(detail.getDamagePart());
			    BigDecimal total = BigDecimal.ZERO;
			    damageRequest.setDamageDirection(directionCode);
			    damageRequest.setPartyType(partCode);
			    //damageRequest.setReplaceOrRepair(detail.getRepairReplace());
			    damageRequest.setReplaceOrRepair("");
			    //damageRequest.setNoUnits(detail.getNoOfParts());
			    damageRequest.setNoUnits("0");
			    if("REPLACE".equalsIgnoreCase(detail.getRepairReplace())) {
			    	damageRequest.setUnitPrice(detail.getGaragePrice()!=null?detail.getGaragePrice().toString():"");
			    	BigDecimal unitPrice = detail.getGaragePrice() != null ? detail.getGaragePrice() : BigDecimal.ZERO;
				    int noOfParts = detail.getNoOfParts() > 0 ? detail.getNoOfParts() : 0;
				    BigDecimal replacementCharge = detail.getReplaceCost() != null ? detail.getReplaceCost() : BigDecimal.ZERO;

				    try {
				        total = unitPrice.multiply(BigDecimal.valueOf(noOfParts)).add(replacementCharge);
				    } catch (Exception e) {
				        // Log the error and default total to zero
				        System.err.println("Error calculating total: " + e.getMessage());
				        total = BigDecimal.ZERO;
				    }

				    //damageRequest.setTotal(total);
				    damageRequest.setTotal("");
			    }else {
			    	damageRequest.setUnitPrice("");
			    	 BigDecimal replacementCharge = detail.getReplaceCost() != null ? detail.getReplaceCost() : BigDecimal.ZERO;
			    	total = replacementCharge;
			    	damageRequest.setAsPerInvoice("true".equalsIgnoreCase(detail.getAsPerInvoice())?"Y" : "N");
			    	damageRequest.setDeductiblePer(detail.getLabourCostDeductPercentage() != null ? detail.getLabourCostDeductPercentage().toString() : "0");
			    	damageRequest.setDeductibleAmount(detail.getLabourCostDeduct() != null ? detail.getLabourCostDeduct().toString() : "0");
			    	//damageRequest.setBeforeDeduction(replacementCharge!=null ? replacementCharge.toString():"0");
			    	damageRequest.setBeforeDeduction("");
			    	BigDecimal dedudct = detail.getLabourCostDeduct() != null ? detail.getLabourCostDeduct() : BigDecimal.ZERO;
			    	total = replacementCharge.subtract(dedudct);
			    	damageRequest.setTotal("");
			    }
			    
			    
			    damageRequest.setReplacementCharge(detail.getReplaceCost()!=null ?detail.getReplaceCost().toString():"0");
			    

			    vehicleDamageDetails.add(damageRequest);
			}

			request.setVehicleDamageDetails(vehicleDamageDetails);


			// Populate metadata (assumed to be populated elsewhere in your code)
			SaveSparePartsRequestMetaData metaData = new SaveSparePartsRequestMetaData();
			metaData.setRequestOrigin("API"); 
			metaData.setCurrentBranch("2222");
			metaData.setOriginBranch("2222");
			metaData.setUserName("09988877772");
			//metaData.setIpAddress(workOrder.getIpAddress());
			metaData.setRequestGeneratedDateTime(isoDateFormat.format(new Date()));
			//metaData.setConsumerTrackingID(UUID.randomUUID().toString()); // example, replace as necessary
			request.setRequestMetaData(metaData);
			
//			request.setReplacementCost(partsSaveDetails.getReplacementCost() != null ? partsSaveDetails.getReplacementCost().toString() : "0");
			request.setReplacementCost("");
			request.setReplacementCostDeductible(partsSaveDetails.getReplacementCostDeductible() != null ? partsSaveDetails.getReplacementCostDeductible().toString() : "0");
			request.setSparePartDepreciation(partsSaveDetails.getSparePartDepreciation() != null ? partsSaveDetails.getSparePartDepreciation().toString() : "0");
			request.setDiscountonSpareParts(partsSaveDetails.getDiscountOnSpareParts() != null ? partsSaveDetails.getDiscountOnSpareParts().toString() : "0");
//			request.setTotalAmountReplacement(partsSaveDetails.getTotalAmountReplacement() != null ? partsSaveDetails.getTotalAmountReplacement().toString() : "0");
			request.setTotalAmountReplacement("");
//			request.setRepairLabour(partsSaveDetails.getRepairLabour() != null ? partsSaveDetails.getRepairLabour().toString() : "0");
			request.setRepairLabour("");
			request.setRepairLabourDeductible(partsSaveDetails.getRepairLabourDeductible() != null ? partsSaveDetails.getRepairLabourDeductible().toString() : "0");
			request.setRepairLabourDiscountAmount(partsSaveDetails.getRepairLabourDiscountAmount() != null ? partsSaveDetails.getRepairLabourDiscountAmount().toString() : "0");
			request.setTotalAmountRepairLabour("");
			//request.setTotalAmountRepairLabour(partsSaveDetails.getTotalAmountRepairLabour() != null ? partsSaveDetails.getTotalAmountRepairLabour().toString() : "0");
			request.setNetAmount(partsSaveDetails.getNetAmount() != null ? partsSaveDetails.getNetAmount().toString() : "0");
			request.setUnkownAccidentDeduction(partsSaveDetails.getUnknownAccidentDeduction() != null ? partsSaveDetails.getUnknownAccidentDeduction().toString() : "0");
			request.setAmounttobeRecovered(partsSaveDetails.getAmountToBeRecovered() != null ? partsSaveDetails.getAmountToBeRecovered().toString() : "0");
//			request.setTotalafterDeductions(partsSaveDetails.getTotalAfterDeductions() != null ? partsSaveDetails.getTotalAfterDeductions().toString() : "0");
			request.setTotalafterDeductions("0");
			request.setVatRatePer(partsSaveDetails.getVatRatePercentage() != null ? partsSaveDetails.getVatRatePercentage().toString() : "0");
			request.setVatRate(partsSaveDetails.getVatRate() != null ? partsSaveDetails.getVatRate().toString() : "0");
			request.setVatAmount(partsSaveDetails.getVatAmount() != null ? partsSaveDetails.getVatAmount().toString() : "0");
			request.setTotalWithVAT(partsSaveDetails.getTotalWithVat() != null ? partsSaveDetails.getTotalWithVat().toString() : "0");

			
		} catch (Exception e) {
			e.printStackTrace();
		}

	    return request;
	}


	private SaveSparePartsRequest mapToSaveSparePartsRequest(GarageWorkOrder workOrder, List<DamageSectionDetails> damageDetails) {
	    SaveSparePartsRequest request = new SaveSparePartsRequest();

	    try {
	    	SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	    	
			// Map fields from GarageWorkOrder to SaveSparePartsRequest
			request.setWorkOrderType(workOrder.getWorkOrderType());
			request.setWorkOrderNo(workOrder.getWorkOrderNo());
			request.setWorkOrderDate(isoDateFormat.format(workOrder.getWorkOrderDate())); 
			request.setAccForSettlementType(workOrder.getSettlementType());
			request.setAccForSettlement(workOrder.getSettlementToDesc());
			request.setSparePartsDealer(workOrder.getSparepartsDealerId());
			request.setGarageCode(workOrder.getGarageId());
			request.setGarageQuotationNo(workOrder.getQuotationNo());
			request.setDeliveryDate(isoDateFormat.format(workOrder.getDeliveryDate()));
			request.setDeliveredTo(workOrder.getGarageName());
			request.setSubrogation(workOrder.getSubrogationYn());
			request.setJointOrder(workOrder.getJointOrderYn());
			request.setTotalLoss(workOrder.getTotalLoss().toString());
			request.setTotalLossType(workOrder.getLossType());
			request.setRemarks(workOrder.getRemarks());
			
//			request.setReplacementCost(workOrder.getReplacementCost().toString());
//			request.setReplacementCostDeductible(workOrder.getReplacementCostDeductible().toString());
//			request.setSparePartDepreciation(workOrder.getSparePartDepreciation().toString());
//			request.setDiscountonSpareParts(workOrder.getDiscountonSpareParts().toString());
//			request.setTotalAmountReplacement(workOrder.getTotalAmountReplacement().toString());
//			request.setRepairLabour(workOrder.getRepairLabour().toString());
//			request.setRepairLabourDeductible(workOrder.getRepairLabourDeductible().toString());
//			request.setRepairLabourDiscountAmount(workOrder.getRepairLabourDiscountAmount().toString());
//			request.setTotalAmountRepairLabour(workOrder.getTotalAmountRepairLabour().toString());
//			request.setNetAmount(workOrder.getNetAmount().toString());
//			request.setUnkownAccidentDeduction(workOrder.getUnkownAccidentDeduction().toString());
//			request.setAmounttobeRecovered(workOrder.getAmounttobeRecovered().toString());
//			request.setTotalafterDeductions(workOrder.getTotalafterDeductions().toString());
//			request.setVatRatePer(workOrder.getVatRatePer().toString());
//			request.setVatRate(workOrder.getVatRate().toString());
//			request.setVatAmount(workOrder.getVatAmount().toString());
//			request.setTotalWithVAT(workOrder.getTotalWithVAT().toString());

			// Map DamageSectionDetails list to vehicleDamageDetails
			List<VehicleDamageDetailRequest> vehicleDamageDetails = damageDetails.stream().map(detail -> {
			    VehicleDamageDetailRequest damageRequest = new VehicleDamageDetailRequest();
			    damageRequest.setDamageDirection(detail.getDamageDirection());
			    damageRequest.setPartyType(detail.getDamagePart());
//			    damageRequest.setReplaceOrRepair(detail.getRepairReplace());
//			    damageRequest.setNoUnits(detail.getNoOfParts());
//			    damageRequest.setUnitPrice(detail.getGaragePrice());
//			    damageRequest.setReplacementCharge(detail.getReplaceCost());
//			    damageRequest.setTotal(detail.getTotPrice());
			    return damageRequest;
			}).collect(Collectors.toList());
			request.setVehicleDamageDetails(vehicleDamageDetails);

			// Populate metadata (assumed to be populated elsewhere in your code)
			SaveSparePartsRequestMetaData metaData = new SaveSparePartsRequestMetaData();
			metaData.setRequestOrigin("API"); 
			metaData.setCurrentBranch("2222");
			metaData.setOriginBranch("2222");
			metaData.setUserName("09988877772");
			//metaData.setIpAddress(workOrder.getIpAddress());
			metaData.setRequestGeneratedDateTime(isoDateFormat.format(new Date()));
			//metaData.setConsumerTrackingID(UUID.randomUUID().toString()); // example, replace as necessary
			request.setRequestMetaData(metaData);
		} catch (Exception e) {
			e.printStackTrace();
		}

	    return request;
	}


	/**
	 * Configures SSL Trust Managers to trust all certificates.
	 * This should only be used in a development or testing environment.
	 */
	private void configureSSLTrustManagers() throws Exception {
	    TrustManager[] trustAllCerts = new TrustManager[]{
	        new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }

	            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
	            }

	            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
	            }
	        }
	    };

	    SSLContext sc = SSLContext.getInstance("SSL");
	    sc.init(null, trustAllCerts, new java.security.SecureRandom());
	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	    HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
	}


	@Override
	public CommonResponse getSavedSpareParts(SaveSparePartsDTO requestPayload) {
		CommonResponse comResponse = new CommonResponse(); 
        try {
        	List<SparePartsSaveDetails> spareSavedList = SparePartsSaveDetailsRepo.findAll();
			
			if(spareSavedList != null && spareSavedList.size()>0) {
		    	List<GetAllQuoteResponse> res = new ArrayList<>();
				for(SparePartsSaveDetails spareSaved : spareSavedList ) {
					 GetAllQuoteResponse response = new GetAllQuoteResponse();
			         response.setClaimNo(spareSaved.getClaimNo());
			         response.setWorkOrderNo(spareSaved.getWorkOrderNo());
			         response.setWorkOrderType(spareSaved.getWorkOrderType());
			         response.setWorkOrderDate(spareSaved.getWorkOrderDate());
			         response.setSettlementType(spareSaved.getAccountSettlementType());
			         response.setSettlementTo(spareSaved.getAccountSettlementName());
			         response.setGarageId(spareSaved.getGarageCode().toString());
			         response.setQuotationNo(spareSaved.getQuotationNo());
			         response.setDeliveryDate(spareSaved.getDeliveryDate());
			         response.setJointOrderYn(spareSaved.getJointOrder());
			         response.setSubrogationYn(spareSaved.getSubrogation());
			         response.setTotalLoss(spareSaved.getTotalLoss().toString());
			         response.setLossType(spareSaved.getTotalLossType());
			         response.setRemarks(spareSaved.getRemarks());
			         response.setSavedStatus(spareSaved.getSavedStatus());
			         response.setSparepartsDealerId(Optional.ofNullable(spareSaved.getSparePartsDealer()).map(String ::valueOf).orElse(""));		         
			         
						response.setReplacementCost(
								spareSaved.getReplacementCost() != null ? spareSaved.getReplacementCost().toString()
										: "0.00");
						response.setReplacementCostDeductible(spareSaved.getReplacementCostDeductible() != null
								? spareSaved.getReplacementCostDeductible().toString()
								: "0.00");
						response.setSparePartDepreciation(spareSaved.getSparePartDepreciation() != null
								? spareSaved.getSparePartDepreciation().toString()
								: "0.00");
						response.setDiscountOnSpareParts(spareSaved.getDiscountOnSpareParts() != null
								? spareSaved.getDiscountOnSpareParts().toString()
								: "0.00");
						response.setTotalAmountReplacement(spareSaved.getTotalAmountReplacement() != null
								? spareSaved.getTotalAmountReplacement().toString()
								: "0.00");
						response.setRepairLabour(
								spareSaved.getRepairLabour() != null ? spareSaved.getRepairLabour().toString()
										: "0.00");
						response.setRepairLabourDeductible(spareSaved.getRepairLabourDeductible() != null
								? spareSaved.getRepairLabourDeductible().toString()
								: "0.00");
						response.setRepairLabourDiscountAmount(spareSaved.getRepairLabourDiscountAmount() != null
								? spareSaved.getRepairLabourDiscountAmount().toString()
								: "0.00");
						response.setTotalAmountRepairLabour(spareSaved.getTotalAmountRepairLabour() != null
								? spareSaved.getTotalAmountRepairLabour().toString()
								: "0.00");
						response.setNetAmount(
								spareSaved.getNetAmount() != null ? spareSaved.getNetAmount().toString() : "0.00");
						response.setUnknownAccidentDeduction(spareSaved.getUnknownAccidentDeduction() != null
								? spareSaved.getUnknownAccidentDeduction().toString()
								: "0.00");
						response.setAmountToBeRecovered(spareSaved.getAmountToBeRecovered() != null
								? spareSaved.getAmountToBeRecovered().toString()
								: "0.00");
						response.setTotalAfterDeductions(spareSaved.getTotalAfterDeductions() != null
								? spareSaved.getTotalAfterDeductions().toString()
								: "0.00");
						response.setVatRatePer(
								spareSaved.getVatRatePercentage() != null ? spareSaved.getVatRatePercentage().toString()
										: "0.00");
						response.setVatRate(
								spareSaved.getVatRate() != null ? spareSaved.getVatRate().toString() : "0.00");
						response.setVatAmount(
								spareSaved.getVatAmount() != null ? spareSaved.getVatAmount().toString() : "0.00");
						response.setTotalWithVAT(
								spareSaved.getTotalWithVat() != null ? spareSaved.getTotalWithVat().toString()
										: "0.00");

			         
			         
			         res.add(response);
				}
				
				comResponse.setErrors(Collections.emptyList());
				comResponse.setMessage("Success");
				comResponse.setResponse(res);
			
			}else {
				
				comResponse.setErrors(Collections.emptyList());
				comResponse.setMessage("Failed");
				comResponse.setResponse(Collections.emptyList());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
                   
         return comResponse;
	}
	
	@Override
	public CommonResponse getSavedGarageSpareParts(SaveSparePartsDTO requestPayload) {
		CommonResponse comResponse = new CommonResponse(); 
        try {
        	LoginMaster loginMaster = loginRepo.findByLoginId(requestPayload.getGarageLoginId());
        	//List<SparePartsSaveDetails> spareSavedList = SparePartsSaveDetailsRepo.findByGarageCode(loginMaster.getCoreAppCode());
        	List<SparePartsSaveDetails> spareSavedList = SparePartsSaveDetailsRepo.findByGarageCodeOrderByEntryDateDesc(loginMaster.getCoreAppCode());
			
			if(spareSavedList != null && spareSavedList.size()>0) {
		    	List<GetAllQuoteResponse> res = new ArrayList<>();
				for(SparePartsSaveDetails spareSaved : spareSavedList ) {
					 GetAllQuoteResponse response = new GetAllQuoteResponse();
			         response.setClaimNo(spareSaved.getClaimNo());
			         response.setWorkOrderNo(spareSaved.getWorkOrderNo());
			         response.setWorkOrderType(spareSaved.getWorkOrderType());
			         response.setWorkOrderDate(spareSaved.getWorkOrderDate());
			         response.setSettlementType(spareSaved.getAccountSettlementType());
			         response.setSettlementTo(spareSaved.getAccountSettlementName());
			         response.setGarageId(spareSaved.getGarageCode().toString());
			         response.setQuotationNo(spareSaved.getQuotationNo());
			         response.setDeliveryDate(spareSaved.getDeliveryDate());
			         response.setJointOrderYn(spareSaved.getJointOrder());
			         response.setSubrogationYn(spareSaved.getSubrogation());
			         response.setTotalLoss(spareSaved.getTotalLoss().toString());
			         response.setLossType(spareSaved.getTotalLossType());
			         response.setRemarks(spareSaved.getRemarks());
			         response.setSavedStatus(spareSaved.getSavedStatus());
			         response.setSparepartsDealerId(Optional.ofNullable(spareSaved.getSparePartsDealer()).map(String ::valueOf).orElse(""));	
			         response.setClgwSgsId(spareSaved.getClgwSgsId());
			         response.setMobileCode(spareSaved.getMobileCode());
			         response.setMobileNo(spareSaved.getMobileNo());			         
						response.setReplacementCost(
								spareSaved.getReplacementCost() != null ? spareSaved.getReplacementCost().toString()
										: "0.00");
						response.setReplacementCostDeductible(spareSaved.getReplacementCostDeductible() != null
								? spareSaved.getReplacementCostDeductible().toString()
								: "0.00");
						response.setSparePartDepreciation(spareSaved.getSparePartDepreciation() != null
								? spareSaved.getSparePartDepreciation().toString()
								: "0.00");
						response.setDiscountOnSpareParts(spareSaved.getDiscountOnSpareParts() != null
								? spareSaved.getDiscountOnSpareParts().toString()
								: "0.00");
						response.setTotalAmountReplacement(spareSaved.getTotalAmountReplacement() != null
								? spareSaved.getTotalAmountReplacement().toString()
								: "0.00");
						response.setRepairLabour(
								spareSaved.getRepairLabour() != null ? spareSaved.getRepairLabour().toString()
										: "0.00");
						response.setRepairLabourDeductible(spareSaved.getRepairLabourDeductible() != null
								? spareSaved.getRepairLabourDeductible().toString()
								: "0.00");
						response.setRepairLabourDiscountAmount(spareSaved.getRepairLabourDiscountAmount() != null
								? spareSaved.getRepairLabourDiscountAmount().toString()
								: "0.00");
						response.setTotalAmountRepairLabour(spareSaved.getTotalAmountRepairLabour() != null
								? spareSaved.getTotalAmountRepairLabour().toString()
								: "0.00");
						response.setNetAmount(
								spareSaved.getNetAmount() != null ? spareSaved.getNetAmount().toString() : "0.00");
						response.setUnknownAccidentDeduction(spareSaved.getUnknownAccidentDeduction() != null
								? spareSaved.getUnknownAccidentDeduction().toString()
								: "0.00");
						response.setAmountToBeRecovered(spareSaved.getAmountToBeRecovered() != null
								? spareSaved.getAmountToBeRecovered().toString()
								: "0.00");
						response.setTotalAfterDeductions(spareSaved.getTotalAfterDeductions() != null
								? spareSaved.getTotalAfterDeductions().toString()
								: "0.00");
						response.setVatRatePer(
								spareSaved.getVatRatePercentage() != null ? spareSaved.getVatRatePercentage().toString()
										: "0.00");
						response.setVatRate(
								spareSaved.getVatRate() != null ? spareSaved.getVatRate().toString() : "0.00");
						response.setVatAmount(
								spareSaved.getVatAmount() != null ? spareSaved.getVatAmount().toString() : "0.00");
						response.setTotalWithVAT(
								spareSaved.getTotalWithVat() != null ? spareSaved.getTotalWithVat().toString()
										: "0.00");

			         
			         
			         res.add(response);
				}
				
				comResponse.setErrors(Collections.emptyList());
				comResponse.setMessage("Success");
				comResponse.setResponse(res);
			
			}else {
				
				comResponse.setErrors(Collections.emptyList());
				comResponse.setMessage("Failed");
				comResponse.setResponse(Collections.emptyList());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
                   
         return comResponse;
	}
	
	public CommonResponse getGarageClaimList(ExternalVehicleGarageViewRequest request) {

	    CommonResponse response = new CommonResponse();
	    ApiTransactionLog log = new ApiTransactionLog();;
	    log.setRequestTime(LocalDateTime.now());
	    log.setEntryDate(new Date());
	    log.setEndpoint(externalApiUrlGarageList);

	    try {

	    	GarageClaimListDto dto = new GarageClaimListDto();
	    	dto.setCategoryId(request.getCategoryId());
	    	dto.setPartyId(request.getPartyId());
	    	dto.setProdId(request.getProdId());
	        
	        // Authenticate and retrieve JWT token
	        String jwtToken = authenticateUserCall();

	        // Create headers and add JWT token
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + jwtToken);
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        // Convert DTO to JSON for request body and add headers
	        String requestBody = objectMapper.writeValueAsString(dto);
	        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
	        log.setRequest(requestBody);
	        
	     // Configure SSL Trust Managers (if necessary)
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
	        
	        // Send request to external API
	        ResponseEntity<String> apiResponse = restTemplate.postForEntity(log.getEndpoint(), entity, String.class);
	        log.setResponse(apiResponse.getBody());
	        log.setStatus("SUCCESS");

	        // Parse response into ExternalApiResponse object
	        GarageClaimListResponseDTO externalApiResponse = objectMapper.readValue(apiResponse.getBody(), GarageClaimListResponseDTO.class);

	        // Process response based on external API success status
	        if (externalApiResponse.isHasError()) {
	            response.setMessage(externalApiResponse.getMessage());
	            //response.setResponse(externalApiResponse);
	            response.setIsError(true);
	        } else {
	            response.setMessage(externalApiResponse.getMessage());
	            response.setIsError(false);
	            response.setResponse(externalApiResponse.getDataset());
	        }

	    } catch (Exception e) {
	        log.setStatus("FAILURE");
	        log.setErrorMessage(e.getMessage());
	        response.setMessage("Failed to get data");
	        response.setIsError(true);
	        response.setErrors(Collections.singletonList(new ErrorResponse("100", "API Error", e.getMessage())));
	    } finally {
	        log.setResponseTime(LocalDateTime.now());
	        //apiTransactionLogRepo.save(log);
	        logger.info(externalApiUrlGarageList +" ==> "+ log);
	    }

	    return response;
	}


	@Override
	public CommonResponse checkClaimStatus(CheckClaimStatusRequest request) {
		CommonResponse response = new CommonResponse();
	    ApiTransactionLog log = new ApiTransactionLog();
	    log.setRequestTime(LocalDateTime.now());
	    log.setEntryDate(new Date());
	    log.setEndpoint(checkClaimStatusApi);

	    try {

	    	ClaimsDetailsRequestDto dto = new ClaimsDetailsRequestDto();
	    	dto.setCustomerId(request.getCustomerId());
	    	dto.setFnolNo(request.getFnolNo());
	        
	        // Authenticate and retrieve JWT token
	        String jwtToken = authenticateUserCall();

	        // Create headers and add JWT token
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + jwtToken);
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        // Convert DTO to JSON for request body and add headers
	        String requestBody = objectMapper.writeValueAsString(dto);
	        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
	        log.setRequest(requestBody);
	        
	     // Configure SSL Trust Managers (if necessary)
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
	        
	        // Send request to external API
	        ResponseEntity<String> apiResponse = restTemplate.postForEntity(log.getEndpoint(), entity, String.class);
	        log.setResponse(apiResponse.getBody());
	        log.setStatus("SUCCESS");

	        // Parse response into ExternalApiResponse object
	        ClaimsDetailsResponseDto externalApiResponse = objectMapper.readValue(apiResponse.getBody(), ClaimsDetailsResponseDto.class);

	        // Process response based on external API success status
	        if (externalApiResponse.isHasError()) {
	            response.setMessage(externalApiResponse.getMessage());
	            //response.setResponse(externalApiResponse);
	            response.setIsError(true);
	        } else {
	        	List<CheckClaimStatusResponse> resList = new ArrayList<>();
	        	List<ClaimDetails> claimsDetailsList = externalApiResponse.getData().getClaimsDetailsList();
	        	for(ClaimDetails CD : claimsDetailsList) {
	        		CheckClaimStatusResponse res = new CheckClaimStatusResponse();
	        		
	        		res.setAccidentNumber(CD.getAccidentNumber());
	                res.setPolicyInceptionDate(parseDate(CD.getPolicyInceptionDate()));
	                res.setPolicyExpiryDate(parseDate(CD.getPolicyExpiryDate()));
	                res.setAccidentLocation(CD.getAccidentLocation());
	                res.setClaimNotificationNo(CD.getClaimNotificationNo());
	                res.setFaultPercentage(CD.getFaultPercentage());
	                res.setIntimationDate(parseDate(CD.getIntimationDate()));
	                res.setClaimType(CD.getClaimType());
	                res.setLossDate(parseDate(CD.getLossDate()));
	                res.setProductId(CD.getProductId());
	                res.setNatureOfLoss(CD.getNatureOfLoss());
	                res.setClaimNo(CD.getClaimNo());
	                res.setFnolNo(CD.getFnolNo());
	                res.setDriverName(CD.getDriverName());
	                res.setLossRemarks(CD.getLossRemarks());
	                res.setPolicyNumber(CD.getPolicyNumber());
	                res.setTotalLossYN(CD.getTotalLossYN());
	                res.setClaimantType(CD.getClaimantType());
	                res.setInsuredName(CD.getInsuredName());
	                res.setCaseNumber(CD.getCaseNumber());
	                res.setClaimStatus(CD.getClaimStatus());
	                res.setCauseOfLoss(CD.getCauseOfLoss());
	                res.setOfficeCode(CD.getOfficeCode());
	                res.setLobName(CD.getLobName());
	                res.setGarageCode(CD.getGarageCode());
	                
	                String surveyorId = "";
	                String gargeId = "";
	                String dealerId = "";

	                List<LoginMaster> loginList = loginMasterRepo.findByCompanyId(request.getCompanyId());

	                try {
	                    surveyorId = loginList.stream()
	                        .filter(login -> "007".equalsIgnoreCase(login.getAgencyCode()) 
	                                         && login.getCoreAppCode().equalsIgnoreCase(CD.getSurveyorId()))
	                        .map(LoginMaster::getLoginId) 
	                        .findFirst()
	                        .orElse("");

	                    gargeId = loginList.stream()
	                        .filter(login -> "009".equalsIgnoreCase(login.getAgencyCode()) 
	                                         && login.getCoreAppCode().equalsIgnoreCase(CD.getSurveyorId()))
	                        .map(LoginMaster::getLoginId) 
	                        .findFirst()
	                        .orElse("");

	                    dealerId = loginList.stream()
	                        .filter(login -> "502".equalsIgnoreCase(login.getAgencyCode()) 
	                                         && login.getCoreAppCode().equalsIgnoreCase(CD.getSurveyorId()))
	                        .map(LoginMaster::getLoginId) 
	                        .findFirst()
	                        .orElse("");
	                    
	                    CD.setSurveyorId(surveyorId);
	                    CD.setGarageId(gargeId);
	                    CD.setDealerSpareId(dealerId);
	                    
	                } catch (Exception e) {
	                    // Log the error
	                    //e.printStackTrace();
	                }

	                res.setSurveyorId(surveyorId);
                    res.setGarageId(gargeId);
                    res.setDealerSpareId(dealerId);
	        		
	        		resList.add(res);
	        		saveClaimStatusToTable(CD);
	        	}
	            response.setMessage(externalApiResponse.getMessage());
	            response.setIsError(false);
	            response.setResponse(resList);
	        }

	    } catch (Exception e) {
	        log.setStatus("FAILURE");
	        log.setErrorMessage(e.getMessage());
	        response.setMessage("Failed to get data");
	        response.setIsError(true);
	        response.setErrors(Collections.singletonList(new ErrorResponse("100", "API Error", e.getMessage())));
	    } finally {
	        log.setResponseTime(LocalDateTime.now());
	        //apiTransactionLogRepo.save(log);
	        logger.info(checkClaimStatusApi +" ==> "+ log);
	    }

	    return response;
	}
	
	private void saveClaimStatusToTable(ClaimDetails claim) {
	    try {
	        // Create a new VcClaimStatus entity
	        VcClaimStatus data = new VcClaimStatus();

	        // Map fields from ClaimDetails to VcClaimStatus
	        data.setClaimNo(claim.getClaimNo());
	        data.setClaimNotificationNo(claim.getClaimNotificationNo());
	        data.setPolicyNumber(claim.getPolicyNumber());
	        data.setClaimStatus(claim.getClaimStatus());
	        //data.setUpdatedAt(new Date()); // You can update this based on your requirements
	        data.setCreatedAt(new Date()); // If needed, update this value
	        data.setLossRemarks(claim.getLossRemarks());
	        data.setFnolNo(claim.getFnolNo());
	        data.setClaimType(claim.getClaimType());
	        data.setLossDate(parseDate(claim.getLossDate()));
	        data.setNatureOfLoss(claim.getNatureOfLoss());
	        data.setProductId(claim.getProductId());
	        data.setDriverName(claim.getDriverName());
	        data.setAccidentNumber(claim.getAccidentNumber());
	        data.setPolicyInceptionDate(parseDate(claim.getPolicyInceptionDate()));
	        //data.setFaultPercentage(claim.getFaultPercentage());
	        data.setAccidentLocation(claim.getAccidentLocation());
	        data.setIntimationDate(parseDate(claim.getIntimationDate()));
	        data.setPolicyExpiryDate(parseDate(claim.getPolicyExpiryDate()));
	        data.setCaseNumber(claim.getCaseNumber());
	        data.setOfficeCode(claim.getOfficeCode());
	        data.setClaimantType(claim.getClaimantType());
	        data.setGarageCode(claim.getGarageCode());
	        data.setTotalLossYN(claim.getTotalLossYN());
	        data.setCauseOfLoss(claim.getCauseOfLoss());
	        data.setLobName(claim.getLobName());
	        data.setInsuredName(claim.getInsuredName());
	        data.setSurveyorId(claim.getSurveyorId());
	        data.setGarageId(claim.getGarageId());
	        data.setDealerId(claim.getDealerSpareId());
	        // Save the data to the database
	        vcClaimStatusRepo.save(data);

	        // Log success if necessary
	        System.out.println("Claim status saved successfully for Claim No: " + claim.getClaimNo());

	    } catch (Exception e) {
	        // Handle the exception
	        System.err.println("Error saving claim status for Claim No: " + claim.getClaimNo());
	        e.printStackTrace();

	    }
	}



	private Date parseDate(String dateString) {
	    if (dateString == null) {
	        return null;
	    }
	    try {
	        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(dateString);
	    } catch (ParseException e) {
	        e.printStackTrace();
	        return null;
	    }
	}


	@Override
	public CommonResponse listClaimantCoverages(ClaimentCoverageRequest request) {
		CommonResponse response = new CommonResponse();
	    ApiTransactionLog log = new ApiTransactionLog();
	    log.setRequestTime(LocalDateTime.now());
	    log.setEntryDate(new Date());
	    log.setEndpoint(listClaimantCoverages);

	    try {

	    	ClaimentCoverageRequestDTO dto = new ClaimentCoverageRequestDTO();
	    	dto.setPolicyNo(request.getPolicyNo());
	    	dto.setRiskId(request.getRiskId());
	    	dto.setSgsId(request.getSgsId());
	    	dto.setClcpClfSgsId(request.getClcpClfSgsId());
	    	
	        // Authenticate and retrieve JWT token
	        String jwtToken = authenticateUserCall();

	        // Create headers and add JWT token
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + jwtToken);
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        // Convert DTO to JSON for request body and add headers
	        String requestBody = objectMapper.writeValueAsString(dto);
	        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
	        log.setRequest(requestBody);
	        
	     // Configure SSL Trust Managers (if necessary)
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
	        
	        // Send request to external API
	        ResponseEntity<String> apiResponse = restTemplate.postForEntity(log.getEndpoint(), entity, String.class);
	        log.setResponse(apiResponse.getBody());
	        log.setStatus("SUCCESS");

	        // Parse response into ExternalApiResponse object
	        ClaimentCoverageResponseDTO externalApiResponse = objectMapper.readValue(apiResponse.getBody(), ClaimentCoverageResponseDTO.class);

	        // Initialize ClaimentCoverageResponse to map the data
	        ClaimentCoverageResponse res = new ClaimentCoverageResponse();

	        if (externalApiResponse.isHasError()) {
	            response.setMessage(externalApiResponse.getMessage());
	            response.setIsError(true);

	            // Map error-specific fields if needed
	            res.setHasError(externalApiResponse.isHasError());
	            res.setStatus(externalApiResponse.getStatus());
	            res.setMessage(externalApiResponse.getMessage());
	        } else {
	            // Map all fields from DTO to the response
	            res.setHasError(externalApiResponse.isHasError());
	            res.setStatus(externalApiResponse.getStatus());
	            res.setData(externalApiResponse.getData());

	            // Map Dataset
	            ClaimentCoverageResponse.Dataset dataset = new ClaimentCoverageResponse.Dataset();
	            dataset.setClaimantList(externalApiResponse.getDataset().getClaimantList());
	            dataset.setCoveragesList(externalApiResponse.getDataset().getCoveragesList());
	            res.setDataset(dataset);

	            // Set success message
	            response.setMessage(externalApiResponse.getMessage());
	            response.setIsError(false);
	            response.setResponse(res);
	        }


	    } catch (Exception e) {
	        log.setStatus("FAILURE");
	        log.setErrorMessage(e.getMessage());
	        response.setMessage("Failed to get data");
	        response.setIsError(true);
	        response.setErrors(Collections.singletonList(new ErrorResponse("100", "API Error", e.getMessage())));
	    } finally {
	        log.setResponseTime(LocalDateTime.now());
	        //apiTransactionLogRepo.save(log);
	        logger.info(listClaimantCoverages +" ==> "+ log);
	    }

	    return response;

	}


	@Override
	public CommonResponse lpoSyncView(LpoApprovalSyncRequest requestPayload) {
		CommonResponse response = new CommonResponse();
	    ApiTransactionLog log = new ApiTransactionLog();
	    log.setSno(apiTransactionLogRepo.findMaxSno()+1);
	    log.setRequestTime(LocalDateTime.now());
	    log.setEntryDate(new Date());
	    
	    // Fetch company ID from request payload (Modify this as needed)
        String companyId = requestPayload.getCompanyId();

        // Fetch API URL from the database
        String apiType = "GET_SPARE_PARTS";  // Match API_TYPE column value
        Optional<ApiIntegMaster> apiConfig = apiIntegMasterRepository.findByCompanyIdAndApiTypeAndStatus(companyId, apiType,"Y");

        if (apiConfig.isEmpty() || apiConfig.get().getApiUrl() == null) {
            response.setMessage("API URL not found for company: " + companyId);
            response.setIsError(true);
            return response;
        }

        String externalApiUrlCreatefnol = apiConfig.get().getApiUrl();
        log.setEndpoint(externalApiUrlCreatefnol);

	    try {
	        
	        LpoApprovalSyncReqDto dto = new LpoApprovalSyncReqDto();
	        dto.setClaimNo(requestPayload.getClaimNo());
	        dto.setLpoId(requestPayload.getLpoId());

	        // Authenticate and retrieve JWT token
	        String jwtToken = authenticateUserCall();

	        // Create headers and add JWT token
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + jwtToken);
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        // Convert DTO to JSON for request body and add headers
	        String requestBody = objectMapper.writeValueAsString(dto);
	        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
	        log.setRequest(requestBody);
	        logger.info(requestBody);
	        
	        // Configure SSL Trust Managers (if necessary)
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
	        
	        // Send request to external API
	        ResponseEntity<String> apiResponse = restTemplate.postForEntity(log.getEndpoint(), entity, String.class);
	        log.setResponse(apiResponse.getBody());
	        log.setStatus("SUCCESS");

	        // Parse response into ExternalApiResponse object
	        LpoApprovalSyncResponse externalApiResponse = objectMapper.readValue(apiResponse.getBody(), LpoApprovalSyncResponse.class);

	        // Process response based on external API success status
	        if (externalApiResponse.getVehicleDamageDetails()!=null) {
	        	List<GarageSectionDetailsSaveReq> groupedDamageDetails = new ArrayList<>();
	        	List<DropDownRes> damageDirection = dropDownServiceImpl.getDamageDirection(companyId);
	        	
	        	Map<String, String> damageDirectionMap = damageDirection.stream()
	        	        .collect(Collectors.toMap(DropDownRes::getCode, DropDownRes::getCodeDesc));

	        	groupedDamageDetails = mapToGroupedDamageDetails(externalApiResponse,damageDirectionMap);
	            response.setMessage("Data Retrived Success");
	            response.setResponse(groupedDamageDetails);
	            response.setIsError(true);

	        }

	    } catch (Exception e) {
	        log.setStatus("FAILURE");
	        log.setErrorMessage(e.getMessage());
	        response.setMessage("Failed to save data");
	        response.setIsError(true);
	        response.setErrors(Collections.singletonList(new ErrorResponse("100", "API Error", e.getMessage())));
	        //partsSaveDetails.setSavedStatus("N");
	    } finally {
	        log.setResponseTime(LocalDateTime.now());
	        if(StringUtils.isNotBlank(log.getRequest())){
	        	apiTransactionLogRepo.save(log);
	        	logger.info("LPO Approval Sync " +" ==> "+ log);
	        }
	    }

	    return response;
	}


	private List<GarageSectionDetailsSaveReq> mapToGroupedDamageDetails(LpoApprovalSyncResponse externalApiResponse,Map<String, String> damageDirectionMap) {
		
		List<GarageSectionDetailsSaveReq> groupedDamageDetails = new ArrayList<>();
	    DecimalFormat df = new DecimalFormat("0.00"); // Ensure two decimal places

	    try {
	        int damageSno = 1;

	        for (LpoApprovalSyncDamageResponse data : externalApiResponse.getVehicleDamageDetails()) {
	            GarageSectionDetailsSaveReq res = new GarageSectionDetailsSaveReq();

	            // Populate fields
	            res.setClaimNo(externalApiResponse.getClaimNo());
	            res.setQuotationNo(externalApiResponse.getGarageQuotationNo());
	            res.setDamageSno(String.valueOf(damageSno));
	            res.setDamageDirection(damageDirectionMap.getOrDefault(data.getDamageDirection(), ""));
	            res.setRepairReplace("REPAIR");
	            res.setNoOfUnits(data.getNoUnits() != null ? data.getNoUnits() : "0");

	            // Convert and format numerical fields
	            res.setReplacementCharge(formatNumber(data.getReplacementCharge(), df));
	            res.setDeductablePer(formatNumber(data.getDeductiblePer(), df));
	            res.setDeductableAmount(formatNumber(data.getDeductibleAmount(), df));

	            groupedDamageDetails.add(res);
	            damageSno++;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return groupedDamageDetails;
	}
	
	private String formatNumber(String value, DecimalFormat df) {
	    if (value == null || value.trim().isEmpty()) {
	        return "0.00"; // Default value
	    }
	    try {
	        return df.format(Double.parseDouble(value));
	    } catch (NumberFormatException e) {
	        return "0.00"; // Handle invalid number inputs
	    }
	}

	@Override
	public CommonResponse getGarageWorkOrder(GetGarageWorkOrderRequest requestPayload) {
	    CommonResponse response = new CommonResponse();
	    ApiTransactionLog transactionLog = new ApiTransactionLog();
	    transactionLog.setRequestTime(LocalDateTime.now());
	    transactionLog.setEntryDate(new Date());

	    try {
	        // Prepare request payload
	        GetGarageWorkOrderRequestDto newdata = new GetGarageWorkOrderRequestDto(requestPayload.getPartyId(), "");

	        // Fetch company ID from request payload
	        String companyId = String.valueOf(requestPayload.getCompanyid());

	        // Fetch API URL from the database
	        String apiType = "WORK_ORDER"; // Match API_TYPE column value
	        Optional<ApiIntegMaster> apiConfig = apiIntegMasterRepository
	                .findByCompanyIdAndApiTypeAndStatus(companyId, apiType, "Y");

	        if (apiConfig.isEmpty() || apiConfig.get().getApiUrl() == null) {
	            response.setMessage("API URL not found for company: " + companyId);
	            response.setIsError(true);
	            return response;
	        }

	        String externalApiUrlCreatefnol = apiConfig.get().getApiUrl();
	        transactionLog.setEndpoint(externalApiUrlCreatefnol);

	        // Authenticate user and get JWT token
	        String jwtToken = authenticateUserCall();

	        // Set up headers
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + jwtToken);
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        // Send request to external API
	        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(newdata), headers);

	        // Configure SSL Trust Managers (if necessary)
	        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
	            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
	            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
	        }};

	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

	        transactionLog.setRequest(newdata.toString());

	        ResponseEntity<String> apiResponse = restTemplate.postForEntity(transactionLog.getEndpoint(), entity, String.class);

	        // Parse API response
	        WorkOrderPendingResponseDto externalApiResponse = objectMapper.readValue(apiResponse.getBody(), WorkOrderPendingResponseDto.class);
	        List<WorkOrderDetailResponseDto> apiData = externalApiResponse.getData();

	        if (apiData == null || apiData.isEmpty()) {
	            response.setMessage("No data received from API");
	            response.setIsError(false);
	            return response;
	        }

	        // Collect file numbers for batch fetching
	        List<String> fileNos = apiData.stream().map(WorkOrderDetailResponseDto::getFileNo).collect(Collectors.toList());
	        List<SparePartsSaveDetails> sparePartsList = new ArrayList<>();

	        // Handle Oracle IN clause limit with batch processing
	        int batchSize = 400;
	        for (int i = 0; i < fileNos.size(); i += batchSize) {
	            int end = Math.min(i + batchSize, fileNos.size());
	            List<String> batch = fileNos.subList(i, end);

	            List<SparePartsSaveDetails> batchResults = SparePartsSaveDetailsRepo
	                    .findByGarageCodeAndClaimNoInOrderByEntryDateDesc(requestPayload.getPartyId(), batch);

	            sparePartsList.addAll(batchResults);
	        }

	        // Process the fetched spare parts data
	        List<GetAllQuoteResponse> res = new ArrayList<>();
	        Map<String, WorkOrderDetailResponseDto> sparePartsMap = apiData.stream()
	                .collect(Collectors.toMap(WorkOrderDetailResponseDto::getFileNo, s -> s, (a, b) -> a));

	        for (SparePartsSaveDetails spareSaved : sparePartsList) {

	            if (spareSaved != null) {
	                GetAllQuoteResponse quoteResponse = new GetAllQuoteResponse();
	                quoteResponse.setClaimNo(spareSaved.getClaimNo());
	                quoteResponse.setWorkOrderNo(spareSaved.getWorkOrderNo());
	                quoteResponse.setWorkOrderType(spareSaved.getWorkOrderType());
	                quoteResponse.setWorkOrderDate(spareSaved.getWorkOrderDate());
	                quoteResponse.setSettlementType(spareSaved.getAccountSettlementType());
	                quoteResponse.setSettlementTo(spareSaved.getAccountSettlementName());
	                quoteResponse.setGarageId(spareSaved.getGarageCode().toString());
	                quoteResponse.setQuotationNo(spareSaved.getQuotationNo());
	                quoteResponse.setDeliveryDate(spareSaved.getDeliveryDate());
	                quoteResponse.setJointOrderYn(spareSaved.getJointOrder());
	                quoteResponse.setSubrogationYn(spareSaved.getSubrogation());
	                quoteResponse.setTotalLoss(spareSaved.getTotalLoss().toString());
	                quoteResponse.setLossType(spareSaved.getTotalLossType());
	                quoteResponse.setRemarks(spareSaved.getRemarks());
	                quoteResponse.setSavedStatus("ESB".equalsIgnoreCase(spareSaved.getSavedStatus()) ? "WST" : spareSaved.getSavedStatus());
	                quoteResponse.setSparepartsDealerId(Optional.ofNullable(spareSaved.getSparePartsDealer()).map(String::valueOf).orElse(""));
	                quoteResponse.setTotalAfterDeductions(sparePartsMap.get(spareSaved.getClaimNo()).getNetamount());
	                quoteResponse.setAmndVersionId(sparePartsMap.get(spareSaved.getClaimNo()).getAmndverno());
	                quoteResponse.setClgwSgsId(spareSaved.getClgwSgsId());
	                quoteResponse.setDeductible(sparePartsMap.get(spareSaved.getClaimNo()).getDeductible());
	                quoteResponse.setMobileCode(spareSaved.getMobileCode());
	                quoteResponse.setMobileNo(spareSaved.getMobileNo());
	                ;
	                res.add(quoteResponse);
	            }
	        }

	        response.setErrors(Collections.emptyList());
	        response.setMessage("Success");
	        response.setResponse(res);

	    } catch (Exception e) {
	        transactionLog.setStatus("FAILURE");
	        transactionLog.setErrorMessage(e.getMessage());
	        response.setMessage("Failed to save data");
	        response.setIsError(true);
	        response.setErrors(Collections.singletonList(new ErrorResponse("100", "API Error", e.getMessage())));
	    } finally {
	        transactionLog.setResponseTime(LocalDateTime.now());
	        logger.info("get Garage WorkOrder ==> " + transactionLog);
	    }

	    return response;
	}

	
	public CommonResponse getGarageWorkOrderV0(GetGarageWorkOrderRequest requestPayload) {
		CommonResponse response = new CommonResponse();
	    ApiTransactionLog transactionLog = new ApiTransactionLog();
	    transactionLog.setRequestTime(LocalDateTime.now());
	    transactionLog.setEntryDate(new Date());

	    try {
	    	// Prepare request payload
	    	GetGarageWorkOrderRequestDto newdata = new GetGarageWorkOrderRequestDto(
	            requestPayload.getPartyId(),""
	        );
	    	
		    // Fetch company ID from request payload (Modify this as needed)
	        String companyId = String.valueOf(requestPayload.getCompanyid());

	        // Fetch API URL from the database
	        String apiType = "CREATE_FNOL";  // Match API_TYPE column value
	        Optional<ApiIntegMaster> apiConfig = apiIntegMasterRepository.findByCompanyIdAndApiTypeAndStatus(companyId, apiType,"Y");

	        if (apiConfig.isEmpty() || apiConfig.get().getApiUrl() == null) {
	            response.setMessage("API URL not found for company: " + companyId);
	            response.setIsError(true);
	            return response;
	        }

	        String externalApiUrlCreatefnol = apiConfig.get().getApiUrl();
	        transactionLog.setEndpoint(externalApiUrlCreatefnol);
	        
	        // Authenticate user and get JWT token
	        String jwtToken = authenticateUserCall();

	        // Set up headers
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + jwtToken);
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        // Send request to external API
	        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(newdata), headers);
	        
	        // Configure SSL Trust Managers (if necessary)
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            
	        ResponseEntity<String> apiResponse = restTemplate.postForEntity(transactionLog.getEndpoint(), entity, String.class);

	        // Parse API response
	        WorkOrderPendingResponseDto externalApiResponse = objectMapper.readValue(apiResponse.getBody(), WorkOrderPendingResponseDto.class);
	        List<WorkOrderDetailResponseDto> apiData = externalApiResponse.getData();
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
	                insured.getClaimNo(),
	                requestPayload.getGarageid()
	            ))
	            .collect(Collectors.toSet());

	        // ✅ Fetch existing records in ONE bulk query
	        Set<InsuredVehicleInfoId> existingIds = repository.findExistingIds(insuredIds);

	        // Process only new records
	        List<InsuredVehicleInfo> insuredInfoList = apiData.stream()
	            .filter(insured -> existingIds.contains(new InsuredVehicleInfoId(
	                requestPayload.getCompanyid(),
	                insured.getPolicyNo(),
	                insured.getFileNo(),
	                requestPayload.getGarageid()
	            )))
	            .map(insured -> {
	                InsuredVehicleInfo insuredVehicleInfo = new InsuredVehicleInfo();
	                insuredVehicleInfo.setCompanyId(requestPayload.getCompanyid());
	                insuredVehicleInfo.setPolicyNo(insured.getPolicyNo());
	                insuredVehicleInfo.setClaimNo(insured.getFileNo());
	                insuredVehicleInfo.setGarageId(requestPayload.getGarageid());
	                insuredVehicleInfo.setEntryDate(new Date());
	                insuredVehicleInfo.setStatus("WST");
	                
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
	        logger.info("get Garage WorkOrder " +" ==> "+ transactionLog);
	    }

	    return response;
	}


	@Override
	public CommonResponse getGarageSettlement(GarageSettlementListRequest requestPayload) {
		CommonResponse response = new CommonResponse();
	    ApiTransactionLog transactionLog = new ApiTransactionLog();
	    transactionLog.setRequestTime(LocalDateTime.now());
	    transactionLog.setEntryDate(new Date());

	    try {
	        // Prepare request payload
	    	GarageSettlementListRequestDto newdata = new GarageSettlementListRequestDto(requestPayload.getPartyId(), "");

	        // Fetch company ID from request payload
	        String companyId = String.valueOf(requestPayload.getCompanyid());

	        // Fetch API URL from the database
	        String apiType = "SETTLEMENT"; // Match API_TYPE column value
	        Optional<ApiIntegMaster> apiConfig = apiIntegMasterRepository
	                .findByCompanyIdAndApiTypeAndStatus(companyId, apiType, "Y");

	        if (apiConfig.isEmpty() || apiConfig.get().getApiUrl() == null) {
	            response.setMessage("API URL not found for company: " + companyId);
	            response.setIsError(true);
	            return response;
	        }

	        String externalApiUrlCreatefnol = apiConfig.get().getApiUrl();
	        transactionLog.setEndpoint(externalApiUrlCreatefnol);

	        // Authenticate user and get JWT token
	        String jwtToken = authenticateUserCall();

	        // Set up headers
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + jwtToken);
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        // Send request to external API
	        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(newdata), headers);

	        // Configure SSL Trust Managers (if necessary)
	        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
	            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
	            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
	        }};

	        SSLContext sc = SSLContext.getInstance("SSL");
	        sc.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

	        transactionLog.setRequest(newdata.toString());

	        ResponseEntity<String> apiResponse = restTemplate.postForEntity(transactionLog.getEndpoint(), entity, String.class);

	        // Parse API response
	        GarageSettlementResponseDto externalApiResponse = objectMapper.readValue(apiResponse.getBody(), GarageSettlementResponseDto.class);
	        List<GarageSettlementListResponseDto> apiData = externalApiResponse.getData();

	        if (apiData == null || apiData.isEmpty()) {
	            response.setMessage("No data received from API");
	            response.setIsError(false);
	            return response;
	        }

	        // Collect file numbers for batch fetching
	        List<String> fileNos = apiData.stream().map(GarageSettlementListResponseDto::getFileNo).collect(Collectors.toList());
	        List<SparePartsSaveDetails> sparePartsList = new ArrayList<>();

	        // Handle Oracle IN clause limit with batch processing
	        int batchSize = 400;
	        for (int i = 0; i < fileNos.size(); i += batchSize) {
	            int end = Math.min(i + batchSize, fileNos.size());
	            List<String> batch = fileNos.subList(i, end);

	            List<SparePartsSaveDetails> batchResults = SparePartsSaveDetailsRepo
	                    .findByGarageCodeAndClaimNoInOrderByEntryDateDesc(requestPayload.getPartyId(), batch);

	            sparePartsList.addAll(batchResults);
	        }

	        // Process the fetched spare parts data
	        List<GetAllQuoteResponse> res = new ArrayList<>();
	        Map<String, GarageSettlementListResponseDto> sparePartsMap = apiData.stream()
	                .collect(Collectors.toMap(GarageSettlementListResponseDto::getFileNo, s -> s, (a, b) -> a));

	        for (SparePartsSaveDetails spareSaved : sparePartsList) {

	            if (spareSaved != null) {
	                GetAllQuoteResponse quoteResponse = new GetAllQuoteResponse();
	                quoteResponse.setClaimNo(spareSaved.getClaimNo());
	                quoteResponse.setWorkOrderNo(spareSaved.getWorkOrderNo());
	                quoteResponse.setWorkOrderType(spareSaved.getWorkOrderType());
	                quoteResponse.setWorkOrderDate(spareSaved.getWorkOrderDate());
	                quoteResponse.setSettlementType(spareSaved.getAccountSettlementType());
	                quoteResponse.setSettlementTo(spareSaved.getAccountSettlementName());
	                quoteResponse.setGarageId(spareSaved.getGarageCode().toString());
	                quoteResponse.setQuotationNo(spareSaved.getQuotationNo());
	                quoteResponse.setDeliveryDate(spareSaved.getDeliveryDate());
	                quoteResponse.setJointOrderYn(spareSaved.getJointOrder());
	                quoteResponse.setSubrogationYn(spareSaved.getSubrogation());
	                quoteResponse.setTotalLoss(spareSaved.getTotalLoss().toString());
	                quoteResponse.setLossType(spareSaved.getTotalLossType());
	                quoteResponse.setRemarks(spareSaved.getRemarks());
	                quoteResponse.setSparepartsDealerId(Optional.ofNullable(spareSaved.getSparePartsDealer()).map(String::valueOf).orElse(""));
	                quoteResponse.setTotalAfterDeductions(sparePartsMap.get(spareSaved.getClaimNo()).getNetamount());
	                quoteResponse.setClgwSgsId(spareSaved.getClgwSgsId());
	                quoteResponse.setPaidamount(sparePartsMap.get(spareSaved.getClaimNo()).getPaidamount());
	                quoteResponse.setOutstanding(sparePartsMap.get(spareSaved.getClaimNo()).getOutstanding());
	                quoteResponse.setDeductible(sparePartsMap.get(spareSaved.getClaimNo()).getDeductible());
	                quoteResponse.setMobileCode(spareSaved.getMobileCode());
	                quoteResponse.setMobileNo(spareSaved.getMobileNo());
	                if(!"ESB".equalsIgnoreCase(spareSaved.getSavedStatus())) {
	                	if("Completed".equalsIgnoreCase(sparePartsMap.get(spareSaved.getClaimNo()).getPaymentStatus())) {
		                	quoteResponse.setSavedStatus("SCT");
		                }else if("Pending".equalsIgnoreCase(sparePartsMap.get(spareSaved.getClaimNo()).getPaymentStatus())) {
		                	quoteResponse.setSavedStatus("WCT");
		                }
		                
		                
		                res.add(quoteResponse);
	                }
	                
	            }
	        }

	        response.setErrors(Collections.emptyList());
	        response.setMessage("Success");
	        response.setResponse(res);

	    } catch (Exception e) {
	        transactionLog.setStatus("FAILURE");
	        transactionLog.setErrorMessage(e.getMessage());
	        response.setMessage("Failed to save data");
	        response.setIsError(true);
	        response.setErrors(Collections.singletonList(new ErrorResponse("100", "API Error", e.getMessage())));
	    } finally {
	        transactionLog.setResponseTime(LocalDateTime.now());
	        logger.info("get Garage WorkOrder ==> " + transactionLog);
	    }

	    return response;
	}


	@Override
	public CommonResponse completeWorkOrder(GetGarageWorkOrderRequest req) {
        CommonResponse response = new CommonResponse();
	    if ("WCT".equalsIgnoreCase(req.getQuoteStatus())) {
			ApiTransactionLog log = new ApiTransactionLog();
			log.setSno(apiTransactionLogRepo.findMaxSno() + 1);
			log.setRequestTime(LocalDateTime.now());
			log.setEntryDate(new Date());
			try {
				// Fetch company ID from request payload
				String companyId = String.valueOf(req.getCompanyid());

				// Fetch API URL from the database
				String apiType = "WORK_BASKET"; // Match API_TYPE column value
				Optional<ApiIntegMaster> apiConfig = apiIntegMasterRepository
						.findByCompanyIdAndApiTypeAndStatus(companyId, apiType, "Y");

				if (apiConfig.isEmpty() || apiConfig.get().getApiUrl() == null) {
					response.setMessage("API URL not found for company: " + companyId);
					response.setIsError(true);
					return response;
				}

				String externalApiUrlCreatefnol = apiConfig.get().getApiUrl();
				log.setEndpoint(externalApiUrlCreatefnol);

				// Step 1: Validate the request
				List<ErrorList> errors = validation.validateCreateWorkBasket(req);
				if (!errors.isEmpty()) {
					// Return early if there are validation errors
					response.setErrors(errors);
					response.setMessage("Validation Failed");
					response.setIsError(true);
					response.setResponse(Collections.emptyList());
					return response;
				}

				String error = uplodeDocument(req);

				// Map entities to request DTO
				CreateWorkBasketRequestDto dto = new CreateWorkBasketRequestDto();

				Optional<InsuredVehicleInfo> optionalInsuredVeh = repository.findByClaimNoAndGarageId(req.getClaimNo(),
						req.getGarageid());
				if (optionalInsuredVeh.isPresent()) {
					InsuredVehicleInfo insuredVeh = optionalInsuredVeh.get();
					insuredVeh.setStatus(req.getQuoteStatus());
					insuredVeh.setEntryDate(new Date());
					insuredVeh.setAmndVerNo(req.getAmndVersionId());

					dto.setClaimNo(insuredVeh.getFileNo());
					dto.setClcpId(insuredVeh.getClcpId());
					dto.setFnolNo(insuredVeh.getFnolNo());
					dto.setProdId(insuredVeh.getProdId());

					repository.save(insuredVeh);
				}

				SparePartsSaveDetails spareSave = SparePartsSaveDetailsRepo.findByClaimNoAndGarageCode(req.getClaimNo(),
						req.getPartyId());

				// Authenticate and retrieve JWT token
				String jwtToken = authenticateUserCall();

				// Create headers and add JWT token
				HttpHeaders headers = new HttpHeaders();
				headers.set("Authorization", "Bearer " + jwtToken);
				headers.setContentType(MediaType.APPLICATION_JSON);

				// Convert DTO to JSON for request body and add headers
				String requestBody = objectMapper.writeValueAsString(dto);
				HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
				log.setRequest(requestBody);
				logger.info(requestBody);
				// Configure SSL Trust Managers (if necessary)
				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					}

					public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					}
				} };

				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
				HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

				// Send request to external API
				ResponseEntity<String> apiResponse = restTemplate.postForEntity(log.getEndpoint(), entity,
						String.class);
				log.setResponse(apiResponse.getBody());
				log.setStatus("SUCCESS");

				// Parse response into ExternalApiResponse object
				ClaimListResponse externalApiResponse = objectMapper.readValue(apiResponse.getBody(),
						ClaimListResponse.class);

				// Process response based on external API success status
				if (!"true".equalsIgnoreCase(externalApiResponse.getHasError())) {
					//response.setMessage(externalApiResponse.getMessage());
					response.setMessage("Success");
					response.setResponse(externalApiResponse);
					response.setIsError(false);
					spareSave.setEntryDate(new Date());
					spareSave.setSavedStatus(req.getQuoteStatus());
					SparePartsSaveDetailsRepo.save(spareSave);
				}

			} catch (Exception e) {
				log.setStatus("FAILURE");
				log.setErrorMessage(e.getMessage());
				response.setMessage("Failed to save data");
				response.setIsError(true);
				response.setErrors(Collections.singletonList(new ErrorResponse("100", "API Error", e.getMessage())));

			} finally {
				log.setResponseTime(LocalDateTime.now());
				if (StringUtils.isNotBlank(log.getRequest())) {
					apiTransactionLogRepo.save(log);
					logger.info(log.getEndpoint() + " ==> " + log);
				}
			} 
		}
	    response.setMessage("Success");
	    if(response.getResponse()==null) {
	    	 response.setResponse("Success");
	    }
	   
		return response;
	}


	private String uplodeDocument(GetGarageWorkOrderRequest req) {
		 try {
		        List<DocumentUploadDetailsReqRes> resList = new ArrayList<>();

		        // Fetch documents by claim number
		        List<VcDocumentUploadDetails> docList = documentUploadDetailsRepo.findByClaimNo(req.getClaimNo());
		        
		        InsuredVehicleInfo insuredVeh = new InsuredVehicleInfo();
		        
		        Optional<InsuredVehicleInfo> optionalInsuredVeh = repository.findByClaimNoAndGarageId(req.getClaimNo(),req.getGarageid());
	            if (optionalInsuredVeh.isPresent()) {
	                insuredVeh = optionalInsuredVeh.get();
				}

		        // Map each VcDocumentUploadDetails to DocumentUploadDetailsReqRes
		        for (VcDocumentUploadDetails doc : docList) {
		            DocumentUploadDetailsReqRes docRes = DocumentUploadDetailsReqRes.builder()
		                .claimNo(doc.getClaimNo())
		                .documentRef(String.valueOf(doc.getDocumentRef()))
		                .docTypeId(doc.getDocTypeId() != null ? String.valueOf(doc.getDocTypeId()) : null)
		                .docDesc(doc.getDescription())
		                .companyId(String.valueOf(doc.getCompanyId()))
		                .filePathName(doc.getFilePathName())
		                .fileName(doc.getFileName())
		                .uploadType(doc.getUploadType())
		                .commonFilePath(doc.getCommonFilePath())
		                .errorRes(doc.getErrorRes())
		                .imgUrl(doc.getFilePathName())
		                .uploadedBy(doc.getUploadedBy())
		                .fileType(doc.getFileType())
		                .build();

		            resList.add(docRes);
		        }

		        // Map the DTO
		        for (DocumentUploadDetailsReqRes res : resList) {
		            FileUploadRequestDto dto = new FileUploadRequestDto();
		            
		            // Set basic details from insured vehicle and request
		            dto.setSgsId(insuredVeh.getFnolSgsId());
		            dto.setAmndVersionId("0");
		            dto.setProductId(insuredVeh.getProdId());
		            dto.setTransactionType("CLM");
		            dto.setPartyId(insuredVeh.getClcpId());
		            dto.setPartyName(insuredVeh.getInsuredName()); 
		            dto.setPartyType(""); // Consider setting a valid type if applicable
		            dto.setCreatedBy("PORTAL");
		            dto.setDocumentTransactionType("CI"); // Ensure this is handled properly
		            dto.setAttachmentRefNo(null); // Ensure this is handled properly

		            // Create and map AttachmentDetails
		            FileUploadRequestDto.AttachmentDetails attachmentDetails = new FileUploadRequestDto.AttachmentDetails();
		            List<FileUploadRequestDto.DocumentDetails> documentDetailsList = new ArrayList<>();

		            // Map individual document details
		            FileUploadRequestDto.DocumentDetails documentDetails = new FileUploadRequestDto.DocumentDetails();
		            documentDetails.setDocumentId(res.getDocTypeId()); 
		            documentDetails.setDocumentName(res.getDocDesc());
		            documentDetails.setDocumentType(res.getFileType());
		            
		            documentDetails.setDocumentData(""); 
		            documentDetails.setDocumentFormat(res.getFileType());
		            documentDetails.setDocumentURL("");
		            documentDetails.setDocumentRefNo(""); 

		            // Add document details to the list
		            documentDetailsList.add(documentDetails);
		            attachmentDetails.setDocumentDetails(documentDetailsList);

		            // Attach document details to DTO
		            dto.setAttachmentDetails(attachmentDetails);

		            // Call external push document API
		            PushDocument(req, dto, res.getImgUrl());
		        }


		    } catch (Exception e) {
		        // Log and set error response details
		        e.printStackTrace();

		    }
		return null;
	}
	
	private void PushDocument(GetGarageWorkOrderRequest req, FileUploadRequestDto dto, String filePath) {
	    ApiTransactionLog log = new ApiTransactionLog();
	    log.setSno(apiTransactionLogRepo.findMaxSno() + 1);
	    log.setRequestTime(LocalDateTime.now());
	    log.setEntryDate(new Date());

	    try {

	        String uploadDocUrl = externalApiUrlUploadDoc;
	        log.setEndpoint(uploadDocUrl);

	        // Authenticate and retrieve JWT token
	        String jwtToken = authenticateUserCall();

	        // Prepare file
	        MultipartFile file = getImageFile(filePath);
	        if (file == null || file.isEmpty()) {
	            throw new RuntimeException("File not found or empty.");
	        }

	        // Prepare request headers
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + jwtToken);
	        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

	        // Convert DTO to JSON string
	        String requestBodyJson = objectMapper.writeValueAsString(dto);
	        log.setRequest(requestBodyJson);

	        LinkedMultiValueMap body = new LinkedMultiValueMap();
	        body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
	        body.add("fileReqest", requestBodyJson);

	        HttpEntity<LinkedMultiValueMap> requestEntity = new HttpEntity<>(body, headers);

	        // Send request to external API
	        ResponseEntity<String> apiResponse = restTemplate.exchange(
	                log.getEndpoint(), HttpMethod.POST, requestEntity, String.class);
	        
	        log.setResponse(apiResponse.getBody());
	        log.setStatus("SUCCESS");

	    } catch (Exception e) {
	        log.setStatus("FAILURE");
	        log.setErrorMessage(e.getMessage());
	    } finally {
	        log.setResponseTime(LocalDateTime.now());
	        if (StringUtils.isNotBlank(log.getRequest())) {
	            apiTransactionLogRepo.save(log);
	            logger.info(log.getEndpoint() + " ==> " + log);
	        }
	    }
	}



	public MultipartFile getImageFile(String path) {
	    try {
	        File file = new File(path);
	        if (StringUtils.isNotBlank(path) && file.exists()) {
	            byte[] array = FileUtils.readFileToByteArray(file);
	            return new BASE64DecodedMultipartFile(array, file.getName());
	        } else {
	            System.out.println("File Not Found");
	            return null;
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	        return null;
	    }
	}


	@Override
	public CommonResponse uplodDocument(GetGarageWorkOrderRequest req) {
		 CommonResponse response = new CommonResponse();
		try {
	        List<DocumentUploadDetailsReqRes> resList = new ArrayList<>();

	        // Fetch documents by claim number
	        List<VcDocumentUploadDetails> docList = documentUploadDetailsRepo.findByClaimNo(req.getClaimNo());
	        
	        InsuredVehicleInfo insuredVeh = new InsuredVehicleInfo();
	        
	        Optional<InsuredVehicleInfo> optionalInsuredVeh = repository.findByClaimNoAndGarageId(req.getClaimNo(),req.getGarageid());
            if (optionalInsuredVeh.isPresent()) {
                insuredVeh = optionalInsuredVeh.get();
			}

	        // Map each VcDocumentUploadDetails to DocumentUploadDetailsReqRes
	        for (VcDocumentUploadDetails doc : docList) {
	            DocumentUploadDetailsReqRes docRes = DocumentUploadDetailsReqRes.builder()
	                .claimNo(doc.getClaimNo())
	                .documentRef(String.valueOf(doc.getDocumentRef()))
	                .docTypeId(doc.getDocTypeId() != null ? String.valueOf(doc.getDocTypeId()) : null)
	                .docDesc(doc.getDescription())
	                .companyId(String.valueOf(doc.getCompanyId()))
	                .filePathName(doc.getFilePathName())
	                .fileName(doc.getFileName())
	                .uploadType(doc.getUploadType())
	                .commonFilePath(doc.getCommonFilePath())
	                .errorRes(doc.getErrorRes())
	                .imgUrl(doc.getFilePathName())
	                .uploadedBy(doc.getUploadedBy())
	                .fileType(doc.getFileType())
	                .build();

	            resList.add(docRes);
	        }

	        // Map the DTO
	        for (DocumentUploadDetailsReqRes res : resList) {
	            FileUploadRequestDto dto = new FileUploadRequestDto();
	            
	            // Set basic details from insured vehicle and request
	            dto.setSgsId(insuredVeh.getFnolSgsId());
//	            dto.setAmndVersionId(req.getAmndVersionId());
	            dto.setAmndVersionId("0");
	            dto.setProductId(insuredVeh.getProdId());
	            dto.setTransactionType("CLM");
	            dto.setPartyId(insuredVeh.getClcpId());
	            dto.setPartyName(insuredVeh.getInsuredName()); 
	            dto.setPartyType(""); // Consider setting a valid type if applicable
	            dto.setCreatedBy("PORTAL");
	            dto.setDocumentTransactionType("CI"); // Ensure this is handled properly
	            dto.setAttachmentRefNo(null); // Ensure this is handled properly

	            // Create and map AttachmentDetails
	            FileUploadRequestDto.AttachmentDetails attachmentDetails = new FileUploadRequestDto.AttachmentDetails();
	            List<FileUploadRequestDto.DocumentDetails> documentDetailsList = new ArrayList<>();

	            // Map individual document details
	            FileUploadRequestDto.DocumentDetails documentDetails = new FileUploadRequestDto.DocumentDetails();
	            documentDetails.setDocumentId(res.getDocTypeId()); 
	            documentDetails.setDocumentName(res.getDocDesc());
	            documentDetails.setDocumentType(res.getFileType());
	            
	            documentDetails.setDocumentData(""); 
	            documentDetails.setDocumentFormat(res.getFileType());
	            documentDetails.setDocumentURL("");
	            documentDetails.setDocumentRefNo(""); 

	            // Add document details to the list
	            documentDetailsList.add(documentDetails);
	            attachmentDetails.setDocumentDetails(documentDetailsList);

	            // Attach document details to DTO
	            dto.setAttachmentDetails(attachmentDetails);

	            // Call external push document API
	            PushDocument(req, dto, res.getImgUrl());
	        }

	        response.setMessage("Success");
            response.setIsError(false);
	    } catch (Exception e) {
	        // Log and set error response details
	        e.printStackTrace();

	    }
	return response;
	}


	@Override
	public CommonResponse getUploadFileList(DownloadDocumentRequest req) {
		CommonResponse response = new CommonResponse();
	    ApiTransactionLog log = new ApiTransactionLog();
	    log.setSno(apiTransactionLogRepo.findMaxSno()+1);
	    log.setRequestTime(LocalDateTime.now());
	    log.setEntryDate(new Date());

	    try {
	        // Fetch company ID from request payload
	        String companyId = String.valueOf(req.getCompanyId());

	        // Fetch API URL from the database
	        String apiType = "UPLOAD_FILE_LIST"; // Match API_TYPE column value
	        Optional<ApiIntegMaster> apiConfig = apiIntegMasterRepository
	                .findByCompanyIdAndApiTypeAndStatus(companyId, apiType, "Y");

	        if (apiConfig.isEmpty() || apiConfig.get().getApiUrl() == null) {
	            response.setMessage("API URL not found for company: " + companyId);
	            response.setIsError(true);
	            return response;
	        }

	        String externalApiUrlCreatefnol = apiConfig.get().getApiUrl();
	        log.setEndpoint(externalApiUrlCreatefnol);
            
            InsuredVehicleInfo insuredVeh = new InsuredVehicleInfo();
	        
	        Optional<InsuredVehicleInfo> optionalInsuredVeh = repository.findByClaimNoAndGarageId(req.getClaimNo(),req.getGarageId());
            if (optionalInsuredVeh.isPresent()) {
                insuredVeh = optionalInsuredVeh.get();
			}
	        
	        // Prepare request body as a simple map
	        Map<String, String> requestBodyMap = new HashMap<>();
	        requestBodyMap.put("sgsId", insuredVeh.getFnolSgsId());
	        
	        // Authenticate and retrieve JWT token
	        String jwtToken = authenticateUserCall();

	        // Create headers and add JWT token
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + jwtToken);
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        // Convert DTO to JSON for request body and add headers
	        String requestBody = objectMapper.writeValueAsString(requestBodyMap);
	        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
	        log.setRequest(requestBody);
	        logger.info(requestBody);
	        
	       // Configure SSL Trust Managers (if necessary)
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
	        
	        // Send request to external API
	        ResponseEntity<String> apiResponse = restTemplate.postForEntity(log.getEndpoint(), entity, String.class);
	        log.setResponse(apiResponse.getBody());
	        log.setStatus("SUCCESS");
	        
	        // Parse response into ExternalApiResponse object
	        UploadedDocumentListResponseDto externalApiResponse = objectMapper.readValue(apiResponse.getBody(), UploadedDocumentListResponseDto.class);

	        // Process response based on external API success status
	        if (!"true".equalsIgnoreCase(externalApiResponse.getHasError())) {
	            //response.setMessage(externalApiResponse.getMessage());
	        	response.setMessage("Success");
	            response.setResponse(externalApiResponse);
	            response.setIsError(false);
	        }

	    } catch (Exception e) {
	        log.setStatus("FAILURE");
	        log.setErrorMessage(e.getMessage());
	        response.setMessage("Failed to save data");
	        response.setIsError(true);
	        response.setErrors(Collections.singletonList(new ErrorResponse("100", "API Error", e.getMessage())));

	    } finally {
	        log.setResponseTime(LocalDateTime.now());
	        if(StringUtils.isNotBlank(log.getRequest())){
	        	apiTransactionLogRepo.save(log);
	        	logger.info(log.getEndpoint()+" ==> "+ log);
	        }
	    }

	    return response;
	}


	@Override
	public CommonResponse downloadDoc(DownloadDocumentRequest req) {
	    CommonResponse response = new CommonResponse();
	    ApiTransactionLog log = new ApiTransactionLog();
	    log.setSno(apiTransactionLogRepo.findMaxSno() + 1);
	    log.setRequestTime(LocalDateTime.now());
	    log.setEntryDate(new Date());

	    try {
	        // Fetch company ID from request payload
	        String companyId = String.valueOf(req.getCompanyId());

	        // Fetch API URL from the database
	        String apiType = "DOWNLOAD_DOC_PRINT";
	        Optional<ApiIntegMaster> apiConfig = apiIntegMasterRepository
	                .findByCompanyIdAndApiTypeAndStatus(companyId, apiType, "Y");

	        if (apiConfig.isEmpty() || apiConfig.get().getApiUrl() == null) {
	            response.setMessage("API URL not found for company: " + companyId);
	            response.setIsError(true);
	            return response;
	        }

	        String externalApiUrl = apiConfig.get().getApiUrl();
	        log.setEndpoint(externalApiUrl);

	        // Prepare DTO
	        DownloadDocumentRequestDto dto = new DownloadDocumentRequestDto();
	        dto.setSgsId(req.getSgsId());
	        dto.setDocId(req.getDocId());
	        dto.setDocPrintType("fileUpload");
	        dto.setFileName(req.getFileName());

	        // Authenticate and retrieve JWT token
	        String jwtToken = authenticateUserCall();

	        // Create headers and add JWT token
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + jwtToken);
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        // Convert DTO to JSON for request body
	        String requestBody = objectMapper.writeValueAsString(dto);
	        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
	        log.setRequest(requestBody);
	        logger.info("Request Payload: " + requestBody);

	        // Call external API expecting a Base64 string as response
	        ResponseEntity<String> apiResponse = restTemplate.exchange(
	                log.getEndpoint(), HttpMethod.POST, entity, String.class);

	        if (apiResponse.getStatusCode() == HttpStatus.OK && apiResponse.getBody() != null) {
	            String base64EncodedFile = apiResponse.getBody();

	            // ✅ Get content type from file extension
	            String contentType = determineContentType(req.getFileName());
	            String base64WithPrefix = "data:" + contentType + ";base64," + base64EncodedFile;

	            // ✅ Return response with prefixed Base64
	            response.setMessage("File retrieved successfully");
	            response.setResponse(base64WithPrefix);
	            response.setIsError(false);
	            log.setResponse("Success");
	            log.setStatus("SUCCESS");
	        } else {
	            response.setMessage("Failed to download document");
	            response.setIsError(true);
	            response.setErrors(Collections.singletonList(new ErrorResponse("101", "Download Failed", "No content received")));
	            log.setStatus("FAILURE");
	        }

	    } catch (Exception e) {
	        log.setStatus("FAILURE");
	        log.setErrorMessage(e.getMessage());
	        response.setMessage("Failed to retrieve file");
	        response.setIsError(true);
	        response.setErrors(Collections.singletonList(new ErrorResponse("100", "API Error", e.getMessage())));
	    } finally {
	        log.setResponseTime(LocalDateTime.now());
	        if (StringUtils.isNotBlank(log.getRequest())) {
	            apiTransactionLogRepo.save(log);
	            logger.info("Transaction Log: " + log.getEndpoint() + " ==> " + log);
	        }
	    }

	    return response;
	}
	
	private String determineContentType(String fileName) {
	    String extension = FilenameUtils.getExtension(fileName).toLowerCase();

	    switch (extension) {
	        case "pdf":
	            return "application/pdf";
	        case "jpg":
	        case "jpeg":
	            return "image/jpeg";
	        case "png":
	            return "image/png";
	        case "gif":
	            return "image/gif";
	        case "doc":
	            return "application/msword";
	        case "docx":
	            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	        case "xls":
	            return "application/vnd.ms-excel";
	        case "xlsx":
	            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	        case "txt":
	            return "text/plain";
	        default:
	            return "application/octet-stream"; // Default binary file type
	    }
	}




}
