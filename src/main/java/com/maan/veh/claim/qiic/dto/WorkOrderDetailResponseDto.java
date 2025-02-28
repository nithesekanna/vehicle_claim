package com.maan.veh.claim.qiic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class WorkOrderDetailResponseDto {
		
	@JsonProperty("partyId")
    private String partyId;
    
    @JsonProperty("workOrderType")
    private String workOrderType;
    
    @JsonProperty("lpoId")
    private String lpoId;
    
    @JsonProperty("vehId")
    private String vehId;
    
    @JsonProperty("clcpId")
    private String clcpId;
    
    @JsonProperty("fnolSgsId")
    private String fnolSgsId;
    
    @JsonProperty("policyNo")
    private String policyNo;
    
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("prodId")
    private String prodId;
    
    @JsonProperty("make")
    private String make;
    
    @JsonProperty("makeKey")
    private String makeKey;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("modelKey")
    private String modelKey;
    
    @JsonProperty("vehRegNo")
    private String vehRegNo;
    
    @JsonProperty("year")
    private String year;
    
    @JsonProperty("modelYear")
    private String modelYear;
    
    @JsonProperty("engineNo")
    private String engineNo;
    
    @JsonProperty("chassisNo")
    private String chassisNo;
    
    @JsonProperty("bodyType")
    private String bodyType;
    
    @JsonProperty("insuredName")
    private String insuredName;
    
    @JsonProperty("insuredid")
    private String insuredId;
    
    @JsonProperty("claimanttyp")
    private String claimantType;
    
    @JsonProperty("claimNo")
    private String claimNo;
    
    @JsonProperty("fnolNo")
    private String fnolNo;
    
    @JsonProperty("lossLocationKey")
    private String lossLocationKey;
    
    @JsonProperty("lossLocation")
    private String lossLocation;
    
    @JsonProperty("lossLocationDesc")
    private String lossLocationDesc;
    
    @JsonProperty("claimStatus")
    private String claimStatus;
    
    @JsonProperty("surveyorId")
    private String surveyorId;
    
    @JsonProperty("fileno")
    private String fileNo;
    
    @JsonProperty("garageaddress")
    private String garageAddress;
    
    @JsonProperty("platetype")
    private String plateType;
    
    @JsonProperty("trafficrepno")
    private String trafficRepNo;
    
    @JsonProperty("createddate")
    private String createdDate;
    
    @JsonProperty("netamount")
    private String netamount;
    
    @JsonProperty("amndverno")
    private String amndverno;
    
    @JsonProperty("deductible")
    private String deductible;
    
}
