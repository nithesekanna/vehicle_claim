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
}
