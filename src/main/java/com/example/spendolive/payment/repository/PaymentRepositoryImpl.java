package com.example.spendolive.payment.repository;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.payment.domain.*;


@Repository
public class PaymentRepositoryImpl implements PaymentRepository{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String successpayment = "UPDATE settlement_payment_tb set paid_at =?, paymentKey =?, orderId =?, card_number =?, card_company =?, payment_status =? "
                            +"where id =? ";
    private final String settlement_paymentByroomId = "select "
    +"sp.payment_id, sp.settlement_id, sp.id, sp.base_amount, sp.fee_rate, sp.fee_amount, sp.total_amount, sp.payment_status, sp.card_number," 
    +"sp.card_company, sp.paid_at, sp.confirmed_at, sp.expired_at, sp.cancelled_at, sp.paymentKey, sp.orderId, sp.memo "
    +" from settlement_payment_tb sp JOIN settlement_tb st ON sp.settlement_id = st.settlement_id "
    +"where st.room_id =? AND sp.id = ?";
    public PaymentRepositoryImpl(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public void updatePaymentStatus(SettlementPaymentVO paymentInfo) {
        jdbcTemplate.update(successpayment, paymentInfo.getPaid_at(), paymentInfo.getPaymentKey(), paymentInfo.getOrderId(), paymentInfo.getCard_number(), paymentInfo.getCard_company(), paymentInfo.getPayment_status(), paymentInfo.getId() );
    }
    public SettlementPaymentVO settlement_paymentByroomId(String userId, int roomId) throws DataAccessException{
        try {
            return (SettlementPaymentVO) jdbcTemplate.queryForObject(settlement_paymentByroomId, (rs, rowNum) -> {
            SettlementPaymentVO settlementPaymentVO = new SettlementPaymentVO();
            settlementPaymentVO.setBase_amount(rs.getInt("base_amount"));
            settlementPaymentVO.setPaid_at(rs.getObject("paid_at", LocalDateTime.class));
            settlementPaymentVO.setConfirmed_at(rs.getObject("confirmed_at", LocalDateTime.class));
            settlementPaymentVO.setExpired_at(rs.getObject("expired_at", LocalDateTime.class));
            settlementPaymentVO.setCancelled_at(rs.getObject("cancelled_at", LocalDateTime.class));
            settlementPaymentVO.setCard_company(rs.getString("card_company"));
            settlementPaymentVO.setCard_number(rs.getString("card_number"));
            settlementPaymentVO.setFee_amount(rs.getInt("fee_amount"));
            settlementPaymentVO.setFee_rate(rs.getDouble("fee_rate"));
            settlementPaymentVO.setId(rs.getString("id"));
            settlementPaymentVO.setMemo(rs.getString("memo"));
            settlementPaymentVO.setOrderId(rs.getString("orderId"));
            settlementPaymentVO.setPaymentKey(rs.getString("patmentKey"));
            settlementPaymentVO.setPayment_id(rs.getInt("payment_id"));
            settlementPaymentVO.setPayment_status(rs.getString("payment_status"));
            settlementPaymentVO.setSettlement_id(rs.getInt("settlement_id"));
            settlementPaymentVO.setTotal_amount(rs.getInt("total_amount"));
            return settlementPaymentVO;
            }, roomId,userId);
        }catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // ◀ [수정] 조회가 안 되면(로그인 실패) 에러를 터뜨리지 말고 null을 안전하게 리턴!
            return null; 
        }
    }
    @Override
    public void insertEscrow(EscrowPayoutVO escrow) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'insertEscrow'");
    }
}
