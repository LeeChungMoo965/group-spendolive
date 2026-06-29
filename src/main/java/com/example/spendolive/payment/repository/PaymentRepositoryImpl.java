package com.example.spendolive.payment.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.payment.domain.*;


@Repository
public class PaymentRepositoryImpl implements PaymentRepository{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void updatePaymentStatus(SettlementPaymentVO paymentInfo) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updatePaymentStatus'");
    }

    @Override
    public void insertEscrow(EscrowPayoutVO escrow) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertEscrow'");
    }
}
