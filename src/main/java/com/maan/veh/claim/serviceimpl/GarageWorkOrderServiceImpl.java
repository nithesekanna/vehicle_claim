package com.maan.veh.claim.serviceimpl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.maan.veh.claim.entity.GarageWorkOrder;
import com.maan.veh.claim.repository.GarageWorkOrderRepository;
import com.maan.veh.claim.request.GarageWorkOrderRequest;
import com.maan.veh.claim.response.CommonResponse;
import com.maan.veh.claim.response.ErrorList;
import com.maan.veh.claim.response.GarageWorkOrderResponse;
import com.maan.veh.claim.response.GarageWorkOrderSaveReq;
import com.maan.veh.claim.service.GarageWorkOrderService;

@Service
public class GarageWorkOrderServiceImpl implements GarageWorkOrderService {
	
	private static SimpleDateFormat DD_MM_YYYY = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    private GarageWorkOrderRepository garageWorkOrderRepository;
    
    @Autowired
    private InputValidationUtil validation;

    @Override
    public CommonResponse getGarageWorkOrders(GarageWorkOrderRequest req) {
    	List<GarageWorkOrderResponse> res = new ArrayList<>();
    	CommonResponse comResponse = new CommonResponse(); 
        try {
			List<GarageWorkOrder> data = garageWorkOrderRepository.findByCreatedBy(req.getCreatedBy());
			
			if(data.size()>0) {
				
				for(GarageWorkOrder workOrder : data ) {
					 GarageWorkOrderResponse response = new GarageWorkOrderResponse();
			         response.setClaimNo(workOrder.getClaimNo());
			         response.setWorkOrderNo(workOrder.getWorkOrderNo());
			         response.setWorkOrderType(workOrder.getWorkOrderType());
			         response.setWorkOrderDate(workOrder.getWorkOrderDate());
			         response.setSettlementType(workOrder.getSettlementType());
			         response.setSettlementTo(workOrder.getSettlementTo());
			         response.setGarageName(workOrder.getGarageName());
			         response.setGarageId(workOrder.getGarageId().toString());
			         response.setLocation(workOrder.getLocation());
			         response.setRepairType(workOrder.getRepairType());
			         response.setQuotationNo(workOrder.getQuotationNo());
			         response.setDeliveryDate(workOrder.getDeliveryDate());
			         response.setJointOrderYn(workOrder.getJointOrderYn());
			         response.setSubrogationYn(workOrder.getSubrogationYn());
			         response.setTotalLoss(workOrder.getTotalLoss().toString());
			         response.setLossType(workOrder.getLossType());
			         response.setRemarks(workOrder.getRemarks());
			         response.setCreatedBy(workOrder.getCreatedBy());
			         response.setCreatedDate(workOrder.getCreatedDate());
			         response.setUpdatedBy(workOrder.getUpdatedBy());
			         response.setUpdatedDate(workOrder.getUpdatedDate());
			         response.setEntryDate(workOrder.getEntryDate());
			         response.setStatus(workOrder.getStatus());
			         response.setSparepartsDealerId(workOrder.getSparepartsDealerId().toString());
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
	public CommonResponse saveWorkOrder(GarageWorkOrderSaveReq req) {
		CommonResponse response = new CommonResponse();
		try {
			List<ErrorList> error =validation.validateWorkOrder(req);
			if(error.size()>0) {
				GarageWorkOrder work = new GarageWorkOrder();
				work.setClaimNo(req.getClaimNo());
				work.setWorkOrderNo(req.getWorkOrderNo());
				work.setWorkOrderType(req.getWorkOrderType());
				work.setWorkOrderDate(DD_MM_YYYY.parse(req.getWorkOrderDate()));
				work.setSettlementType(req.getSettlementType());
		        work.setSettlementTo(req.getSettlementTo());
		        work.setGarageName(req.getGarageName());
		        work.setGarageId(Integer.valueOf(req.getGarageId()));
		        work.setLocation(req.getLocation());
		        work.setRepairType(req.getRepairType());
		        work.setQuotationNo(req.getQuotationNo());
		        work.setDeliveryDate(DD_MM_YYYY.parse(req.getDeliveryDate()));
		        work.setJointOrderYn(req.getJointOrderYn());
		        work.setSubrogationYn(req.getSubrogationYn());
		        work.setTotalLoss(new BigDecimal(req.getTotalLoss()));
		        work.setLossType(req.getLossType());
		        work.setRemarks(req.getRemarks());
		        work.setCreatedBy(req.getCreatedBy());
		        work.setCreatedDate(new Date());
		        work.setUpdatedBy(req.getUpdatedBy());
		        work.setUpdatedDate(DD_MM_YYYY.parse(req.getUpdatedDate()));
		        work.setEntryDate(new Date());
		        work.setStatus("Y");
		        work.setSparepartsDealerId(Integer.valueOf(req.getSparepartsDealerId()));
		        
		        response.setErrors(Collections.emptyList());
		        response.setMessage("Success");
		        response.setResponse(Collections.emptyList());
			}else {
				 response.setErrors(error);
			     response.setMessage("Failed");
			     response.setResponse(Collections.emptyList());
			     response.setIsError(true);
			}
		}catch (Exception e) {
			e.printStackTrace();
			 response.setErrors(Collections.emptyList());
		     response.setMessage("Failed");
		     response.setResponse(e.getMessage());
		}
		return response;
	}
	
	
	@Override
	public CommonResponse getGarageWorkOrdersByClaimNo(GarageWorkOrderRequest request) {
    	CommonResponse response = new CommonResponse(); 
    	try {
    		GarageWorkOrder data =garageWorkOrderRepository.findByClaimNoAndCreatedBy(request.getClaimNo(), request.getCreatedBy());
    		if(data!=null) {
    			
    			 GarageWorkOrderResponse garage = new GarageWorkOrderResponse();
    			 garage.setClaimNo(data.getClaimNo());
    			 garage.setWorkOrderNo(data.getWorkOrderNo());
    			 garage.setWorkOrderType(data.getWorkOrderType());
    			 garage.setWorkOrderDate(data.getWorkOrderDate());
    			 garage.setSettlementType(data.getSettlementType());	        		 
    			 garage.setGarageName(data.getGarageName());
		         garage.setGarageId(data.getGarageId().toString());
		         garage.setLocation(data.getLocation());
		         garage.setRepairType(data.getRepairType());
		         garage.setQuotationNo(data.getQuotationNo());
		         garage.setDeliveryDate(data.getDeliveryDate());
		         garage.setJointOrderYn(data.getJointOrderYn());
		         garage.setSubrogationYn(data.getSubrogationYn());
		         garage.setTotalLoss(data.getTotalLoss().toString());
		         garage.setLossType(data.getLossType());
		         garage.setRemarks(data.getRemarks());
		         garage.setCreatedBy(data.getCreatedBy());
		         garage.setCreatedDate(data.getCreatedDate());
		         garage.setUpdatedBy(data.getUpdatedBy());
		         garage.setUpdatedDate(data.getUpdatedDate());
		         garage.setEntryDate(data.getEntryDate());
		         garage.setStatus(data.getStatus());
		         garage.setSparepartsDealerId(data.getSparepartsDealerId().toString());
		         
		         response.setErrors(Collections.emptyList());
			     response.setMessage("Success");
			     response.setResponse(garage);
    		}else {
    			 response.setErrors(Collections.emptyList());
			     response.setMessage("Failed");
			     response.setResponse(Collections.emptyList());
			     response.setIsError(true);
   			
    		}
    	
    	}catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
}
