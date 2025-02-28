package com.maan.veh.claim.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GetAllQuoteResponse {

	@JsonProperty("ClaimNo")
    private String claimNo;
	
	@JsonProperty("QuotationNo")
    private String quotationNo;
	
	@JsonProperty("SparepartsDealerId")
    private String sparepartsDealerId;
	
	@JsonProperty("GarageName")
    private String garageName;

    @JsonProperty("GarageId")
    private String garageId;
    
    @JsonProperty("ReplacementCost")
    private String replacementCost;

    @JsonProperty("ReplacementCostDeductible")
    private String replacementCostDeductible;

    @JsonProperty("SparePartDepreciation")
    private String sparePartDepreciation;

    @JsonProperty("DiscountOnSpareParts")
    private String discountOnSpareParts;

    @JsonProperty("TotalAmountReplacement")
    private String totalAmountReplacement;

    @JsonProperty("RepairLabour")
    private String repairLabour;

    @JsonProperty("RepairLabourDeductible")
    private String repairLabourDeductible;

    @JsonProperty("RepairLabourDiscountAmount")
    private String repairLabourDiscountAmount;

    @JsonProperty("TotalAmountRepairLabour")
    private String totalAmountRepairLabour;

    @JsonProperty("NetAmount")
    private String netAmount;

    @JsonProperty("UnknownAccidentDeduction")
    private String unknownAccidentDeduction;

    @JsonProperty("AmountToBeRecovered")
    private String amountToBeRecovered;

    @JsonProperty("TotalAfterDeductions")
    private String totalAfterDeductions;

    @JsonProperty("VatRatePer")
    private String vatRatePer;

    @JsonProperty("VatRate")
    private String vatRate;

    @JsonProperty("VatAmount")
    private String vatAmount;

    @JsonProperty("TotalWithVAT")
    private String totalWithVAT;
    
    @JsonProperty("SalvageDeduction")
    private String salvageDeduction;
    
    @JsonProperty("WorkOrderNo")
    private String workOrderNo;
 	
 	@JsonProperty("WorkOrderType")
    private String workOrderType;
 	
 	 @JsonProperty("WorkOrderTypeDesc")
     private String workOrderTypeDesc;
 	 
 	@JsonFormat(pattern="dd/MM/yyyy")
 	@JsonProperty("WorkOrderDate")
    private Date workOrderDate;

 	@JsonProperty("SettlementType")
    private String settlementType;
 	
 	@JsonProperty("SettlementTypeDesc")
    private String settlementTypeDesc;

 	@JsonProperty("SettlementTo")
    private String settlementTo;
 	
 	@JsonProperty("SettlementToDesc")
    private String settlementToDesc;

 	@JsonProperty("Location")
    private String location;
 	
 	@JsonProperty("RepairType")
    private String repairType;
 	
 	@JsonFormat(pattern="dd/MM/yyyy")
 	@JsonProperty("DeliveryDate")
    private Date deliveryDate;
 	
 	@JsonProperty("JointOrderYn")
    private String jointOrderYn;
 	
 	@JsonProperty("SubrogationYn")
    private String subrogationYn;
 	
 	@JsonProperty("TotalLoss")
    private String totalLoss;
 	
 	@JsonProperty("LossType")
    private String lossType;
 	
 	@JsonProperty("Remarks")
    private String remarks;
 	
 	@JsonProperty("QuoteStatus")
    private String quoteStatus;
 	
 	@JsonProperty("SavedStatus")
    private String savedStatus;
 	
 	@JsonProperty("UserType")
    private String userType;
 	
 	@JsonProperty("ClgwSgsId")
 	private String clgwSgsId;
 	
 	@JsonProperty("AmndVersionId")
 	private String AmndVersionId;
 	
 	@JsonProperty("Outstanding")
    private String outstanding;
    
    @JsonProperty("Paidamount")
    private String paidamount;
    
    @JsonProperty("MobileNo")
    private String mobileNo;
    
    @JsonProperty("MobileCode")
    private String mobileCode;
    
    @JsonProperty("Deductible")
    private String deductible;
    
}
