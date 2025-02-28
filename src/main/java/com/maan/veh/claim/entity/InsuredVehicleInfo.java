package com.maan.veh.claim.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insured_vehicle_info")
@IdClass(InsuredVehicleInfoId.class)
public class InsuredVehicleInfo {

    @Id
    @Column(name = "company_id", nullable = false)
    private Integer companyId;

    @Id
    @Column(name = "policy_no", nullable = false, length = 50)
    private String policyNo;

    @Id
    @Column(name = "claim_no", nullable = false, length = 50)
    private String claimNo;

    @Column(name = "vehicle_make", length = 50)
    private String vehicleMake;

    @Column(name = "vehicle_model", length = 50)
    private String vehicleModel;

    @Column(name = "make_year")
    private Integer makeYear;

    @Column(name = "chassis_no", length = 50)
    private String chassisNo;

    @Column(name = "insured_name", length = 100)
    private String insuredName;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "vehicle_regno", length = 50)
    private String vehicleRegNo;

    @Column(name = "entry_date")
    private Date entryDate;

    @Column(name = "status", length = 20)
    private String status;
<<<<<<< Updated upstream
=======
    
    @Column(name = "fnol_sgs_id", length = 100)
    private String fnolSgsId;
    
    @Id
    @Column(name = "garage_id")
    private String garageId;
    
    @Column(name = "surveyor_id")
    private String surveyorId;
    
    @Column(name = "dealer_id")
    private String dealerId;
    
    @Column(name = "quotation_no")
    private String quotationNo;
    
    @Column(name = "lpo_id")
    private String lpoId;

    // Newly added columns
    @Column(name = "work_order_type", length = 100)
    private String workOrderType;

    @Column(name = "engine_no", length = 100)
    private String engineNo;

    @Column(name = "claimant_type", length = 100)
    private String claimantType;

    @Column(name = "loss_location_desc", length = 2000)
    private String lossLocationDesc;

    @Column(name = "claim_status", length = 100)
    private String claimStatus;

    @Column(name = "file_no", length = 255)
    private String fileNo;

    @Column(name = "garage_address", length = 500)
    private String garageAddress;

    @Column(name = "plate_type", length = 100)
    private String plateType;
    
    @Column(name ="veh_id")
    private String vehId;
    
    @Column(name ="clcp_id")
    private String clcpId;
    
    @Column(name ="prod_id")
    private String prodId;
    
    @Column(name ="fnol_no")
    private String fnolNo;
    
    @Column(name ="amnd_ver_no")
    private String amndVerNo;
    
    @Column(name ="mobile_no")
    private String mobileNo;
    
    @Column(name ="mobile_code")
    private String mobileCode;
>>>>>>> Stashed changes
}
