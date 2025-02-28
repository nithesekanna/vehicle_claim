package com.maan.veh.claim.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleInfoResponse {

    @JsonProperty("CompanyId")
    private String companyId;

    @JsonProperty("PolicyNo")
    private String policyNo;

    @JsonProperty("ClaimNo")
    private String claimNo;

    @JsonProperty("VehicleMake")
    private String vehicleMake;

    @JsonProperty("VehicleModel")
    private String vehicleModel;

    @JsonProperty("MakeYear")
    private String makeYear;

    @JsonProperty("ChassisNo")
    private String chassisNo;

    @JsonProperty("InsuredName")
    private String insuredName;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("VehicleRegno")
    private String vehicleRegNo;
    
    @JsonFormat(pattern="dd/MM/yyyy")
    @JsonProperty("EntryDate")
    private Date entryDate;

    @JsonProperty("Status")
    private String status;
<<<<<<< Updated upstream
=======
    
    @JsonProperty("QuoteStatus")
    private String quoteStatus;
    
    @JsonProperty("QuotationNo")
    private String quotationNo;
    
    @JsonProperty("DealerLogin")
    private String dealerLogin;
    
    @JsonProperty("GarageLoginId")
    private String garageLoginId;
    
    @JsonProperty("fnolSgsId")
    private String fnolSgsId;
    
    @JsonProperty("lossLocation")
    private String lossLocation;
    
    @JsonProperty("WorkOrderType")
    private String workOrderType;

    @JsonProperty("EngineNo")
    private String engineNo;

    @JsonProperty("ClaimantType")
    private String claimantType;

    @JsonProperty("LossLocationDesc")
    private String lossLocationDesc;

    @JsonProperty("ClaimStatus")
    private String claimStatus;

    @JsonProperty("FileNo")
    private String fileNo;

    @JsonProperty("GarageAddress")
    private String garageAddress;

    @JsonProperty("PlateType")
    private String plateType;
    
    @JsonProperty("MobileNo")
    private String mobileNo;
    
    @JsonProperty("MobileCode")
    private String mobileCode;
>>>>>>> Stashed changes
}
