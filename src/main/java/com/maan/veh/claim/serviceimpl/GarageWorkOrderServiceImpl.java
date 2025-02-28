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
<<<<<<< Updated upstream
			         response.setSparepartsDealerId(workOrder.getSparepartsDealerId().toString());
=======
			         response.setSparepartsDealerId(Optional.ofNullable(workOrder.getSparepartsDealerId()).map(String ::valueOf).orElse(""));
			         response.setQuoteStatus(workOrder.getQuoteStatus());			         res.add(response);
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
            // Step 1: Validate the request
            List<ErrorList> errors = validation.validateWorkOrder(req);
            if (!errors.isEmpty()) {
                // Return early if there are validation errors
                response.setErrors(errors);
                response.setMessage("Validation Failed");
                response.setIsError(true);
                response.setResponse(Collections.emptyList());
                return response;
            }

            // Step 2: Find existing work order by claim 
            Optional<GarageWorkOrder> optionalWorkOrder = garageWorkOrderRepository.findByClaimNoAndGarageId(req.getClaimNo(),req.getGarageId());
            GarageWorkOrder workOrder = new GarageWorkOrder();
            if(optionalWorkOrder.isPresent()) {
            	workOrder = optionalWorkOrder.get();
            }

            // Step 4: Set common work order fields
            workOrder.setClaimNo(req.getClaimNo());
            workOrder.setWorkOrderNo(req.getWorkOrderNo());
            workOrder.setWorkOrderType(req.getWorkOrderType());
            workOrder.setWorkOrderTypeDesc(req.getWorkOrderTypeDesc());

            // Parse and set the work order date with error handling
            //try {
                workOrder.setWorkOrderDate(req.getWorkOrderDate());
//            } catch (ParseException e) {
//                response.setErrors(Collections.singletonList("Invalid work order date format."));
//                response.setMessage("Failed");
//                response.setIsError(true);
//                return response;
//            }

            // Step 5: Set settlement details
            workOrder.setSettlementType(req.getSettlementType());
            workOrder.setSettlementTypeDesc(req.getSettlementTypeDesc());
            workOrder.setSettlementTo(req.getSettlementTo());
            workOrder.setSettlementToDesc(req.getSettlementToDesc());

            // Step 6: Set optional fields
            if (StringUtils.isNotBlank(req.getGarageName())) {
                workOrder.setGarageName(req.getGarageName());
            }
            if (StringUtils.isNotBlank(req.getGarageId())) {
                workOrder.setGarageId(req.getGarageId());
            }
            workOrder.setLocation(req.getLocation());
            workOrder.setRepairType(req.getRepairType());

            // Step 7: Handle Quotation Number
            if (StringUtils.isNotBlank(req.getQuotationNo())) {
                workOrder.setQuotationNo(req.getQuotationNo());
            } else {
            	
            	long count = garageWorkOrderRepository.count();
            	count = count + 1;

            	String extractedValue = req.getClaimNo().substring(req.getClaimNo().lastIndexOf("/") + 1);
            	extractedValue = extractedValue.substring(Math.max(extractedValue.length() - 3, 0)); // Get last 3 digits

            	String quoteNo = "QUO-" + extractedValue + "-" + count;
            	workOrder.setQuotationNo(quoteNo);

            }

            // Step 8: Set delivery and other dates
            //try {
                workOrder.setDeliveryDate(req.getDeliveryDate());
//            } catch (ParseException e) {
//                response.setErrors(Collections.singletonList("Invalid delivery date format."));
//                response.setMessage("Failed");
//                response.setIsError(true);
//                return response;
//            }

            workOrder.setJointOrderYn(req.getJointOrderYn());
            workOrder.setSubrogationYn(req.getSubrogationYn());

            // Step 9: Financial details
            workOrder.setTotalLoss(!StringUtils.isBlank(req.getTotalLoss())? new BigDecimal(req.getTotalLoss()) : BigDecimal.ZERO);
            workOrder.setLossType(req.getLossType());
            workOrder.setRemarks(req.getRemarks());

            // Step 10: Set audit fields
            workOrder.setCreatedBy(req.getCreatedBy());
            workOrder.setCreatedDate(new Date());
            workOrder.setUpdatedBy(req.getUpdatedBy());
            workOrder.setUpdatedDate(new Date());
            workOrder.setEntryDate(new Date());

            // Step 11: Status and Quote Status
            workOrder.setStatus(req.getUserType()); 
            
            workOrder.setQuoteStatus(req.getQuoteStatus());

            // Step 12: Set Spareparts Dealer ID (optional)
            workOrder.setSparepartsDealerId(StringUtils.isBlank(req.getSparepartsDealerId()) ? null : req.getSparepartsDealerId());

            // Step 13: Save the work order
            garageWorkOrderRepository.save(workOrder);

            // Step 14: Update Insured Vehicle Status based on Quote Status
            Optional<InsuredVehicleInfo> optionalInsuredVeh = insuredVehRepo.findByClaimNoAndGarageId(req.getClaimNo(),req.getGarageId());
            if (optionalInsuredVeh.isPresent()) {
                InsuredVehicleInfo insuredVeh = optionalInsuredVeh.get();
                insuredVeh.setStatus(req.getQuoteStatus());
                insuredVeh.setQuotationNo(workOrder.getQuotationNo());
                insuredVeh.setEntryDate(new Date());
                insuredVehRepo.save(insuredVeh);
			} else if(StringUtils.isNotBlank(req.getFnolSgsId())) {
				// Instantiate a new InsuredVehicleInfo object
//				InsuredVehicleInfo newInsuredVeh = new InsuredVehicleInfo();
//
//				// Map fields from GarageWorkOrderSaveReq to InsuredVehicleInfo
//				newInsuredVeh.setCompanyId(req.getCompanyId() != null ? Integer.valueOf(req.getCompanyId()) : null);
//				newInsuredVeh.setFnolSgsId(req.getFnolSgsId());
//				newInsuredVeh.setPolicyNo(req.getPolicyNo());
//				newInsuredVeh.setClaimNo(req.getClaimNo());
//				newInsuredVeh.setVehicleMake(req.getVehicleMake());
//				newInsuredVeh.setVehicleModel(req.getVehicleModel());
//				newInsuredVeh.setMakeYear(req.getMakeYear() != null ? Integer.valueOf(req.getMakeYear()) : null);
//				newInsuredVeh.setChassisNo(req.getChassisNo());
//				newInsuredVeh.setInsuredName(req.getInsuredName());
//				newInsuredVeh.setType(req.getType());
//				newInsuredVeh.setVehicleRegNo(req.getVehicleRegNo());
//				newInsuredVeh.setEntryDate(req.getEntryDate());
//				newInsuredVeh.setStatus(req.getStatus());
//				newInsuredVeh.setGarageId(req.getGarageId());
//				newInsuredVeh.setQuotationNo(req.getQuotationNo());
//				insuredVehRepo.save(newInsuredVeh);
////                response.setErrors(Collections.singletonList("No insured vehicle found for claim number: " + req.getClaimNo()));
////                response.setMessage("Failed");
////                response.setIsError(true);
////                return response;
			}
            //saving data in spare parts details table for direct garage save
            if("GPC".equalsIgnoreCase(req.getQuoteStatus())) {
            	directGarageSave(optionalInsuredVeh.get(),workOrder);
            }
            
            // Step 15: Prepare response
            Map<String, String> resMap = new HashMap<>();
            resMap.put("ClaimNo", workOrder.getClaimNo());
            resMap.put("QuotationNo", workOrder.getQuotationNo());

            response.setErrors(Collections.emptyList());
            response.setMessage("Success");
            response.setResponse(resMap);

        } catch (Exception e) {
            // Handle any unexpected exceptions
            e.printStackTrace();
            response.setErrors(Collections.singletonList("An unexpected error occurred: " + e.getMessage()));
            response.setMessage("Failed");
            response.setIsError(true);
            response.setResponse(Collections.emptyList());
        }

        return response;
    }

	
	
    public void directGarageSave(InsuredVehicleInfo insuredVehicleInfo, GarageWorkOrder workOrder) {
    	try {
    		LoginMaster loginMaster = loginRepo.findByLoginId(insuredVehicleInfo.getGarageId());
        	//SparePartsSaveDetails spareSave = SparePartsSaveDetailsRepo.findByClaimNo(workOrder.getClaimNo());
    		SparePartsSaveDetails spareSave = SparePartsSaveDetailsRepo.findByClaimNoAndGarageCode(workOrder.getClaimNo(),loginMaster.getCoreAppCode());
        	if(spareSave == null) {
        		spareSave = new SparePartsSaveDetails();
        	}
			if(workOrder != null) {
				spareSave.setClaimNo(workOrder.getClaimNo());
				spareSave.setWorkOrderNo(workOrder.getWorkOrderNo());
				spareSave.setWorkOrderType("G");
				spareSave.setWorkOrderDate(workOrder.getWorkOrderDate());
				spareSave.setAccountSettlementType("I");
				spareSave.setAccountSettlementName(insuredVehicleInfo.getInsuredName());
				spareSave.setGarageQuotationNo(workOrder.getQuotationNo());
				spareSave.setGarageCode(loginMaster.getCoreAppCode());
				spareSave.setDeliveredTo(workOrder.getGarageName());
				spareSave.setQuotationNo(workOrder.getQuotationNo());
				spareSave.setDeliveryDate(workOrder.getDeliveryDate());
				spareSave.setJointOrder("N");
				spareSave.setSubrogation("N");
				spareSave.setTotalLoss(workOrder.getTotalLoss());
				spareSave.setTotalLossType(workOrder.getLossType());
				spareSave.setRemarks(workOrder.getRemarks());
				spareSave.setSparePartsDealer(loginMaster.getCoreAppCode());		         
				spareSave.setLpoId(insuredVehicleInfo.getLpoId());
				spareSave.setVehId(insuredVehicleInfo.getVehId());
				spareSave.setClcpId(insuredVehicleInfo.getClcpId());
				
				List<DamageSectionDetails> damageList = damageRepository.findByClaimNoAndQuotationNo(workOrder.getClaimNo(), workOrder.getQuotationNo());

				BigDecimal replacementCost = BigDecimal.ZERO;
				BigDecimal repairLabour = BigDecimal.ZERO;
				BigDecimal deductAmount = BigDecimal.ZERO;
				BigDecimal netAmount = BigDecimal.ZERO;

				if (damageList != null) {  // Ensure list is not null
				    for (DamageSectionDetails damage : damageList) {
				        if (damage == null) continue;  // Skip null elements in the list
				        BigDecimal replaceCost = damage.getReplaceCost() != null ? damage.getReplaceCost() : BigDecimal.ZERO;
				        if ("REPAIR".equals(damage.getRepairReplace())) {
				        	deductAmount = deductAmount.add(damage.getLabourCostDeduct()!=null?damage.getLabourCostDeduct():BigDecimal.ZERO);
				            repairLabour = repairLabour.add(replaceCost);
				            netAmount = netAmount.add(replaceCost);
				            replacementCost = replacementCost.add(replaceCost);
				        } else if("REPLACE".equals(damage.getRepairReplace())){
				        	BigDecimal garagePrice = damage.getGaragePrice() != null ? damage.getGaragePrice() : BigDecimal.ZERO;
				        	BigDecimal noOfParts = damage.getNoOfParts() != null ? BigDecimal.valueOf(damage.getNoOfParts()) : BigDecimal.ZERO;
				        	BigDecimal labourCost = damage.getLabourCost() != null ? damage.getLabourCost() : BigDecimal.ZERO;

				        	BigDecimal totamtReplace = garagePrice.multiply(noOfParts).add(labourCost);

				            netAmount = netAmount.add(totamtReplace);
				            replacementCost = replacementCost.add(replaceCost);
				        }
				        
				        
				    }
				}

				 spareSave.setReplacementCost(replacementCost);
	        	 spareSave.setReplacementCostDeductible(BigDecimal.ZERO);
	        	 spareSave.setSparePartDepreciation(BigDecimal.ZERO);
	        	 spareSave.setDiscountOnSpareParts(BigDecimal.ZERO);
	        	 spareSave.setTotalAmountReplacement(replacementCost);
	        	 spareSave.setRepairLabour(repairLabour);
	        	 spareSave.setRepairLabourDeductible(BigDecimal.ZERO);
	        	 spareSave.setRepairLabourDiscountAmount(BigDecimal.ZERO);
	        	 spareSave.setTotalAmountRepairLabour(repairLabour);
	        	 spareSave.setNetAmount(netAmount);
	        	 spareSave.setUnknownAccidentDeduction(deductAmount);
	        	 spareSave.setAmountToBeRecovered(BigDecimal.ZERO);
	        	 spareSave.setTotalAfterDeductions(netAmount.subtract(deductAmount));
	        	 spareSave.setVatRatePercentage(BigDecimal.ZERO);
	        	 spareSave.setVatRate(BigDecimal.ZERO);
	        	 spareSave.setVatAmount(BigDecimal.ZERO);
	        	 spareSave.setTotalWithVat(netAmount.subtract(deductAmount));   
	        	 spareSave.setEntryDate(new Date());
	        	 spareSave.setSavedStatus(insuredVehicleInfo.getStatus());
	        	 spareSave.setFileNo(insuredVehicleInfo.getFileNo());
	        	 spareSave.setMobileCode(insuredVehicleInfo.getMobileCode());
	        	 spareSave.setMobileNo(insuredVehicleInfo.getMobileNo());
	        	 System.out.println("claim number ==> "+ insuredVehicleInfo.getClaimNo() + ", file number == > "+insuredVehicleInfo.getFileNo());
			     SparePartsSaveDetailsRepo.save(spareSave);	
			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	@Override
    public CommonResponse getGarageWorkOrdersByClaimNo(GarageWorkOrderRequest request) {
        CommonResponse response = new CommonResponse(); 
        try {
            // Fetch the work order using Optional to avoid NullPointerException
            Optional<GarageWorkOrder> optionalWorkOrder = garageWorkOrderRepository.findByClaimNoAndGarageId(request.getClaimNo(),request.getGarageLoginId());

            if (optionalWorkOrder.isPresent()) {
                // Work order found, map its fields to the response DTO
                GarageWorkOrder data = optionalWorkOrder.get();
                GarageWorkOrderResponse garage = new GarageWorkOrderResponse();
                
                // Set fields in GarageWorkOrderResponse
                garage.setClaimNo(data.getClaimNo());
                garage.setWorkOrderNo(data.getWorkOrderNo());
                garage.setWorkOrderType(data.getWorkOrderType());
                garage.setWorkOrderTypeDesc(data.getWorkOrderTypeDesc());
                garage.setWorkOrderDate(data.getWorkOrderDate());
                garage.setSettlementType(data.getSettlementType());
                garage.setSettlementTypeDesc(data.getSettlementTypeDesc());
                garage.setSettlementTo(data.getSettlementTo());
                garage.setSettlementToDesc(data.getSettlementToDesc());
                garage.setGarageName(data.getGarageName());
                garage.setGarageId(Optional.ofNullable(data.getGarageId()).map(String::valueOf).orElse(null));  // Handle nullable values
                garage.setLocation(data.getLocation());
                garage.setRepairType(data.getRepairType());
                garage.setQuotationNo(data.getQuotationNo());
                garage.setDeliveryDate(data.getDeliveryDate());
                garage.setJointOrderYn(data.getJointOrderYn());
                garage.setSubrogationYn(data.getSubrogationYn());
                garage.setTotalLoss(Optional.ofNullable(data.getTotalLoss()).map(BigDecimal::toString).orElse(null)); // Handle nullable BigDecimal
                garage.setLossType(data.getLossType());
                garage.setRemarks(data.getRemarks());
                garage.setCreatedBy(data.getCreatedBy());
                garage.setCreatedDate(data.getCreatedDate());
                garage.setUpdatedBy(data.getUpdatedBy());
                garage.setUpdatedDate(data.getUpdatedDate());
                garage.setEntryDate(data.getEntryDate());
                garage.setStatus(data.getStatus());
                garage.setSparepartsDealerId(Optional.ofNullable(data.getSparepartsDealerId()).map(String::valueOf).orElse(null));
                garage.setQuoteStatus(data.getQuoteStatus());
                try {
					SparePartsSaveDetails saveDetails = SparePartsSaveDetailsRepo.findByClaimNo(data.getClaimNo());
					if( saveDetails!=null && "ESB".equalsIgnoreCase(saveDetails.getSavedStatus())){
						garage.setQuoteStatus("WST");
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
                List<DamageSectionDetails> damageList = damageRepository.findByClaimNoAndQuotationNo(data.getClaimNo(), data.getQuotationNo());

                boolean foundReplace = damageList.stream()
                    .anyMatch(damage -> "REPLACE".equalsIgnoreCase(damage.getRepairReplace()));

                garage.setDealerYn(foundReplace ? "Y" : "N");

                // Success response
                response.setErrors(Collections.emptyList());
                response.setMessage("Success");
                response.setResponse(garage);

            } else {
                // No work order found for the given claim number
                //response.setErrors(Collections.singletonList("No work order found for claim number: " + request.getClaimNo()));
                response.setMessage("Failed");
                response.setResponse(Collections.emptyList());
                response.setIsError(true);
            }
        } catch (Exception e) {
            // Log the exception and return a failure response
            e.printStackTrace();
            response.setErrors(Collections.singletonList("An unexpected error occurred: " + e.getMessage()));
            response.setMessage("Failed");
            response.setIsError(true);
            response.setResponse(Collections.emptyList());
        }

        return response;
    }


	@Override
	public CommonResponse getAllGarageWorkOrders(GarageWorkOrderRequest request) {
		CommonResponse comResponse = new CommonResponse(); 
        try {
			List<GarageWorkOrder> data = garageWorkOrderRepository.findAll();
			
			if(data.size()>0) {
		    	List<GarageWorkOrderResponse> res = new ArrayList<>();
				for(GarageWorkOrder workOrder : data ) {
					 GarageWorkOrderResponse response = new GarageWorkOrderResponse();
			         response.setClaimNo(workOrder.getClaimNo());
			         response.setWorkOrderNo(workOrder.getWorkOrderNo());
			         response.setWorkOrderType(workOrder.getWorkOrderType());
			         response.setWorkOrderTypeDesc(workOrder.getWorkOrderTypeDesc());
			         response.setWorkOrderDate(workOrder.getWorkOrderDate());
			         response.setSettlementType(workOrder.getSettlementType());
			         response.setSettlementTypeDesc(workOrder.getSettlementTypeDesc());
			         response.setSettlementTo(workOrder.getSettlementTo());
			         response.setSettlementToDesc(workOrder.getSettlementToDesc());
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
			         response.setSparepartsDealerId(Optional.ofNullable(workOrder.getSparepartsDealerId()).map(String ::valueOf).orElse(""));
			         response.setQuoteStatus(workOrder.getQuoteStatus());
			         
>>>>>>> Stashed changes
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
