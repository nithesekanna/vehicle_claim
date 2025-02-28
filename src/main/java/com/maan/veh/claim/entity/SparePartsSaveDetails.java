package com.maan.veh.claim.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "qiic_spare_parts_save_details")
@IdClass(SparePartsSaveDetailsId.class)
public class SparePartsSaveDetails {
	
	@Id
	@Column(name = "claim_no", length = 100)
    private String claimNo;
	
	@Id
	@Column(name = "garage_code", length = 50)
    private String garageCode;
	
    @Column(name = "quotation_no", length = 100)
    private String quotationNo;
    
    @Column(name = "saved_status")
    private String savedStatus;
	
	@Column(name = "work_order_type", length = 50)
    private String workOrderType;

    @Column(name = "work_order_no", length = 50)
    private String workOrderNo;

    @Column(name = "work_order_date")
    @Temporal(TemporalType.DATE)
    private Date workOrderDate;

    @Column(name = "account_settlement_type", length = 50)
    private String accountSettlementType;

    @Column(name = "account_settlement_name", length = 255)
    private String accountSettlementName;

    @Column(name = "spare_parts_dealer", length = 255)
    private String sparePartsDealer;

    @Column(name = "garage_quotation_no", length = 50)
    private String garageQuotationNo;

    @Column(name = "delivery_date")
    @Temporal(TemporalType.DATE)
    private Date deliveryDate;

    @Column(name = "delivered_to", length = 255)
    private String deliveredTo;

    @Column(name = "subrogation", length = 2)
    private String subrogation;

    @Column(name = "joint_order", length = 2)
    private String jointOrder;

    @Column(name = "total_loss", precision = 15, scale = 2)
    private BigDecimal totalLoss;

    @Column(name = "total_loss_type", length = 50)
    private String totalLossType;

    @Column(name = "remarks")
    private String remarks;
    
    @Column(name = "lpo_id")
    private String lpoId;

    @Column(name = "replacement_cost", precision = 15, scale = 2)
    private BigDecimal replacementCost;

    @Column(name = "replacement_cost_deductible", precision = 15, scale = 2)
    private BigDecimal replacementCostDeductible;

    @Column(name = "spare_part_depreciation", precision = 15, scale = 2)
    private BigDecimal sparePartDepreciation;

    @Column(name = "discount_on_spare_parts", precision = 15, scale = 2)
    private BigDecimal discountOnSpareParts;

    @Column(name = "total_amount_replacement", precision = 15, scale = 2)
    private BigDecimal totalAmountReplacement;

    @Column(name = "repair_labour", precision = 15, scale = 2)
    private BigDecimal repairLabour;

    @Column(name = "repair_labour_deductible", precision = 15, scale = 2)
    private BigDecimal repairLabourDeductible;

    @Column(name = "repair_labour_discount_amount", precision = 15, scale = 2)
    private BigDecimal repairLabourDiscountAmount;

    @Column(name = "total_amount_repair_labour", precision = 15, scale = 2)
    private BigDecimal totalAmountRepairLabour;

    @Column(name = "net_amount", precision = 15, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "unknown_accident_deduction", length = 255)
    private BigDecimal unknownAccidentDeduction;

    @Column(name = "amount_to_be_recovered", precision = 15, scale = 2)
    private BigDecimal amountToBeRecovered;

    @Column(name = "total_after_deductions", precision = 15, scale = 2)
    private BigDecimal totalAfterDeductions;

    @Column(name = "vat_rate_percentage", precision = 5, scale = 2)
    private BigDecimal vatRatePercentage;

    @Column(name = "vat_rate", precision = 15, scale = 2)
    private BigDecimal vatRate;

    @Column(name = "vat_amount", precision = 15, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "total_with_vat", precision = 15, scale = 2)
    private BigDecimal totalWithVat;
    
    @Column(name = "entry_date")
    private Date entryDate;
    
    @Column(name = "file_no")
    private String fileNo;
    
    @Column(name = "clgw_sgs_id")
    private String clgwSgsId;
    
    @Column(name ="veh_id")
    private String vehId;
    
    @Column(name ="clcp_id")
    private String clcpId;
    
    @Column(name ="prod_id")
    private String prodId;
    
    @Column(name ="mobile_no")
    private String mobileNo;
    
    @Column(name ="mobile_code")
    private String mobileCode;
}
