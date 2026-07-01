package com.example.spendolive.payment.repository;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.spendolive.ott.domain.OttSettlementDTO;
import com.example.spendolive.payment.domain.*;


@Repository
public class PaymentRepositoryImpl implements PaymentRepository{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final String successpayment = "INSERT INTO settlement_payment_tb (SETTLEMENT_ID, ID, BASE_AMOUNT, FEE_RATE, FEE_AMOUNT, TOTAL_AMOUNT, PAYMENT_STATUS, CARD_NUMBER, CARD_COMPANY, PAID_AT, PAYMENTKEY, ORDERID, MEMO)  "
    +" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?) ";
    private final String insertEscrow = "INSERT INTO escrow_payout_tb (SETTLEMENT_ID, room_id, payer_id, host_id, amount, status, created_at)  "
    +" VALUES(?,?,?,?,?,?,?) ";
    private final String insertRevenue = "INSERT INTO platform_revenue_tb ("
    +"SETTLEMENT_ID, ROOM_ID, PAYER_ID, BASE_AMOUNT, FEE_RATE, FEE_AMOUNT, STATUS, CREATED_AT) "
    +" VALUES(?,?,?,?,?,?,?,?) ";
    private final String settlement_paymentByroomId = "select "
    +"sp.payment_id, sp.settlement_id, sp.id, sp.base_amount, sp.fee_rate, sp.fee_amount, sp.total_amount, sp.payment_status, sp.card_number," 
    +"sp.card_company, sp.paid_at, sp.confirmed_at, sp.expired_at, sp.cancelled_at, sp.paymentKey, sp.orderId, sp.memo "
    +" from settlement_payment_tb sp JOIN settlement_tb st ON sp.settlement_id = st.settlement_id "
    +"where st.room_id =? AND sp.id = ?";
    private final String settlementByroomId = "select "
    +"st.settlement_id, st.room_id, st.total_price, r.member_limit, r.HOST_LOGIN_ID "
    +"from settlement_tb st "
    +"JOIN ott_room_tb r ON st.room_id = r.room_id where st.room_id =? ";
    
    public PaymentRepositoryImpl(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public void updatePaymentStatus(SettlementPaymentVO paymentInfo) {
        jdbcTemplate.update(successpayment, paymentInfo.getSettlement_id(), paymentInfo.getId() ,paymentInfo.getBase_amount(), paymentInfo.getFee_rate(),paymentInfo.getFee_amount(),paymentInfo.getTotal_amount(), paymentInfo.getPayment_status(), paymentInfo.getCard_number(), paymentInfo.getCard_company(), paymentInfo.getPaid_at(), paymentInfo.getPaymentKey(), paymentInfo.getOrderId(),paymentInfo.getMemo());
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
            settlementPaymentVO.setPaymentKey(rs.getString("paymentKey"));
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
    public OttSettlementDTO settlementByroomId (int roomId) throws DataAccessException{
        try {
            return (OttSettlementDTO) jdbcTemplate.queryForObject(settlementByroomId, (rs, rowNum) -> {
            OttSettlementDTO set = new OttSettlementDTO();
            set.setSettlementId(rs.getLong("settlement_id"));
            set.setRoomId(rs.getLong("room_id"));
            set.setTotalPrice(rs.getInt("total_price"));
            set.setMember_limit(rs.getInt("member_limit"));
            set.setHost_id(rs.getString("host_login_id"));
            return set;
        }, roomId);
    
    }catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // ◀ [수정] 조회가 안 되면(로그인 실패) 에러를 터뜨리지 말고 null을 안전하게 리턴!
            return null; 
        }
    }
    @Override
    public void insertEscrow(EscrowPayoutVO escrowInfo) {
        jdbcTemplate.update(insertEscrow, escrowInfo.getSettlement_id(),escrowInfo.getRoom_id(), escrowInfo.getPayer_id() ,escrowInfo.getHost_id(), escrowInfo.getAmount() ,escrowInfo.getStatus(), escrowInfo.getCreated_at());
    }
    @Override
    public void insertPlatfoem_Revenue(PlatformRevenueVO revenueInfo) {
        jdbcTemplate.update(insertRevenue, revenueInfo.getSettlement_id(),revenueInfo.getRoom_id(), revenueInfo.getPayer_id() , revenueInfo.getBase_amount() ,revenueInfo.getFee_rate(), revenueInfo.getFee_amount(),revenueInfo.getStatus(),revenueInfo.getCreated_at());
    }
} 
