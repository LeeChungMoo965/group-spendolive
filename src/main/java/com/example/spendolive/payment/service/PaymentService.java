package com.example.spendolive.payment.service;

import com.example.spendolive.payment.domain.*;

public interface PaymentService {
    public boolean processWithdraw(SettlementPaymentVO paymentInfo, String accessToken, String memberName) throws Exception;
}
