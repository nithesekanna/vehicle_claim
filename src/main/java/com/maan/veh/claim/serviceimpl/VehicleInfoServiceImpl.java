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
<<<<<<< Updated upstream
    public CommonResponse getVehicleInfoByCompanyId(VehicleInfoRequest request) {
=======
    public CommonResponse getVehicleInfoByCompanyId(VehicleGarageViewRequest request) {
        CommonResponse response = new CommonResponse();    
        try {
            // Fetch the list of vehicle info based on company ID and Garage ID, ordered by EntryDate Desc
            List<InsuredVehicleInfo> vehicleInfoList = insuredVehicleInfoRepository
                    .findByCompanyIdAndGarageIdOrderByEntryDateDesc(
                            Integer.valueOf(request.getCompanyId()), request.getGarageId());

            if (vehicleInfoList.isEmpty()) {
                response.setErrors(Collections.emptyList());
                response.setMessage("Failed");
                response.setIsError(true);
                response.setResponse(Collections.emptyList());
                return response;
            }

            // Fetch all work orders in smaller batches to avoid ORA-01795
            Set<String> claimNos = vehicleInfoList.stream()
                    .map(InsuredVehicleInfo::getClaimNo)
                    .collect(Collectors.toSet());

            Map<String, String> workOrderMap = new HashMap<>();
            List<String> claimNoList = new ArrayList<>(claimNos);
            int batchSize = 500; // Oracle limit
            for (int i = 0; i < claimNoList.size(); i += batchSize) {
                List<String> batch = claimNoList.subList(i, Math.min(i + batchSize, claimNoList.size()));
                garageWorkOrderRepository.findByClaimNoInAndGarageId(new HashSet<>(batch), request.getGarageId())
                        .forEach(workOrder -> workOrderMap.putIfAbsent(workOrder.getClaimNo(), workOrder.getQuotationNo()));
            }

            // Convert each entity to a response object using streams
            List<VehicleInfoResponse> vehList = vehicleInfoList.stream()
                    .map(vehicle -> {
                        VehicleInfoResponse veh = new VehicleInfoResponse();
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
                        veh.setFnolSgsId(vehicle.getFnolSgsId());
                        veh.setLossLocation(vehicle.getLossLocation());
                        veh.setQuoteStatus("Y".equalsIgnoreCase(vehicle.getStatus()) ? "PFG" : vehicle.getStatus());

                        // Set quotation number from pre-fetched workOrderMap
                        veh.setQuotationNo(workOrderMap.getOrDefault(vehicle.getClaimNo(), ""));
                        
                        veh.setWorkOrderType(vehicle.getWorkOrderType());
                        veh.setEngineNo(vehicle.getEngineNo());
                        veh.setClaimantType(vehicle.getClaimantType());
                        veh.setLossLocationDesc(vehicle.getLossLocationDesc());
                        veh.setClaimStatus(vehicle.getClaimStatus());
                        veh.setFileNo(vehicle.getFileNo());
                        veh.setGarageAddress(vehicle.getGarageAddress());
                        veh.setPlateType(vehicle.getPlateType());
                        veh.setMobileCode(vehicle.getMobileCode());
                        veh.setMobileNo(vehicle.getMobileNo());
                        
                        return veh;
                    }).collect(Collectors.toList());

            response.setErrors(Collections.emptyList());
            response.setMessage("Success");
            response.setResponse(vehList);

        } catch (Exception e) {
            e.printStackTrace();
            response.setMessage("Error occurred");
            response.setIsError(true);
            response.setErrors(Collections.singletonList(e.getMessage()));
        }

        return response;
    }


    public CommonResponse getVehicleInfoByCompanyIdV0(VehicleGarageViewRequest request) {
>>>>>>> Stashed changes
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
