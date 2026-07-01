package com.example.spendolive.payment.repository;
import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.ott.domain.OttSettlementDTO;
import com.example.spendolive.payment.domain.*;
public interface PaymentRepository {

    public void updatePaymentStatus(SettlementPaymentVO paymentInfo);
    public SettlementPaymentVO settlement_paymentByroomId(String userId, int roomId) throws DataAccessException;
    public void insertEscrow(EscrowPayoutVO escrow);
    public OttSettlementDTO settlementByroomId (int roomId) throws  Exception;
    public void insertPlatfoem_Revenue(PlatformRevenueVO revenueInfo);
}
