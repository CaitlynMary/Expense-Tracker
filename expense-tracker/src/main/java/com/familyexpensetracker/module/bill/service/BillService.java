package com.familyexpensetracker.module.bill.service;

import com.familyexpensetracker.module.bill.dto.BillRequest;
import com.familyexpensetracker.module.bill.dto.BillResponse;

import java.util.List;

public interface BillService {

    BillResponse createBill(BillRequest request);

    BillResponse updateBill(Long id, BillRequest request);

    void deleteBill(Long id);

    BillResponse markBillAsPaid(Long id);

    List<BillResponse> getAllBills();

    List<BillResponse> getUpcomingBills();

    List<BillResponse> getOverdueBills();

    BillResponse getBillById(Long id);
}
