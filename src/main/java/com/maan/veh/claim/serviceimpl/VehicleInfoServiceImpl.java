package com.maan.veh.claim.serviceimpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.maan.veh.claim.entity.InsuredVehicleInfo;
import com.maan.veh.claim.repository.InsuredVehicleInfoRepository;
import com.maan.veh.claim.request.VehicleInfoRequest;
import com.maan.veh.claim.response.CommonResponse;
import com.maan.veh.claim.response.VehicleInfoResponse;
import com.maan.veh.claim.service.VehicleInfoService;

@Service
public class VehicleInfoServiceImpl implements VehicleInfoService {

    @Autowired
    private InsuredVehicleInfoRepository insuredVehicleInfoRepository;

    @Override
    public CommonResponse getVehicleInfoByCompanyId(VehicleInfoRequest request) {
    	CommonResponse response = new CommonResponse(); 	
    	List<VehicleInfoResponse> vehList = new ArrayList<>();
        
        try {
            // Fetch the list of vehicle info based on company ID
            List<InsuredVehicleInfo> vehicleInfoList = insuredVehicleInfoRepository.findByCompanyIdAndStatus(Integer.valueOf(request.getCompanyId()),"Y");

            if(vehicleInfoList.size()>0) {
	            // Convert each entity to a response object and add to the response list
	            for (InsuredVehicleInfo vehicle : vehicleInfoList) {
	                // Create a new VehicleInfoResponse object
	                VehicleInfoResponse veh = new VehicleInfoResponse();
	                
	                // Set each property one by one
	                veh.setCompanyId(vehicle.getCompanyId() != null ? String.valueOf(vehicle.getCompanyId()) : null);
	                veh.setPolicyNo(vehicle.getPolicyNo());
	                veh.setClaimNo(vehicle.getClaimNo());
	                veh.setVehicleMake(vehicle.getVehicleMake());
	                veh.setVehicleModel(vehicle.getVehicleModel());
	                veh.setMakeYear(vehicle.getMakeYear() != null ? String.valueOf(vehicle.getMakeYear()) : null);
	                veh.setChassisNo(vehicle.getChassisNo());
	                veh.setInsuredName(vehicle.getInsuredName());
	                veh.setType(vehicle.getType());
	                veh.setVehicleRegNo(vehicle.getVehicleRegNo()); 
	                veh.setEntryDate(vehicle.getEntryDate());
	                veh.setStatus(vehicle.getStatus());
	                
	                // Add the populated response to the list
	                vehList.add(veh);
	            }
	            response.setErrors(Collections.emptyList());
	            response.setMessage("Success");
				response.setResponse(vehList);
			
			}else {
				response.setErrors(Collections.emptyList());
				response.setMessage("Failed");
				response.setIsError(true);
				response.setResponse(Collections.emptyList());
			}
        } catch (Exception e) {
            e.printStackTrace();
            return response;
        }

        return response;
    }
}
