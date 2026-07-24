package com.example.spendolive.payment.domain;

/**
 * OTT 방 결제 화면과 실제 결제 처리에서 공통으로 사용하는 금액 정보입니다.
 * 화면에서 보인 금액과 실제 승인 금액이 달라지지 않도록 서버에서 한 번 계산합니다.
 */
public class PaymentAmountDTO {
    private final int roomId;
    private final int settlementId;
    private final String roomName;
    private final String hostLoginId;
    private final int memberLimit;
    private final int baseAmount;
    private final int feeRate;
    private final int feeAmount;
    private final int totalAmount;
    private final int automaticPaymentDay;

    public PaymentAmountDTO(
            int roomId,
            int settlementId,
            String roomName,
            String hostLoginId,
            int memberLimit,
            int baseAmount,
            int feeRate,
            int feeAmount,
            int totalAmount,
            int automaticPaymentDay) {
        this.roomId = roomId;
        this.settlementId = settlementId;
        this.roomName = roomName;
        this.hostLoginId = hostLoginId;
        this.memberLimit = memberLimit;
        this.baseAmount = baseAmount;
        this.feeRate = feeRate;
        this.feeAmount = feeAmount;
        this.totalAmount = totalAmount;
        this.automaticPaymentDay = automaticPaymentDay;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getSettlementId() {
        return settlementId;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getHostLoginId() {
        return hostLoginId;
    }

    public int getMemberLimit() {
        return memberLimit;
    }

    public int getBaseAmount() {
        return baseAmount;
    }

    public int getFeeRate() {
        return feeRate;
    }

    public int getFeeAmount() {
        return feeAmount;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public int getAutomaticPaymentDay() {
        return automaticPaymentDay;
    }
}
