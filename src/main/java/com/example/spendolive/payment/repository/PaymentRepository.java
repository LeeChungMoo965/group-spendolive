package com.example.spendolive.payment.repository;
import java.util.Map;

import org.springframework.dao.DataAccessException;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.payment.domain.*;
public interface PaymentRepository {

    void updatePaymentStatus(SettlementPaymentVO paymentInfo);
    public SettlementPaymentVO settlement_paymentByroomId(String userId, int roomId) throws DataAccessException;
    void insertEscrow(EscrowPayoutVO escrow);
    
    
}
