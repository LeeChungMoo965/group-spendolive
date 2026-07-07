package com.example.spendolive.payment.repository;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.ott.domain.OttRoomDTO;
import com.example.spendolive.ott.domain.OttSettlementDTO;
import com.example.spendolive.payment.domain.*;
public interface PaymentRepository {
    public List<OttRoomDTO> selectTodaysettlement(int day) throws  Exception;
    public void updateEscrowStatus(int roomId);
    public void updatePaymentStatus(SettlementPaymentVO paymentInfo);
    public SettlementPaymentVO settlement_paymentByroomId(String userId, int roomId) throws DataAccessException;
    public void insertEscrow(EscrowPayoutVO escrow);
    public OttSettlementDTO settlementByroomId (int roomId) throws  Exception;
    public void insertPlatfoem_Revenue(PlatformRevenueVO revenueInfo);
    public void insertSeller(SellerAccountVO sellerInfo);
    public void updatSettlementStatus(int roomId);

}
