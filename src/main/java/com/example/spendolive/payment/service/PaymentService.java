package com.example.spendolive.payment.service;

import com.example.spendolive.member.domain.MemberVO;
import com.example.spendolive.payment.domain.*;

public interface PaymentService {
    public boolean processWithdraw(SettlementPaymentVO paymentInfo,  MemberVO memberInfo) throws Exception;
    public void issueAndSaveBillingKey(String customerKey, String authKey) throws Exception;
    
}
